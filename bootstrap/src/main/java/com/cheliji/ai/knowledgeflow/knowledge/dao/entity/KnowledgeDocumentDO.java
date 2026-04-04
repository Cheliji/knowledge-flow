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
@TableName("t_knowledge_document")
public class KnowledgeDocumentDO {

    /**
     * 知识库文档 id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 知识库 ID
     */
    private String kbId ;

    /**
     * 文档名称
     */
    private String docName ;

    /**
     * 是否启用  1：启用  0：禁用
     */
    private Integer enabled ;

    /**
     * 分块后的分块数量
     */
    private Integer chunkCount ;

    /**
     * 文件连接，用于存放文件在对象存储中的连接
     */
    private String fileUrl ;

    /**
     * 文件类型
     */
    private String fileType ;

    /**
     * 文件大小
     */
    private Long fileSize ;

    /**
     * 处理模式
     */
    private String processMode ;

    /**
     * 状态
     */
    private String status ;

    /**
     * 来源类型：file/url
     */
    private String sourceType ;

    /**
     * 文件位置：用于存储文件
     */
    private String sourceLocation ;

    /**
     * 定时更新是否起用，用于定时更新共享文件（URL 文件）
     * 1：启用，0：禁用
     */
    private Integer scheduleEnabled ;

    /**
     * 定时拉取cron表达式
     */
    private String scheduleCron ;

    /**
     * 分块策略
     */
    private String chunkStrategy ;

    /**
     * 分块参数JSON
     */
    // TODO 自定义 JSON 解析器
    private String chunkConfig;

    /**
     * 数据通道 pipeline_id
     */
    private String pipelineId ;

    /**
     * 创建人
     */
    private String createdBy ;


    /**
     * 修改人
     */
    private String updatedBy ;

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

    /**
     * 是否删除  0:正常  1：删除
     */
    @TableLogic
    private Integer deleted ;


}
