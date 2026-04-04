package com.cheliji.ai.knowledgeflow.infra.chat;


import lombok.Getter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 首保等待器 - 用于等待第一个数据包到达的同步工具
 */
public class FirstPacketAwaiter {

    private final CountDownLatch latch = new CountDownLatch(1);
    private final AtomicBoolean hasContent = new AtomicBoolean(false);
    private final AtomicBoolean eventFired = new AtomicBoolean(false);
    private final AtomicReference<Throwable> error = new AtomicReference<>();

    /**
     * 标记收到内容
     */
    public void markContent() {
        hasContent.set(true);
        fireEventONce() ;
    }

    /**
     * 标记完成
     */
    public void markComplete() {
        fireEventONce();
    }

    /**
     * 标记错误
     */
    public void markError(Throwable t) {
        error.set(t);
        fireEventONce();
    }

    /**
     * 确保指只触发一次事件
     */
    private void fireEventONce() {
        if (eventFired.compareAndSet(false, true)) {
            latch.countDown();
        }
    }

    /**
     * 等待结果
     */
    public Result await(long timeout, TimeUnit unit) throws InterruptedException {

        boolean completed = latch.await(timeout, unit);

        if (error.get() != null) {
            return Result.error(error.get());
        }

        if (!completed) {
            return Result.timeout() ;
        }

        if (!hasContent.get()) {
            return Result.noContent();
        }

        return Result.success() ;

    }


    /**
     * 结构封装
     */
    @Getter
    public static class Result {

        public enum Type {
            SUCCESS ,
            ERROR ,
            TIMEOUT ,
            NO_CONTENT
        }

        private final Type type;
        private final Throwable error;

        public Result(Type type, Throwable error) {
            this.type = type;
            this.error = error;
        }

        public static Result success() {
            return new Result(Type.SUCCESS, null);
        }

        public static Result error(Throwable error) {
            return new Result(Type.ERROR, error);
        }

        public static Result timeout() {
            return new Result(Type.TIMEOUT, null);
        }

        public static Result noContent() {
            return new Result(Type.NO_CONTENT, null);
        }

        public boolean isSuccess() {
            return type == Type.SUCCESS;
        }


    }

}
