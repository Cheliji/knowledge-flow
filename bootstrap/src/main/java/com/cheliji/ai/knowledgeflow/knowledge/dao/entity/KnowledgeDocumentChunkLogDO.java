package com.cheliji.ai.knowledgeflow.knowledge.dao.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_knowledge_document_chunk_log")
public class KnowledgeDocumentChunkLogDO {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id ;

    /**
     * 文档 id
     */
    private String docId ;

    /**
     * 执行状态
     */
    private String status ;

    /**
     * 处理模式
     */
    private String processMode ;

    /**
     * 分块策略
     */
    private String chunkStrategy ;

    /**
     * Pipeline Id
     */
    private String pipelineId ;

    /**
     * 文本提取时间
     */
    private Long extractDuration ;

    /**
     * 分块耗时
     */
    private Long chunkDuration ;

    /**
     * 向量化时间
     */
    private Long embeddingDuration ;

    /**
     * chunk 落库
     */
    private Long persistDuration ;

    /**
     * 总耗时
     */
    private Long totalDuration ;

    /**
     * 分块数量
     */
    private Integer chunkCount ;

    /**
     * 错误信息
     */
    private String errorMessage ;

    /**
     * 开始时间
     */
    private Date startTime ;

    /**
     * 结束时间
     */
    private Date endTime ;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime ;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime ;

}
