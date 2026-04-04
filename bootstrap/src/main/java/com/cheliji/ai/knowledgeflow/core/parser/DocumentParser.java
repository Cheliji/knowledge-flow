package com.cheliji.ai.knowledgeflow.core.parser;


import java.io.InputStream;
import java.util.Map;

/**
 * 文档解析统一接口
 */
public interface DocumentParser {

    /**
     * 获取解析器类型标识
     */
    String getParserType() ;


    /**
     * 解析文档内容（字节数组）
     */
    default ParseResult parse(byte[] content, String mimeType, Map<String,Object> options) {
        throw new UnsupportedOperationException("parse(byte[], String, Map) not implemented");
    }


    /**
     * 解析文档内容（输入流）
     */
    default String extractText(InputStream stream,String fileName){
        throw new UnsupportedOperationException("parse(InputStream, String) not implemented");
    }


    /**
     * 检查是否支持指定的 MIME 类型
     */
    default boolean supports(String mimeType) {
        return true ;
    }


}
