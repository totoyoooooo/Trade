package com.second.hand.trading.server.service;

import com.second.hand.trading.server.vo.ChatResponseVo;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

public interface AIAgentService {
    ChatResponseVo chatWithAgent(String sessionId, String userMessage, Long userId);
    boolean isMcpServerReady();
    void chatWithAgentStream(String sessionId, String userMessage, Long userId, SseEmitter emitter);
}
