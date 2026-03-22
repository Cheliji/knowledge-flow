package com.cheliji.ai.knowledgeflow.framework.exception;


import com.cheliji.ai.knowledgeflow.framework.errorcode.BaseErrorCode;
import com.cheliji.ai.knowledgeflow.framework.errorcode.IErrorCode;

/**
 * 客户端异常
 */
public class ClientException extends AbstractException {

    public ClientException(IErrorCode errorCode) { this(null,null,errorCode); }

    public ClientException(String message){ this(message,null, BaseErrorCode.CLIENT_ERROR); }

    public ClientException(String message,IErrorCode errorCode){ this(message,null, errorCode); }

    public ClientException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }

    @Override
    public String toString() {
        return "ClientException{" +
                "errorMessage='" + errorMessage + '\'' +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}
