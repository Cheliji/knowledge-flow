package com.cheliji.ai.knowledgeflow.framework.convention;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {

    /**
     * 完整消息列表
     */
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * 采样温度参数，取值通常为 0 ~ 2
     */
    private Double temperature ;

    /**
     * nucleus sampling (Top-P) 参数
     */
    private Double topP ;

    /**
     * Top-K 采样参数
     */
    private Integer topK ;

    /**
     * 限制模型本次回答最多生成的 token 数量
     */
    private Integer maxTokens ;


    /**
     * 可选：是否启用 [思考模式] 开关
     */
    private Boolean thinking ;


    /**
     * 可选：是否启用工具调用（Tool Calling / Function Calling）
     */
    private Boolean enableTools ;


}
