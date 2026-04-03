package com.cheliji.ai.knowledgeflow.knowledge.controller;


import com.cheliji.ai.knowledgeflow.framework.convention.Result;
import com.cheliji.ai.knowledgeflow.framework.web.Results;
import com.cheliji.ai.knowledgeflow.knowledge.controller.request.KnowledgeBaseCreateRequest;
import com.cheliji.ai.knowledgeflow.knowledge.controller.request.KnowledgeBaseUpdateRequest;
import com.cheliji.ai.knowledgeflow.knowledge.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;


    /**
     * 创建知识库
     * @return 知识库 ID
     */
    @PostMapping("/knowledge-base")
    public Result<String> createKnowledgeBase(@RequestBody KnowledgeBaseCreateRequest requestParam) {

        return Results.success(knowledgeBaseService.createKnowledgeBase(requestParam)) ;

    }

    /**
     * 重命名知识库
     */
    @PutMapping("/knowledge-base/{kb-id}")
    public Result<Void> updateKnowledgeBaseNameById(@PathVariable("kb-id")String kbId,
                                                    @RequestBody KnowledgeBaseUpdateRequest requestParam) {
        knowledgeBaseService.update(kbId,requestParam) ;
        return Results.success();
    }

}
