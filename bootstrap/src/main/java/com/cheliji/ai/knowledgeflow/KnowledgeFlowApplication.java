package com.cheliji.ai.knowledgeflow;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KnowledgeFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeFlowApplication.class, args);
    }

}
