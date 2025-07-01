package com.second.hand.trading.server.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint("/websocket/{userId}")
@Component
public class WebSocketServer {
    private static final Set<WebSocketServer> webSocketSet = Collections.synchronizedSet(new HashSet<>());
    // 用于存储用户的在线状态
    private static final Map<Long, Boolean> onlineStatusMap = Collections.synchronizedMap(new HashMap<>());
    private static final AtomicInteger onlineCount = new AtomicInteger(0);
    private Session session;
    private Long userId;
    private static final Map<Long,Session> userSessions = new LinkedHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        this.session = session;
        userSessions.put(userId, session);
        this.userId = userId;
        webSocketSet.add(this);
        onlineStatusMap.put(userId, true);
        onlineCount.incrementAndGet();
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
        webSocketSet.remove(this);
        onlineStatusMap.remove(userId);
        onlineCount.decrementAndGet();
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("WebSocket 错误");
        error.printStackTrace();
    }

    public static void sendAllMessage(String message) {
        for (WebSocketServer webSocket : webSocketSet) {
            try {
                webSocket.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 获取用户在线状态
    public static boolean isOnline(Long userId) {
        return onlineStatusMap.getOrDefault(userId, false);
    }

}