package com.cheliji.ai.knowledgeflow.core.chunk;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;


/**
 * 分块结果对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VectorChunk {

    /**
     * 快的唯一标识符
     */
    private String chunkId ;

    /**
     * 块在文档中的序号索引，从 0 开始
     */
    private Integer index ;

    /**
     * 快的原始文本内容
     */
    private String content ;

    /**
     * 快的元数据信息
     */
    @Builder.Default
    private Map<String,Object> metadat = new HashMap<> () ;

    /**
     * 快的向量嵌入表示
     */
    @JsonIgnore
    private float[] embedding ;

}
