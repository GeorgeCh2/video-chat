package com.george.demo.chat;

import com.george.demo.model.ChatMsg;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.netty.util.internal.StringUtil;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatHandler extends SimpleChannelInboundHandler<String> {

    // room - uid map
    private static Map<String, Set<String>> roomMap = new HashMap<>();

    private static Map<String, Channel> channelMap = new ConcurrentHashMap<>();

    public static final AttributeKey<String> UID = AttributeKey.newInstance("uid");

    public static final AttributeKey<String> ROOM_ID = AttributeKey.newInstance("roomid");

    private Gson gson = new GsonBuilder().create();

    @Override
    protected void channelRead0(ChannelHandlerContext context, String msgStr) throws Exception {
        ChatMsg chatMsg = gson.fromJson(msgStr, ChatMsg.class);

        switch (chatMsg.getMsgType()) {
            case HEARTBEAT:
                break;

            case JOIN:
                // new user join the room
                channelMap.put(chatMsg.getUid(), context.channel());
                context.channel().attr(UID).set(chatMsg.getUid());
                context.channel().attr(ROOM_ID).set(chatMsg.getRoomId());

                Set<String> sameRoomUids = roomMap.get(chatMsg.getRoomId());
                if (sameRoomUids == null) {
                    sameRoomUids = new HashSet<>();
                    sameRoomUids.add(chatMsg.getUid());
                } else {
                    for (String uid : sameRoomUids) {
                        if (uid.equals(chatMsg.getUid())) {
                            continue;
                        }
                        Channel channel = channelMap.get(uid);
                        channel.writeAndFlush(gson.toJson(ChatMsg.builder()
                                .msgType(ChatMsg.Type.JOIN)
                                .uid(chatMsg.getUid())
                                .msg(chatMsg.getUid() + " join the room")
                                .roomId(chatMsg.getRoomId())
                                .name(chatMsg.getName())
                                .build()));
                    }

                    context.channel().writeAndFlush(gson.toJson(ChatMsg.builder()
                            .msgType(ChatMsg.Type.PROTOCOL)
                            .uid(chatMsg.getUid())
                            .msg("{\"type\": \"send_peer\"}")
                            .roomId(chatMsg.getRoomId())
                            .name(chatMsg.getName())
                            .build()));

                    sameRoomUids.add(chatMsg.getUid());
                }
                roomMap.put(chatMsg.getRoomId(), sameRoomUids);
                System.out.println("user: " + chatMsg.getUid() + " join the room: " + chatMsg.getRoomId());
                break;

            case LEAVE:
                // user leave the room
                Set<String> uids = roomMap.get(chatMsg.getRoomId());
                uids.remove(chatMsg.getUid());
                if (CollectionUtils.isEmpty(uids)) {
                    roomMap.remove(chatMsg.getRoomId());
                } else {
                    for (String uid : uids) {
                        if (uid.equals(chatMsg.getUid())) {
                            continue;
                        }
                        Channel channel = channelMap.get(uid);
                        channel.writeAndFlush(gson.toJson(ChatMsg.builder()
                                .msgType(ChatMsg.Type.LEAVE)
                                .uid(chatMsg.getUid())
                                .msg(chatMsg.getUid() + " leave the room")
                                .roomId(chatMsg.getRoomId())
                                .build()));
                    }
                    roomMap.put(chatMsg.getRoomId(), uids);
                }
                channelMap.remove(chatMsg.getUid());

                System.out.println("user: " + chatMsg.getUid() + " leave the room: " + chatMsg.getRoomId());
                break;

            case CHAT:
                // user chat
                Set<String> uidList = roomMap.get(chatMsg.getRoomId());
                for (String uid : uidList) {
                    if (uid.equals(chatMsg.getUid())) {
                        continue;
                    }

                    Channel channel = channelMap.get(uid);
                    channel.writeAndFlush(gson.toJson(ChatMsg.builder()
                            .msgType(ChatMsg.Type.CHAT)
                            .uid(chatMsg.getUid())
                            .msg(chatMsg.getMsg())
                            .roomId(chatMsg.getRoomId())
                            .build()));
                }
                System.out.println("user: " + chatMsg.getUid() + " chat in the room: " + chatMsg.getRoomId());
                break;

            case PROTOCOL:
                Set<String> msgUids = roomMap.get(chatMsg.getRoomId());
                for (String uid : msgUids) {
                    if (uid.equals(chatMsg.getUid())) {
                        continue;
                    }

                    Channel channel = channelMap.get(uid);
                    channel.writeAndFlush(msgStr);
                }
                break;

            default:
                System.out.println("unknown msg type: " + chatMsg.getMsgType());
                break;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        String uid = context.channel().attr(UID).get();
        String roomId = context.channel().attr(ROOM_ID).get();
        if (StringUtil.isNullOrEmpty(uid)) {
            super.channelInactive(context);
        }
        context.channel().attr(UID).set(null);
        context.channel().attr(ROOM_ID).set(null);

        leaveRoom(roomId, uid);
    }

    private void leaveRoom(String roomId, String uid) {
        Set<String> uids = roomMap.get(roomId);
        if (CollectionUtils.isEmpty(uids)) {
            roomMap.remove(roomId);
        } else {
            uids.remove(uid);
            for (String otherUid : uids) {
                if (uid.equals(otherUid)) {
                    continue;
                }
                Channel channel = channelMap.get(otherUid);
                channel.writeAndFlush(gson.toJson(ChatMsg.builder()
                        .msgType(ChatMsg.Type.LEAVE)
                        .uid(uid)
                        .msg(uid + " leave the room")
                        .roomId(roomId)
                        .build()));
            }
            roomMap.put(roomId, uids);
        }
        channelMap.remove(uid);

        System.out.println("user: " + uid + " leave the room: " + roomId);
    }
}
