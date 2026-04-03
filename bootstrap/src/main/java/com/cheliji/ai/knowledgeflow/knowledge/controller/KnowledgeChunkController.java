package com.cheliji.ai.knowledgeflow.knowledge.controller;

import com.cheliji.ai.knowledgeflow.knowledge.service.KnowledgeChunkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KnowledgeChunkController {

    private final KnowledgeChunkService knowledgeChunkService;

}
