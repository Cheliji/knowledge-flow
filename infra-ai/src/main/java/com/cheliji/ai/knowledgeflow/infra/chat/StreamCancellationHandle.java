package com.cheliji.ai.knowledgeflow.infra.chat;


/**
 * 流式取消句柄
 */
public interface StreamCancellationHandle {

    /**
     * 取消当前流式推理任务
     */
    void cancel() ;

}
