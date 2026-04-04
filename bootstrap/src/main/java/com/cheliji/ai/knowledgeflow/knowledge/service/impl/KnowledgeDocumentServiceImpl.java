package com.cheliji.ai.knowledgeflow.knowledge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.cheliji.ai.knowledgeflow.core.chunk.ChunkingMode;
import com.cheliji.ai.knowledgeflow.framework.context.UserContext;
import com.cheliji.ai.knowledgeflow.framework.exception.ClientException;
import com.cheliji.ai.knowledgeflow.knowledge.controller.request.KnowledgeDocumentUploadRequest;
import com.cheliji.ai.knowledgeflow.knowledge.controller.vo.KnowledgeDocumentVO;
import com.cheliji.ai.knowledgeflow.knowledge.dao.entity.KnowledgeBaseDO;
import com.cheliji.ai.knowledgeflow.knowledge.dao.entity.KnowledgeDocumentDO;
import com.cheliji.ai.knowledgeflow.knowledge.dao.mapper.KnowledgeBaseMapper;
import com.cheliji.ai.knowledgeflow.knowledge.dao.mapper.KnowledgeDocumentMapper;
import com.cheliji.ai.knowledgeflow.knowledge.enums.DocumentStatus;
import com.cheliji.ai.knowledgeflow.knowledge.enums.ProcessMode;
import com.cheliji.ai.knowledgeflow.knowledge.enums.SourceType;
import com.cheliji.ai.knowledgeflow.knowledge.handler.RemoteFileFetcher;
import com.cheliji.ai.knowledgeflow.knowledge.service.KnowledgeDocumentService;
import com.cheliji.ai.knowledgeflow.rag.dto.StoredFileDTO;
import com.cheliji.ai.knowledgeflow.rag.service.FileStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeDocumentServiceImpl implements KnowledgeDocumentService {

    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final FileStorageService fileStorageService;
    private final RemoteFileFetcher remoteFileFetcher;
    private final ObjectMapper objectMapper;
    private final ReactiveRedisTemplate reactiveRedisTemplate;

    @Override
    @Transactional
    public KnowledgeDocumentVO upload(String kbId, MultipartFile file, KnowledgeDocumentUploadRequest requestParam) {
        // 检查知识库是否存在
        KnowledgeBaseDO knowledgeBaseDO = knowledgeBaseMapper.selectById(kbId);
        if (knowledgeBaseDO == null) {
            throw new ClientException("知识库不存在") ;
        }
        // 获取来源类型
        SourceType sourceType = SourceType.normalize(requestParam.getSourceType());
        // TODO 检查定时拉去参数是否合法

        // 获取分块配置
        ProcessModeConfig modeConfig = resolveProcessModeConfig(requestParam) ;

        // 上传文件
        StoredFileDTO stored = resolveStoredFile(knowledgeBaseDO.getCollectionName(),sourceType,requestParam.getSourceLocation(),file) ;

        // 保存文档信息
        KnowledgeDocumentDO documentDO = KnowledgeDocumentDO.builder()
                .kbId(knowledgeBaseDO.getId())
                .docName(stored.getOriginalFilename())
                .enabled(1)
                .fileUrl(stored.getUrl())
                .fileSize(stored.getSize())
                .processMode(modeConfig.processMode.getValue())
                .status(DocumentStatus.PENDING.getCode())
                .sourceType(sourceType.getValue())
                .sourceLocation(sourceType == SourceType.URL ? requestParam.getSourceLocation() : null)
                .scheduleEnabled(isScheduleEnabled(sourceType, requestParam) ? 1 : 0)
                .scheduleCron(isScheduleEnabled(sourceType, requestParam) ? StrUtil.trimToNull(requestParam.getScheduleCron()) : null)
                .chunkStrategy(modeConfig.chunkingMode() != null ? modeConfig.chunkingMode().getValue() : null)
                .chunkConfig(modeConfig.chunkConfig)
                .pipelineId(modeConfig.pipelineId())
                .createdBy(UserContext.getUserId())
                .updatedBy(UserContext.getUserId())
                .build();

        knowledgeDocumentMapper.insert(documentDO);


        return BeanUtil.toBean(documentDO, KnowledgeDocumentVO.class);
    }

    private boolean isScheduleEnabled(SourceType sourceType, KnowledgeDocumentUploadRequest requestParam) {
        return SourceType.URL == sourceType && Boolean.TRUE.equals(requestParam.getScheduleEnabled());
    }

    private ProcessModeConfig resolveProcessModeConfig(KnowledgeDocumentUploadRequest requestParam) {

        ProcessMode processMode = ProcessMode.normalize(requestParam.getProcessMode());

        if (ProcessMode.CHUNK == processMode) {
            ChunkingMode chunkingMode = ChunkingMode.fromValue(requestParam.getProcessMode());
            String chunkConfig = validateAndNormalizeChunkConfig(chunkingMode,requestParam.getChunkConfig()) ;
            return new ProcessModeConfig(processMode,chunkingMode,chunkConfig,null) ;
        } else {
            if (!StringUtils.hasText(requestParam.getPipelineId())) {
                throw new ClientException("使用 Pipeline 模式，必须指明 Pipeline ID") ;
            }

            // TODO 查询指定的 Pipeline

            return new ProcessModeConfig(processMode,null,null,requestParam.getPipelineId()) ;
        }

    }

    private String validateAndNormalizeChunkConfig(ChunkingMode chunkingMode, String chunkConfig) {

        if (!StringUtils.hasText(chunkConfig)) {
            return null ;
        }

        if (chunkingMode == null)
            chunkingMode = ChunkingMode.STRUCTURE_AWARE ;

        String json = chunkConfig.trim() ;

        Map<String,Object> config ;

        try {
            config = objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new ClientException("分块参数JSON格式不合法");
        }

        for (String key : chunkingMode.getDefaultConfig().keySet()) {
            if (!config.containsKey(key)) {
                throw new ClientException("分块参数缺少必要字段：" + key) ;
            }
        }
        return json ;

    }

    private StoredFileDTO resolveStoredFile(String collectionName, SourceType sourceType, String sourceLocation, MultipartFile file) {
        if (sourceType == SourceType.FILE) {

            if (file == null || file.isEmpty()) {
                throw new ClientException("上传文件不能为空") ;
            }

            return fileStorageService.upload(collectionName,file) ;
        }

        return remoteFileFetcher.fetchAndStore(collectionName,sourceLocation) ;

    }

    private record ProcessModeConfig(ProcessMode processMode, ChunkingMode chunkingMode, String chunkConfig,
                                     String pipelineId) {
    }
}
