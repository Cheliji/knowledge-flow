package com.cheliji.ai.knowledgeflow.knowledge.controller;

import com.cheliji.ai.knowledgeflow.framework.convention.Result;
import com.cheliji.ai.knowledgeflow.framework.web.Results;
import com.cheliji.ai.knowledgeflow.knowledge.controller.request.KnowledgeDocumentUploadRequest;
import com.cheliji.ai.knowledgeflow.knowledge.controller.vo.KnowledgeDocumentVO;
import com.cheliji.ai.knowledgeflow.knowledge.service.KnowledgeDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库文档管理
 */
@RestController
@RequiredArgsConstructor
public class KnowledgeDocumentController {

    private final KnowledgeDocumentService knowledgeDocumentService ;

    @PostMapping(value="/knowledge-base/{kb-id}/docs/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<KnowledgeDocumentVO> upload(@PathVariable("kb-id") String kbId,
                                              @RequestPart(value = "file",required = false) MultipartFile file,
                                              @ModelAttribute KnowledgeDocumentUploadRequest requestParam) {
        return Results.success(knowledgeDocumentService.upload(kbId,file,requestParam)) ;
    }

}
