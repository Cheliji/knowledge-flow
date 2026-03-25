package com.cheliji.ai.knowledgeflow.infra.chat;


import com.cheliji.ai.knowledgeflow.framework.convention.ChatMessage;
import com.cheliji.ai.knowledgeflow.framework.convention.ChatRequest;

import java.util.List;

/**
 *
 * 通用大语言模型（LLM）访问接口
 */
public interface LLMService {

    /**
     * 同步调用
     */
    default String chat(String prompt) {
        ChatRequest req = ChatRequest.builder()
                .messages(List.of(ChatMessage.user(prompt)))
                .build() ;
        return chat(req) ;
    }

    /**
     * 同步调用
     */
    String chat(ChatRequest req) ;


    /**
     * 流式调用
     */
    default StreamCancellationHandle streamChat(String prompt,StreamCallback callback) {
        ChatRequest req = ChatRequest.builder()
                .messages(List.of(ChatMessage.user(prompt)))
                .build() ;
        return streamChat(req,callback);
    }


    /**
     * 流式调用
     */
    StreamCancellationHandle streamChat(ChatRequest req,StreamCallback callback) ;



}
