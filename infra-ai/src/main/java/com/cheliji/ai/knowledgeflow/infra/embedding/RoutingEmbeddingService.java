package com.cheliji.ai.knowledgeflow.infra.embedding;

import com.cheliji.ai.knowledgeflow.framework.errorcode.BaseErrorCode;
import com.cheliji.ai.knowledgeflow.framework.exception.RemoteException;
import com.cheliji.ai.knowledgeflow.infra.enums.ModelCapability;
import com.cheliji.ai.knowledgeflow.infra.model.ModelHealthStore;
import com.cheliji.ai.knowledgeflow.infra.model.ModelRoutingExecutor;
import com.cheliji.ai.knowledgeflow.infra.model.ModelSelector;
import com.cheliji.ai.knowledgeflow.infra.model.ModelTarget;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Primary
public class RoutingEmbeddingService implements EmbeddingService{

    private final ModelSelector selector;
    private final ModelHealthStore healthStore;
    private final ModelRoutingExecutor executor;
    private final Map<String, EmbeddingClient> clientsByProvider;



    public RoutingEmbeddingService(
            ModelSelector selector,
            ModelHealthStore healthStore,
            ModelRoutingExecutor executor,
            List<EmbeddingClient> clients) {
        this.selector = selector;
        this.healthStore = healthStore;
        this.executor = executor;
        this.clientsByProvider = clients.stream()
                .collect(Collectors.toMap(EmbeddingClient::provider, Function.identity()));
    }

    @Override
    public List<Float> embed(String text) {
        return executor.executeWithFallback(
                ModelCapability.EMBEDDING,
                selector.selectEmbeddingCandidates(),
                target -> clientsByProvider.get(target.candidate().getProvider()),
                (client, target) -> client.embed(text, target)
        );
    }

    @Override
    public List<Float> embed(String text, String modelId) {
        ModelTarget target = resolveTarget(modelId);
        EmbeddingClient client = resolveClient(target);
        if (!healthStore.allowCall(target.id())) {
            throw new RemoteException("Embedding 模型暂不可用: " + target.id());
        }
        try {
            List<Float> vector = client.embed(text, target);
            healthStore.markSuccess(target.id());
            return vector;
        } catch (Exception e) {
            healthStore.markFail(target.id());
            throw new RemoteException("Embedding 模型调用失败: " + target.id(), e, BaseErrorCode.REMOTE_ERROR);
        }
    }

    @Override
    public List<List<Float>> embedBatch(List<String> texts) {
        return executor.executeWithFallback(
                ModelCapability.EMBEDDING,
                selector.selectEmbeddingCandidates(),
                target -> clientsByProvider.get(target.candidate().getProvider()),
                (client, target) -> client.embedBatch(texts, target)
        );
    }

    @Override
    public List<List<Float>> embedBatch(List<String> texts, String modelId) {
        ModelTarget target = resolveTarget(modelId);
        EmbeddingClient client = resolveClient(target);
        if (!healthStore.allowCall(target.id())) {
            throw new RemoteException("Embedding 模型暂不可用: " + target.id());
        }
        try {
            List<List<Float>> vectors = client.embedBatch(texts, target);
            healthStore.markSuccess(target.id());
            return vectors;
        } catch (Exception e) {
            healthStore.markFail(target.id());
            throw new RemoteException("Embedding 模型调用失败: " + target.id(), e, BaseErrorCode.REMOTE_ERROR);
        }
    }

    @Override
    public int dimension() {
        ModelTarget target = selector.selectDefaultEmbedding();
        if (target == null || target.candidate().getDimension() == null) {
            return 0;
        }
        return target.candidate().getDimension();
    }

    private ModelTarget resolveTarget(String modelId) {
        if (!StringUtils.hasText(modelId)) {
            throw new RemoteException("Embedding 模型ID不能为空");
        }
        return selector.selectEmbeddingCandidates().stream()
                .filter(target -> modelId.equals(target.id()))
                .findFirst()
                .orElseThrow(() -> new RemoteException("Embedding 模型不可用: " + modelId));
    }

    private EmbeddingClient resolveClient(ModelTarget target) {
        EmbeddingClient client = clientsByProvider.get(target.candidate().getProvider());
        if (client == null) {
            throw new RemoteException("Embedding 模型客户端不存在: " + target.candidate().getProvider());
        }
        return client;
    }
}
