package com.cheliji.ai.knowledgeflow;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = {
        "com.cheliji.ai.knowledgeflow.knowledge.dao.mapper",
})
public class KnowledgeFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeFlowApplication.class, args);
    }

}
