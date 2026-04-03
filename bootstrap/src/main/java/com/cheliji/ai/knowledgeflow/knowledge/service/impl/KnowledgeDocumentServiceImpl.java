package com.cheliji.ai.knowledgeflow.knowledge.service.impl;

import com.cheliji.ai.knowledgeflow.knowledge.dao.mapper.KnowledgeDocumentMapper;
import com.cheliji.ai.knowledgeflow.knowledge.service.KnowledgeDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeDocumentServiceImpl implements KnowledgeDocumentService {

    private final KnowledgeDocumentMapper knowledgeDocumentMapper;

}
