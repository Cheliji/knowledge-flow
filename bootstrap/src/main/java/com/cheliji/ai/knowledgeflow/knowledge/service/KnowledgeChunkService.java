package com.cheliji.ai.knowledgeflow.knowledge.service;

import com.cheliji.ai.knowledgeflow.knowledge.controller.request.KnowledgeChunkCreateRequest;

import java.util.List;

public interface KnowledgeChunkService {


    void deleteByDocId(String docId);

    void batchCreate(String docId, List<KnowledgeChunkCreateRequest> chunks);
}
