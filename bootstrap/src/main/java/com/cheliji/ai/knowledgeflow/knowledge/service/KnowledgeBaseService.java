package com.cheliji.ai.knowledgeflow.knowledge.service;

import com.cheliji.ai.knowledgeflow.knowledge.controller.request.KnowledgeBaseCreateRequest;
import com.cheliji.ai.knowledgeflow.knowledge.controller.request.KnowledgeBaseUpdateRequest;

public interface KnowledgeBaseService {

    /**
     * 创建知识库
     * @return 返回知识库 id
     */
    String createKnowledgeBase(KnowledgeBaseCreateRequest requestParam);

    void update(String kdId, KnowledgeBaseUpdateRequest requestParam);
}
