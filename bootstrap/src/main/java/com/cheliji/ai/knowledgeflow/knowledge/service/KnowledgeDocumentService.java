package com.cheliji.ai.knowledgeflow.knowledge.service;

import com.cheliji.ai.knowledgeflow.knowledge.controller.request.KnowledgeDocumentUploadRequest;
import com.cheliji.ai.knowledgeflow.knowledge.controller.vo.KnowledgeDocumentVO;
import org.springframework.web.multipart.MultipartFile;

public interface KnowledgeDocumentService {


    /**
     * 文档上传
     * @param kbId 知识库 id
     * @param file 本地上传的文件
     * @param requestParam 请求参数
     */
    KnowledgeDocumentVO upload(String kbId, MultipartFile file, KnowledgeDocumentUploadRequest requestParam);

    /**
     * 文档分块内容
     * @param docId 文档 id ;
     */
    void startChunk(String docId);
}
