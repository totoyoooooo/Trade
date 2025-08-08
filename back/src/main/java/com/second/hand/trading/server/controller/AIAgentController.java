package com.second.hand.trading.server.controller;

import com.second.hand.trading.server.enums.ErrorMsg;
import com.second.hand.trading.server.model.ChatRequestModel;
import com.second.hand.trading.server.service.AIAgentService;
import com.second.hand.trading.server.vo.ChatResponseVo;
import com.second.hand.trading.server.vo.ResultVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
}