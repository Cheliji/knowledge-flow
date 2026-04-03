package com.cheliji.ai.knowledgeflow.knowledge.controller.request;

import lombok.Data;

@Data
public class KnowledgeBaseUpdateRequest {

    /**
     * 修改后的知识库名称
     */
    private String name ;

    /**
     * 如果知识库没有分块可以修改嵌入模型
     */
    private String embeddingModel ;

}
