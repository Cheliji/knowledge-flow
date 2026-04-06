package com.cheliji.ai.knowledgeflow.rag.config;


import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.alibaba.ttl.threadpool.TtlExecutors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池执行器配置类
 */
@Configuration
public class ThreadPoolExecutorConfig {

    /**
     * CPU 核心数，用于动态计算线程池大小
     */
    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();


    /**
     * 文档分块处理线程池
     */
    @Bean
    public Executor chunkThreadPoolExecutor() {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                Math.max(2, CPU_COUNT),
                Math.max(4, CPU_COUNT >> 1),
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                ThreadFactoryBuilder.create()
                        .setNamePrefix("knowledgeflow_chunk_executor_")
                        .build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        return TtlExecutors.getTtlExecutor(executor) ;

    }

}
