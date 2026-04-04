package com.cheliji.ai.knowledgeflow.infra.embedding;


import com.cheliji.ai.knowledgeflow.infra.model.ModelTarget;

import java.util.List;

/**
 * 文本嵌入客户端接口
 */
public interface EmbeddingClient {

    /**
     * 获取嵌入式服务提供商名称
     */
    String provider() ;


    /**
     * 将单个文本转换为嵌入向量
     */
    List<Float> embed(String text, ModelTarget target) ;


    /**
     * 批量将多个文本转换嵌入式向量
     */
    List<List<Float>> embedBatch(List<String> texts, ModelTarget target) ;

}
