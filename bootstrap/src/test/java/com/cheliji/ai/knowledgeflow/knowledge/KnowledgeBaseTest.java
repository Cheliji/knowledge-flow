package com.cheliji.ai.knowledgeflow.knowledge;

import com.cheliji.ai.knowledgeflow.framework.context.LoginUser;
import com.cheliji.ai.knowledgeflow.framework.context.UserContext;
import com.cheliji.ai.knowledgeflow.knowledge.controller.request.KnowledgeBaseCreateRequest;
import com.cheliji.ai.knowledgeflow.knowledge.controller.request.KnowledgeBaseUpdateRequest;
import com.cheliji.ai.knowledgeflow.knowledge.service.KnowledgeBaseService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class KnowledgeBaseTest {

    @Autowired
    private KnowledgeBaseService service;


    @Test
    public void createKnowledgeBaseTest() {

        KnowledgeBaseCreateRequest request = new KnowledgeBaseCreateRequest() ;
        request.setName("test");
        request.setCollectionName("test");
        request.setEmbeddingModel("qwen-emb-8b");

        UserContext.set(LoginUser.builder()
                .avatar("2001523723396308993")
                .role("admin")
                .userId("001")
                .username("admin")
                .build());

        String knowledgeBase = service.createKnowledgeBase(request);


        System.out.println(knowledgeBase);

    }

    @Test
    public void renewKnowledgeBaseNameTest() {
        KnowledgeBaseUpdateRequest request = new KnowledgeBaseUpdateRequest() ;
        request.setName("test01");

        UserContext.set(LoginUser.builder()
                .avatar("2001523723396308993")
                .role("admin")
                .userId("001")
                .username("admin")
                .build());

        service.update("2039976197217792002",request);

    }



}
