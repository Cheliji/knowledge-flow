package com.cheliji.ai.knowledgeflow.infra.embedding;


import java.util.List;

/**
 * 向量化服务接口
 */
public interface EmbeddingService {

    /**
     * 对单个文本进行向量化
     */
    List<Float> embed(String text) ;

    /**
     * 指定模型对单个文本进行向量化
     */
    List<Float> embed(String text,String modelId) ;

    /**
     * 对多个文本进行批量向量化
     */
    List<List<Float>> embedBatch(List<String> texts) ;

    /**
     * 指定模型对多个文本进行批量向量化
     */
    List<List<Float>> embedBatch(List<String> texts,String modelId) ;

    /**
     * 返回向量维度
     */
    default int dimension() {
        return 0 ;
    }


}
