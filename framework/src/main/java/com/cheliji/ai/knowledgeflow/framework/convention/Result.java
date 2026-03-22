package com.cheliji.ai.knowledgeflow.framework.convention;


import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 全局统一返回结果对象
 */
@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 5679018624309023727L;

    /**
     * 成功状态码
     */
    public static final String SUCCESS_CODE = "0" ;


    /**
     * 状态码
     */
    private String code ;


    /**
     * 响应消息
     */
    private String message ;


    /**
     * 响应数据
     */
    private T data ;


    /**
     * 请求追踪 ID
     */
    private String requestId ;


    /**
     * 判断请求是否成功
     */
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(code);
    }



}
