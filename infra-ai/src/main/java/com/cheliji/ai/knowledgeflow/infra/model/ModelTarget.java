package com.cheliji.ai.knowledgeflow.infra.model;


import com.cheliji.ai.knowledgeflow.infra.config.AIModelProperties;

/**
 * 模型目标配置记录
 */
public record ModelTarget(
        String id,
        AIModelProperties.ModelCandidate candidate,
        AIModelProperties.ProviderConfig provider
) {
}