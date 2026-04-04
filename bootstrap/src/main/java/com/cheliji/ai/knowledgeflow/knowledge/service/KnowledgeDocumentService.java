package com.cheliji.ai.knowledgeflow.knowledge.service;

import com.cheliji.ai.knowledgeflow.knowledge.controller.request.KnowledgeDocumentUploadRequest;
import com.cheliji.ai.knowledgeflow.knowledge.controller.vo.KnowledgeDocumentVO;
import org.springframework.web.multipart.MultipartFile;

public interface KnowledgeDocumentService {


    KnowledgeDocumentVO upload(String kbId, MultipartFile file, KnowledgeDocumentUploadRequest requestParam);
}
