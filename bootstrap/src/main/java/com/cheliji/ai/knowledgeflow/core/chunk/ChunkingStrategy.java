package com.cheliji.ai.knowledgeflow.core.chunk;


import java.util.List;

/**
 * 文本分块器核心接口
 * 定义统一的文本分块能力
 */
public interface ChunkingStrategy {

    /**
     * 获取分块器类型标识
     */
    ChunkingMode getType() ;

    /**
     * 对文本进行分块处理
     */
    List<VectorChunk> chunk(String text,ChunkingOptions config) ;

}
