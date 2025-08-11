package com.second.hand.trading.server.controller;

import com.second.hand.trading.server.enums.ErrorMsg;
import com.second.hand.trading.server.model.ChatRequestModel;
import com.second.hand.trading.server.service.AIAgentService;
import com.second.hand.trading.server.vo.ChatResponseVo;
import com.second.hand.trading.server.vo.ResultVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.awt.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// 控制器
@Slf4j
@RestController
@RequestMapping("ai-agent")
@CrossOrigin()
public class AIAgentController {

    @Autowired
    private AIAgentService aiAgentService;

    @PostMapping("chat")
    public ResultVo<ChatResponseVo> chat(@RequestBody ChatRequestModel request) {
        if (aiAgentService.isMcpServerReady()) {
            try {
                ChatResponseVo response = aiAgentService.chatWithAgent(
                        request.getSessionId(),
                        request.getMessage(),
                        request.getUserId()
                );

                return ResultVo.success(response);
            } catch (Exception e) {
                log.error("AI聊天服务异常", e);
                return ResultVo.fail(ErrorMsg.SYSTEM_ERROR, ChatResponseVo.builder()
                        .message("抱歉，我暂时无法回答您的问题")
                        .success(false)
                        .products(null)
                        .productsCount(0)
                        .build());
            }

        } else {
            log.error("智能MCP服务器启动失败");
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR, ChatResponseVo.builder()
                    .message("后端MCP服务器启动故障")
                    .success(false)
                    .products(null)
                    .productsCount(0)
                    .build());
        }
    }

    /**
     * SSE 流式对话接口
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody ChatRequestModel request) {
        log.info("开始流式对话: sessionId={}, message={}", request.getSessionId(), request.getMessage());

        // 创建 SSE 连接，设置超时时间为 5 分钟
        SseEmitter emitter = new SseEmitter(300000L);

        // 异步处理对话
        CompletableFuture.runAsync(() -> {
            try {
                // 检查 MCP 服务器状态
                if (!aiAgentService.isMcpServerReady()) {
                    sendError(emitter, "MCP服务器未就绪，请稍后重试");
                    return;
                }

                // 发送开始标记
                sendEvent(emitter, "start", "开始处理您的请求...");

                // 调用流式对话服务
                aiAgentService.chatWithAgentStream(
                        request.getSessionId(),
                        request.getMessage(),
                        request.getUserId(),
                        emitter
                );

            } catch (Exception e) {
                log.error("流式对话异常: sessionId={}", request.getSessionId(), e);
                sendError(emitter, "抱歉，AI助手暂时不可用，请稍后重试");
            }
        });

        // 设置连接关闭和异常处理
        emitter.onCompletion(() -> {
            log.info("SSE连接正常关闭: sessionId={}", request.getSessionId());
        });

        emitter.onTimeout(() -> {
            log.warn("SSE连接超时: sessionId={}", request.getSessionId());
            emitter.complete();
        });

        emitter.onError((ex) -> {
            log.error("SSE连接异常: sessionId={}", request.getSessionId(), ex);
            emitter.completeWithError(ex);
        });

        return emitter;
    }

    private void sendEvent(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(event)
                    .data(data)
                    .reconnectTime(3000));
        } catch (IOException e) {
            log.error("发送SSE事件失败: event={}", event, e);
            emitter.completeWithError(e);
        }
    }

    private void sendError(SseEmitter emitter, String errorMessage) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(errorMessage));
            emitter.complete();
        } catch (IOException e) {
            log.error("发送错误事件失败", e);
            emitter.completeWithError(e);
        }
    }
}