package com.cheliji.ai.knowledgeflow.core.parser;


import com.cheliji.ai.knowledgeflow.framework.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Tika 文档解析器
 */
@Slf4j
@Component
@Primary
public class TikaDocumentParser implements DocumentParser{

    private static final Tika TIKA = new Tika();

    static {
        PDFParserConfig pdfConfig = new PDFParserConfig();
        pdfConfig.setExtractInlineImages(false);
        pdfConfig.setExtractUniqueInlineImagesOnly(true);
    }


    @Override
    public String getParserType() {
        return ParserType.TIKA.getType();
    }

    @Override
    public ParseResult parse(byte[] content, String mimeType, Map<String, Object> options) {
        if (content == null || content.length ==0) {
            return ParseResult.ofText("") ;
        }

        try (InputStream stream = new ByteArrayInputStream(content)) {
            String text = TIKA.parseToString(stream) ;
            String cleaned = TextCleanupUtil.cleanup(text) ;
            return ParseResult.ofText(cleaned) ;
        } catch (Exception e) {
            log.error("Tika 解析失败，MIME 类型：{}",mimeType,e) ;
            throw new ServiceException("文档解析失败：" + e.getMessage());
        }

    }



    @Override
    public String extractText(InputStream stream, String fileName) {
        try {
            String text = TIKA.parseToString(stream) ;
            String cleaned = TextCleanupUtil.cleanup(text) ;
            return cleaned ;
        } catch (Exception e) {
            log.error("从文件中提取文本内容失败：{}",fileName,e) ;
            throw new ServiceException("文档解析失败：" + e.getMessage());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        // Tika 支持大部分常见文档格式
        return mimeType != null && !mimeType.startsWith("text/markdown");
    }
}
