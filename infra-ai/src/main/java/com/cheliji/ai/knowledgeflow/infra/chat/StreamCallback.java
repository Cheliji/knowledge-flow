package com.cheliji.ai.knowledgeflow.infra.chat;


/**
 * 流式响应调用接口
 */
public interface StreamCallback {

    /**
     * 接收一次增量内容
     */
    void onContent(String content);

    /**
     * 接受思考过程增量
     */
    default void onThinking(String content ){}


    /**
     * 整个推理流程结束
     */
    void onComplete() ;

    /**
     * 流式推送过程中出现异常
     */
    void onError(Throwable error);

}
