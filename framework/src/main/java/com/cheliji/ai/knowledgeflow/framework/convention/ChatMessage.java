package com.cheliji.ai.knowledgeflow.framework.convention;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话消息实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /**
     * 消息角色类型
     */
    public enum Role {

        /**
         * 系统角色，一般用于设定对话规则、身份设定、风格约束
         */
        SYSTEM ,

        /**
         * 用户角色，表示真实用户的提问或输入内容
         */
        USER ,


        /**
         * 助手机器人角色，表示大模型返回的回复内容
         */
        ASSISTANT

        /**
         * 根据字符串值匹配对应的角色枚举
         */
        public static Role fromString(String value) {
            for (Role role : Role.values()) {
                if (role.name().equalsIgnoreCase(value)) {
                    return role;
                }
            }
            throw new IllegalArgumentException("无效的角色类型: " + value);
        }
    }

    /**
     * 当前消息角色
     */
    private Role role ;


    /**
     * 消息的具体文本内容
     */
    private String content ;

    /**
     * 创建一条系统消息
     */
    public static ChatMessage system(String content) {
        return new ChatMessage(Role.SYSTEM, content);
    }


    /**
     * 创建一条用户消息
     */
    public static ChatMessage user(String content) {
        return new ChatMessage(Role.USER, content);
    }


    /**
     * 创建一条助手消息
     */
    public static ChatMessage assistant(String content) {
        return new ChatMessage(Role.ASSISTANT, content);
    }



}
