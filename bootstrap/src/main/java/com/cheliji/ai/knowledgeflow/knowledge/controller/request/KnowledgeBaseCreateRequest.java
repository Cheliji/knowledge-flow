package com.cheliji.ai.knowledgeflow.knowledge.controller.request;

import lombok.Data;

@Data
public class KnowledgeBaseCreateRequest {

    /**
     * 知识库名称
     */
    private String name ;

    /**
     * 嵌入模型标识
     */
    private String embeddingModel ;

    /**
     * 向量数据 Collection
     */
    private String collectionName ;

}
