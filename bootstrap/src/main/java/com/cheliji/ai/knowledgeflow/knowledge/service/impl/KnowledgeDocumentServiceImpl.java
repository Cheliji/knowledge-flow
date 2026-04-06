package com.cheliji.ai.knowledgeflow.knowledge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cheliji.ai.knowledgeflow.core.chunk.*;
import com.cheliji.ai.knowledgeflow.core.parser.DocumentParserSelector;
import com.cheliji.ai.knowledgeflow.core.parser.ParserType;
import com.cheliji.ai.knowledgeflow.framework.context.UserContext;
import com.cheliji.ai.knowledgeflow.framework.exception.ClientException;
import com.cheliji.ai.knowledgeflow.knowledge.controller.request.KnowledgeChunkCreateRequest;
import com.cheliji.ai.knowledgeflow.knowledge.controller.request.KnowledgeDocumentUploadRequest;
import com.cheliji.ai.knowledgeflow.knowledge.controller.vo.KnowledgeDocumentVO;
import com.cheliji.ai.knowledgeflow.knowledge.dao.entity.KnowledgeBaseDO;
import com.cheliji.ai.knowledgeflow.knowledge.dao.entity.KnowledgeDocumentChunkLogDO;
import com.cheliji.ai.knowledgeflow.knowledge.dao.entity.KnowledgeDocumentDO;
import com.cheliji.ai.knowledgeflow.knowledge.dao.mapper.KnowledgeBaseMapper;
import com.cheliji.ai.knowledgeflow.knowledge.dao.mapper.KnowledgeDocumentChunkLogMapper;
import com.cheliji.ai.knowledgeflow.knowledge.dao.mapper.KnowledgeDocumentMapper;
import com.cheliji.ai.knowledgeflow.knowledge.enums.DocumentStatus;
import com.cheliji.ai.knowledgeflow.knowledge.enums.ProcessMode;
import com.cheliji.ai.knowledgeflow.knowledge.enums.SourceType;
import com.cheliji.ai.knowledgeflow.knowledge.handler.RemoteFileFetcher;
import com.cheliji.ai.knowledgeflow.knowledge.service.KnowledgeChunkService;
import com.cheliji.ai.knowledgeflow.knowledge.service.KnowledgeDocumentService;
import com.cheliji.ai.knowledgeflow.rag.dto.StoredFileDTO;
import com.cheliji.ai.knowledgeflow.rag.service.FileStorageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeDocumentServiceImpl implements KnowledgeDocumentService {

    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final FileStorageService fileStorageService;
    private final RemoteFileFetcher remoteFileFetcher;
    private final ObjectMapper objectMapper;
    private final KnowledgeDocumentChunkLogMapper knowledgeDocumentChunkLogMapper;
    private final DocumentParserSelector parserSelector ;
    private final ChunkingStrategyFactory ChunkingStrategyFactory ;
    private final ChunkEmbeddingService chunkEmbeddingService ;
    private final TransactionOperations transactionOperations;
    private final KnowledgeChunkService knowledgeChunkService;

    @Qualifier("chunkThreadPoolExecutor")
    private final Executor chunkThreadPoolExecutor;

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

    @Override
    public void startChunk(String docId) {

        KnowledgeDocumentDO knowledgeDocumentDO = knowledgeDocumentMapper.selectById(docId);
        if (knowledgeDocumentDO == null) {
            throw new ClientException("文档不存在") ;
        }

        if (knowledgeDocumentDO.getEnabled() == 0) {
            throw new ClientException("文档被禁用，目前不可执行分块操作") ;
        }

        int update = knowledgeDocumentMapper.update(
                new LambdaUpdateWrapper<KnowledgeDocumentDO>()
                        .set(KnowledgeDocumentDO::getStatus, DocumentStatus.RUNNING)
                        .set(KnowledgeDocumentDO::getUpdatedBy, UserContext.getUserId())
                        .eq(KnowledgeDocumentDO::getId, knowledgeDocumentDO.getId())
                        .ne(KnowledgeDocumentDO::getStatus, DocumentStatus.RUNNING)
        );

        if (update == 0) {
            throw new ClientException("文档分块操作正在进行中，请稍后再试");
        }
        
        //TODO 共享文档应该定时更新拉取
        //TODO 当前版本没有引入 RocketMQ 暂时使用线程池代替

        chunkThreadPoolExecutor.execute(() -> executeChunk(knowledgeDocumentDO.getId()));

    }

    private void executeChunk(String docId) {

        KnowledgeDocumentDO knowledgeDocumentDO = knowledgeDocumentMapper.selectById(docId);
        if (knowledgeDocumentDO == null) {
            log.warn("文档不存在");
            return ;
        }

        runChunk(knowledgeDocumentDO) ;

    }

    private void runChunk(KnowledgeDocumentDO knowledgeDocumentDO) {

        // 开始执行分块操作，获取分块策略和分块配置 ;
        String docId = knowledgeDocumentDO.getId();
        ProcessMode processMode = ProcessMode.normalize(knowledgeDocumentDO.getProcessMode());

        KnowledgeDocumentChunkLogDO chunkLog = KnowledgeDocumentChunkLogDO.builder()
                .docId(docId)
                .status(DocumentStatus.RUNNING.getCode())
                .processMode(processMode.getValue())
                .chunkStrategy(knowledgeDocumentDO.getChunkStrategy())
                .pipelineId(knowledgeDocumentDO.getPipelineId())
                .startTime(new Date())
                .build();
        knowledgeDocumentChunkLogMapper.insert(chunkLog) ;

        long totalStartTime = System.currentTimeMillis();
        long extractDuration = 0 ;
        long chunkDuration = 0 ;
        long embedDuration = 0 ;
        long persistDuration = 0 ;

        try {
            List<VectorChunk> chunkResult = List.of();

            if (ProcessMode.PIPELINE == processMode) {
                // pipeline 处理模式
            } else {
                ChunkProcessResult result = runChunkProcess(knowledgeDocumentDO) ;
                extractDuration = result.extractDuration ;
                chunkDuration = result.chunkDuration;
                embedDuration = result.embedDuration;
                chunkResult =result.chunks() ;
            }

            long persisStart = System.currentTimeMillis();
            String collectionName = knowledgeBaseMapper.selectById(knowledgeDocumentDO.getKbId()).getCollectionName() ;
            int savedCount = persistChunkAndVectorsAtomically(collectionName,docId,chunkResult) ;
            persistDuration = System.currentTimeMillis() - persisStart;

            long totalDuration = System.currentTimeMillis() - totalStartTime;

            updateChunkLog(chunkLog.getId(),
                    DocumentStatus.SUCCESS.getCode(),
                    savedCount,
                    extractDuration,
                    chunkDuration,
                    embedDuration,
                    persistDuration,
                    totalDuration ,
                    null
                    );

        } catch (Exception e) {
            log.error("文档分块任务执行失败：docId={}", docId, e);
            markChunkFailed(knowledgeDocumentDO.getId()) ;
            long totalDuration = System.currentTimeMillis() - totalStartTime;
            updateChunkLog(chunkLog.getId(),
                    DocumentStatus.FAILED.getCode(),
                    0,
                    extractDuration,
                    chunkDuration,
                    embedDuration,
                    persistDuration,
                    totalDuration,
                    e.getMessage()
            );
        }



    }

    private void markChunkFailed(String id) {
        knowledgeDocumentMapper.update(
                new LambdaUpdateWrapper<KnowledgeDocumentDO>()
                        .eq(KnowledgeDocumentDO::getId, id)
                        .set(KnowledgeDocumentDO::getStatus, DocumentStatus.FAILED.getCode())
        ) ;
    }

    private void updateChunkLog(String id, String code, int savedCount, long extractDuration, long chunkDuration, long embedDuration, long persistDuration, long totalDuration, String errorMessage) {

        KnowledgeDocumentChunkLogDO chunkLogDO = knowledgeDocumentChunkLogMapper.selectById(id);
        chunkLogDO.setChunkCount(savedCount);
        chunkLogDO.setExtractDuration(extractDuration);
        chunkLogDO.setChunkDuration(chunkDuration);
        chunkLogDO.setEmbeddingDuration(embedDuration);
        chunkLogDO.setPersistDuration(persistDuration);
        chunkLogDO.setTotalDuration(totalDuration);
        chunkLogDO.setErrorMessage(errorMessage);

        knowledgeDocumentChunkLogMapper.updateById(chunkLogDO) ;

    }

    private int persistChunkAndVectorsAtomically(String collectionName, String docId, List<VectorChunk> chunkResult) {
        List<KnowledgeChunkCreateRequest> chunks = chunkResult.stream()
                .map(vc -> {
                    KnowledgeChunkCreateRequest req = new KnowledgeChunkCreateRequest();
                    req.setChunkId(vc.getChunkId());
                    req.setIndex(vc.getIndex());
                    req.setContent(vc.getContent());
                    return req ;
                })
                .toList() ;

        transactionOperations.executeWithoutResult(status -> {

            // TODO 将 chunk 存储至向量数据库和数据库中
            knowledgeChunkService.deleteByDocId(docId);
            knowledgeChunkService.batchCreate(docId, chunks);



            KnowledgeDocumentDO updateDocumentDO = KnowledgeDocumentDO.builder()
                    .id(docId)
                    .chunkCount(chunks.size())
                    .status(DocumentStatus.SUCCESS.getCode())
                    .updatedBy(UserContext.getUsername())
                    .build();
            knowledgeDocumentMapper.updateById(updateDocumentDO);

        }) ;

        return chunks.size() ;
    }

    private ChunkProcessResult runChunkProcess(KnowledgeDocumentDO knowledgeDocumentDO) {
        ChunkingMode chunkingMode = ChunkingMode.fromValue(knowledgeDocumentDO.getChunkStrategy());
        KnowledgeBaseDO kbDo = knowledgeBaseMapper.selectById(knowledgeDocumentDO.getKbId());
        String embeddingModel = kbDo.getEmbeddingModel();
        ChunkingOptions config = buildChunkingOptions(chunkingMode, knowledgeDocumentDO);

        long extractStart = System.currentTimeMillis() ;
        try (InputStream is = fileStorageService.openStream(knowledgeDocumentDO.getFileUrl())) {
            // 解析文本
            String text = parserSelector.select(ParserType.TIKA.getType()).extractText(is,knowledgeDocumentDO.getDocName()) ;
            long extractDuration = extractStart - System.currentTimeMillis();

            //文本分块
            ChunkingStrategy chunkingStrategy = ChunkingStrategyFactory.requireStrategy(chunkingMode);
            long chunkStart = System.currentTimeMillis();
            List<VectorChunk> chunk = chunkingStrategy.chunk(text, config);
            long chunkDuration = System.currentTimeMillis() - chunkStart;

            // chunk向量化
            long embedStart = System.currentTimeMillis();
            chunkEmbeddingService.embed(chunk,embeddingModel);
            long embedDuration = embedStart - System.currentTimeMillis();

            return new ChunkProcessResult(chunk,extractDuration,chunkDuration,embedDuration) ;


        } catch (Exception e) {
            throw new RuntimeException("文档内容提取或分块失败", e);
        }

    }

    private ChunkingOptions buildChunkingOptions(ChunkingMode mode, KnowledgeDocumentDO documentDO) {
        Map<String, Object> config = parseChunkConfig(documentDO.getChunkConfig());
        return mode.createOptions(config);
    }

    private Map<String, Object> parseChunkConfig(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.warn("分块参数解析失败: {}", json, e);
            return Map.of();
        }
    }

    private record ChunkProcessResult(List<VectorChunk> chunks, long extractDuration, long chunkDuration,
                                      long embedDuration) {
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
