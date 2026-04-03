package com.cheliji.ai.knowledgeflow.knowledge.controller;

import com.cheliji.ai.knowledgeflow.knowledge.service.KnowledgeDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KnowledgeDocumentController {

    private final KnowledgeDocumentService knowledgeDocumentService ;

}
