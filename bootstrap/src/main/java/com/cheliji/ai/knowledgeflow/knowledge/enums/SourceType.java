package com.cheliji.ai.knowledgeflow.knowledge.enums;


import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SourceType {

    /**
     * 本地文件上传
     */
    FILE("file") ,

    /**
     * 远程 URL 获取
     */
    URL("url") ;


    /**
     * 来源类型值
     */
    private final String value ;

    /**
     * 根据值获取枚举
     */
    public static SourceType fromValue(String v) {
        if (v == null)
            return null ;

        String normalized = v.trim().toLowerCase() ;

        if ("file".equals(normalized) || "localfile".equals(normalized)) {
            return FILE ;
        }
        if ("url".equals(normalized)){
            return URL ;
        }

        return null ;
    }

    /**
     * 解析来源类型，空值或非法值抛出异常
     */
    public static SourceType normalize(String v) {

        if (StrUtil.isBlank(v)) {
            throw new IllegalArgumentException("来源类型不能为空");
        }

        SourceType result = fromValue(v);

        if (result == null) {
            throw new IllegalArgumentException("不支持的来源类型：" + v) ;
        }

        return result;

    }

}
