package com.second.hand.trading.server.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.second.hand.trading.server.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

@ServerEndpoint("/websocket/{userId}")
@Component
public class WebSocketServer {
    // 用于存储用户的在线状态
    private static final Map<Long, WebSocketServer> onlineStatusMap = Collections.synchronizedMap(new HashMap<>());
    private Session session;
    private Long userId;
    private static final Map<Long,Session> userSessions = new LinkedHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        this.session = session;
        userSessions.put(userId, session);
        this.userId = userId;
        onlineStatusMap.put(userId, this);
        //给每个在线用户发送消息;
        sendAllMessage("{\"type\":\"online\"}");
    }

    @OnMessage
    public void onMessage(String message) {
        // 处理接收到的消息
        System.out.println("Received message: " + message);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(message);

            Long targetId = rootNode.path("target").asLong();
            String data = rootNode.path("data").toString();
            System.out.println(data);

            Session targetSession = userSessions.get(targetId);
            if (targetSession != null && targetSession.isOpen()) {
                targetSession.getBasicRemote().sendText(data);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose() {
        onlineStatusMap.remove(userId);
        //给每个在线用户发送消息;
        sendAllMessage("{\"type\":\"online\"}");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("WebSocket 错误");
        error.printStackTrace();
    }

    public void sendAllMessage(String message) {
        synchronized (onlineStatusMap) {
            for (WebSocketServer server : onlineStatusMap.values()) {
                Session session = server.session;
                if (session != null && session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        System.err.println("发送失败: " + e.getMessage());
                    }
                }
            }
        }
    }

    // 获取用户在线状态
    public static boolean isOnline(Long userId) {
        return onlineStatusMap.containsKey(userId);
    }

}