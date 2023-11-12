package com.george.demo.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ChatMsg implements Serializable {

    private Type msgType;

    private String uid;

    private String msg;

    private String roomId;

    private String name;

    public enum Type {
        // 心跳
        HEARTBEAT,

        // 加入聊天室
        JOIN,

        // 离开聊天室
        LEAVE,

        // 聊天
        CHAT,

        PROTOCOL
    }
}
