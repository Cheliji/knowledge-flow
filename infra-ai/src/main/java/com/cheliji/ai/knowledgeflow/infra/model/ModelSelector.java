package com.cheliji.ai.knowledgeflow.infra.model;

import cn.hutool.core.util.StrUtil;
import com.cheliji.ai.knowledgeflow.infra.config.AIModelProperties;
import com.cheliji.ai.knowledgeflow.infra.enums.ModelProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 模型选择器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ModelSelector {

    private final AIModelProperties properties ;
    private final ModelHealthStore healthStore ;

    /**
     * (Chat 型)模型选择，选择默认+候选（通过根据 deepThinking 选择思考模型）
     */
    public List<ModelTarget> selectChatCandidates(Boolean deepThinking) {
        AIModelProperties.ModelGroup group = properties.getChat();

        if (group == null) {
            return List.of();
        }

        String firstChoiceModelId = resolveFirstChoiceModel(group,deepThinking) ;

        return selectCandidates(group,firstChoiceModelId,deepThinking) ;

    }

    // （Embdding） 模型选择器
    public List<ModelTarget> selectEmbeddingCandidates() {
        return selectCandidates(properties.getEmbedding()) ;
    }

    // (Rerank) 模型选择器
    public List<ModelTarget> selectRerankCandidates() {
        return selectCandidates(properties.getRerank()) ;
    }

    public ModelTarget selectDefaultEmbedding() {
        List<ModelTarget> targets = selectEmbeddingCandidates();
        return targets.isEmpty() ? null : targets.get(0);
    }



    private List<ModelTarget> selectCandidates(AIModelProperties.ModelGroup group) {
        if (group == null) {
            return List.of();
        }
        return selectCandidates(group, group.getDefaultModel(), null);
    }

    private List<ModelTarget> selectCandidates(AIModelProperties.ModelGroup group, String firstChoiceModelId, Boolean deepThinking) {
        if (group == null || group.getConstants() == null)
            return List.of( ) ;

        List<AIModelProperties.ModelCandidate> orderedCandidates =
                prepareOrderedCandidates(group.getConstants(), firstChoiceModelId, deepThinking);

        return buildAvailableTargets(orderedCandidates) ;
    }

    private List<ModelTarget> buildAvailableTargets(List<AIModelProperties.ModelCandidate> orderedCandidates) {

        Map<String, AIModelProperties.ProviderConfig> providers = properties.getProviders();

        return orderedCandidates.stream()
                .map(candidate -> buildModelTarget(candidate, providers))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }

    private ModelTarget buildModelTarget(AIModelProperties.ModelCandidate candidate, Map<String, AIModelProperties.ProviderConfig> providers) {
        String modelId = resolveId(candidate);

        // 检查熔断状态
        if (healthStore.isOpen(modelId)) {
            return null;
        }

        // 验证 provider 配置
        AIModelProperties.ProviderConfig provider = providers.get(candidate.getProvider());
        if (provider == null && !ModelProvider.NOOP.matches(candidate.getProvider())) {
            log.warn("Provider配置缺失: provider={}, modelId={}",
                    candidate.getProvider(), modelId);
            return null;
        }

        return new ModelTarget(modelId, candidate, provider);
    }

    private String resolveId(AIModelProperties.ModelCandidate candidate) {
        if (candidate == null) {
            return null;
        }
        if (StrUtil.isNotBlank(candidate.getId())) {
            return candidate.getId();
        }
        return String.format("%s::%s",
                Objects.toString(candidate.getProvider(), "unknown"),
                Objects.toString(candidate.getModel(), "unknown"));
    }

    private List<AIModelProperties.ModelCandidate> prepareOrderedCandidates(List<AIModelProperties.ModelCandidate> constants, String firstChoiceModelId, Boolean deepThinking) {

        ArrayList<AIModelProperties.ModelCandidate> candidates = constants.stream()
                // 不为空，且可以使用
                .filter(c -> c != null && Boolean.TRUE.equals(c.getEnable()))
                // 过滤是否为思考模型以及支持思考
                .filter(c -> !Boolean.TRUE.equals(deepThinking) || Boolean.TRUE.equals(c.getSupportsThinking()))
                // 排序
                .sorted(Comparator
                        .comparing(AIModelProperties.ModelCandidate::getPriority,
                                Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(AIModelProperties.ModelCandidate::getId,
                                Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toCollection(ArrayList::new));

        if (Boolean.TRUE.equals(deepThinking) && constants.isEmpty()) {
            log.warn("深度思考模式没有可用候选模型");
            return constants;
        }

        // 将默认模型插入第一位
        promoteFirstChoiceModel(constants, firstChoiceModelId);

        return candidates;


    }

    private void promoteFirstChoiceModel(List<AIModelProperties.ModelCandidate> constants, String firstChoiceModelId) {

        if (StrUtil.isBlank(firstChoiceModelId)) {
            return ;
        }

        AIModelProperties.ModelCandidate firstChoice = findCandidate(constants, firstChoiceModelId);

        constants.remove(firstChoice);
        constants.add(0,firstChoice);

    }

    private AIModelProperties.ModelCandidate findCandidate(List<AIModelProperties.ModelCandidate> constants,
                                                           String firstChoiceModelId) {

        return constants.stream()
                .filter(c -> firstChoiceModelId.equals(c.getId()))
                .findFirst()
                .orElse(null);

    }

    private String resolveFirstChoiceModel(AIModelProperties.ModelGroup group, Boolean deepThinking) {

        if(Boolean.TRUE.equals(deepThinking)) {

            String deepModel = group.getDeepThinkModel() ;

            if(StrUtil.isNotBlank(deepModel))
                return deepModel;

        }

        return group.getDefaultModel() ;

    }

}
