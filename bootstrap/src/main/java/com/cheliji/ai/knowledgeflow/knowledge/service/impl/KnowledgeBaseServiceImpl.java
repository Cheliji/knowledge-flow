package com.cheliji.ai.knowledgeflow.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cheliji.ai.knowledgeflow.framework.context.UserContext;
import com.cheliji.ai.knowledgeflow.framework.exception.ClientException;
import com.cheliji.ai.knowledgeflow.framework.exception.ServiceException;
import com.cheliji.ai.knowledgeflow.knowledge.controller.request.KnowledgeBaseCreateRequest;
import com.cheliji.ai.knowledgeflow.knowledge.controller.request.KnowledgeBaseUpdateRequest;
import com.cheliji.ai.knowledgeflow.knowledge.dao.entity.KnowledgeBaseDO;
import com.cheliji.ai.knowledgeflow.knowledge.dao.entity.KnowledgeChunkDO;
import com.cheliji.ai.knowledgeflow.knowledge.dao.mapper.KnowledgeBaseMapper;
import com.cheliji.ai.knowledgeflow.knowledge.dao.mapper.KnowledgeChunkMapper;
import com.cheliji.ai.knowledgeflow.knowledge.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeChunkMapper knowledgeChunkMapper;
    private final S3Client s3Client;


    @Override
    @Transactional
    public String createKnowledgeBase(KnowledgeBaseCreateRequest requestParam) {

        String name = requestParam.getName().replaceAll("\\s+", "");

        Long count = knowledgeBaseMapper.selectCount(
                new LambdaQueryWrapper<KnowledgeBaseDO>()
                .eq(KnowledgeBaseDO::getName, name)
                .eq(KnowledgeBaseDO::getCollectionName, requestParam.getCollectionName())
                .eq(KnowledgeBaseDO::getDeleted,0)
        );
        if (count > 0) {
            throw new ClientException("知识库已存在" + name) ;
        }

        KnowledgeBaseDO knowledgeBaseDO = KnowledgeBaseDO.builder()
                .name(name)
                .collectionName(requestParam.getCollectionName())
                .embeddingModel(requestParam.getEmbeddingModel())
                .createTime(new Date())
                .updateTime(new Date())
                .createdBy(UserContext.getUserId())
                .updatedBy(UserContext.getUserId())
                .deleted(0)
                .build();

        knowledgeBaseMapper.insert(knowledgeBaseDO);


        String bucketName = requestParam.getCollectionName() ;
        try {

            s3Client.createBucket(builder -> builder.bucket(bucketName));

            log.info("成功创建RestFS存储桶，Bucket名称: {}", bucketName);
        } catch (BucketAlreadyOwnedByYouException | BucketAlreadyExistsException e) {
            if (e instanceof BucketAlreadyOwnedByYouException) {
                log.error("RestFS存储桶已存在，Bucket名称: {}", bucketName, e);
            } else {
                log.error("RestFS存储桶已存在但由其他账户拥有，Bucket名称: {}", bucketName, e);
            }
            throw new ServiceException("存储桶名称已被占用：" + bucketName);
        }

        // TODO 创建向量数据库 collection


        return knowledgeBaseDO.getId() ;
    }

    @Override
    public void update(String kbId, KnowledgeBaseUpdateRequest requestParam) {
        KnowledgeBaseDO knowledgeBaseDO = knowledgeBaseMapper.selectById(kbId);

        if (knowledgeBaseDO == null || knowledgeBaseDO.getDeleted() != null && knowledgeBaseDO.getDeleted() == 1) {
            throw  new ClientException("知识库不存在") ;
        }

        if (!StringUtils.hasText(requestParam.getName())) {
            throw new ClientException("知识库名称不能为空");
        }

        String name = requestParam.getName().replaceAll("\\s+", "");

        Long count = knowledgeBaseMapper.selectCount(
                new LambdaQueryWrapper<KnowledgeBaseDO>()
                        .eq(KnowledgeBaseDO::getName, name)
                        .eq(KnowledgeBaseDO::getCollectionName, knowledgeBaseDO.getCollectionName())
                        .eq(KnowledgeBaseDO::getDeleted,0)
        );

        if (count > 0) {
            throw new ServiceException("知识库已存在" + name) ;
        }


        //判断是否需要修改嵌入模型，再判断是否知识库是否以及分块了，如果分了就不能修改

        if (requestParam.getEmbeddingModel() != null && requestParam.getEmbeddingModel().length() > 0) {
            Long chunkCount = knowledgeChunkMapper.selectCount(
                    new LambdaQueryWrapper<KnowledgeChunkDO>()
                            .eq(KnowledgeChunkDO::getKbId, knowledgeBaseDO.getId())
                            .eq(KnowledgeChunkDO::getEnable, 1)
                            .eq(KnowledgeChunkDO::getDeleted, 0)
            );

            if (chunkCount > 0) {
                throw new ClientException("知识库已有文档进行了向量化，不能再改变向量化模型") ;
            }
            knowledgeBaseDO.setEmbeddingModel(requestParam.getEmbeddingModel());
        }

        knowledgeBaseDO.setName(name) ;
        knowledgeBaseDO.setUpdatedBy(UserContext.getUserId());

        knowledgeBaseMapper.updateById(knowledgeBaseDO);


        log.info("成功重命名知识库, kbId={}, newName={}", kbId, requestParam.getName());
    }
}
