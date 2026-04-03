package com.cheliji.ai.knowledgeflow.knowledge.service.impl;

import com.cheliji.ai.knowledgeflow.knowledge.dao.mapper.KnowledgeChunkMapper;
import com.cheliji.ai.knowledgeflow.knowledge.service.KnowledgeChunkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeChunkServiceImpl implements KnowledgeChunkService {

    private final KnowledgeChunkMapper knowledgeChunkMapper;

}
