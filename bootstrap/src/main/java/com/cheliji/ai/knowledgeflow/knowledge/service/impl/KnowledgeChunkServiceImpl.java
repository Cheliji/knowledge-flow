package com.cheliji.ai.knowledgeflow.knowledge.service.impl;

import com.cheliji.ai.knowledgeflow.knowledge.controller.request.KnowledgeChunkCreateRequest;
import com.cheliji.ai.knowledgeflow.knowledge.dao.mapper.KnowledgeChunkMapper;
import com.cheliji.ai.knowledgeflow.knowledge.service.KnowledgeChunkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeChunkServiceImpl implements KnowledgeChunkService {

    private final KnowledgeChunkMapper knowledgeChunkMapper;

    @Override
    public void deleteByDocId(String docId) {

    }

    @Override
    public void batchCreate(String docId, List<KnowledgeChunkCreateRequest> chunks) {

    }
}
