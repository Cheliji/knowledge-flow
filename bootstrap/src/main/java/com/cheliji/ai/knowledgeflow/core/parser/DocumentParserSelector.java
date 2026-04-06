package com.cheliji.ai.knowledgeflow.core.parser;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DocumentParserSelector {

    private final List<DocumentParser> strategies;
    private final Map<String, DocumentParser> strategyMap;

    public DocumentParserSelector(List<DocumentParser> parsers) {
        this.strategies = parsers;
        this.strategyMap = parsers.stream()
                .collect(Collectors.toMap(
                        DocumentParser::getParserType,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    /**
     * 根据解析器类型选择解析策略
     *
     * @param parserType 解析器类型（如 {@link ParserType#TIKA}, {@link ParserType#MARKDOWN}）
     * @return 解析器实例，如果不存在则返回 null
     */
    public DocumentParser select(String parserType) {
        return strategyMap.get(parserType);
    }

    /**
     * 根据 MIME 类型自动选择合适的解析策略
     * <p>
     * 遍历所有可用的解析器，返回第一个支持该 MIME 类型的解析器。
     * 如果没有找到匹配的解析器，则返回默认的 Tika 解析器。
     * </p>
     *
     * @param mimeType MIME 类型（如 "application/pdf", "text/markdown"）
     * @return 支持该 MIME 类型的解析器，如果没有则返回默认的 Tika 解析器
     */
    public DocumentParser selectByMimeType(String mimeType) {
        return strategies.stream()
                .filter(parser -> parser.supports(mimeType))
                .findFirst()
                .orElseGet(() -> select(ParserType.TIKA.getType()));
    }

    /**
     * 获取所有可用的解析策略
     *
     * @return 解析器列表
     */
    public List<DocumentParser> getAllStrategies() {
        return List.copyOf(strategies);
    }

    /**
     * 获取所有解析器类型
     *
     * @return 解析器类型列表
     */
    public List<String> getAvailableTypes() {
        return strategies.stream()
                .map(DocumentParser::getParserType)
                .toList();
    }
}

