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
public class KnowledgeBaseDO {

    /**
     * 知识库 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

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

    /**
     * 创建人
     */
    private String createBy ;

    /**
     * 修改人
     */
    private String updateBy ;


    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 是否删除：0-正常，1-删除
     */
    @TableLogic
    private Integer deleted;

}
