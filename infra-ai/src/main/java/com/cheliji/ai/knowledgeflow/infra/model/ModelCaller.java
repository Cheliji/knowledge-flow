package com.cheliji.ai.knowledgeflow.infra.model;


/**
 * 模型调用器函数式接口
 */
@FunctionalInterface
public interface ModelCaller<C,T> {

    /**
     * 执行模型调用
     */
    T call(C client,ModelTarget target) throws Exception ;
}
