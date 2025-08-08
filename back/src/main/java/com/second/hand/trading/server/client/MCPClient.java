package com.second.hand.trading.server.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class MCPClient {

    private Process mcpProcess;
    private BufferedWriter processInput;
    private BufferedReader processOutput;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicLong requestIdCounter = new AtomicLong(1);
    private boolean isInitialized = false;

    /**
     * 初始化MCP客户端并启动服务器进程
     * @param processBuilder 进程构建器
     * @throws IOException 如果进程启动失败
     */
    public void initialize(ProcessBuilder processBuilder) throws IOException {
        try {
            log.info("启动MCP服务器进程...");
            this.mcpProcess = processBuilder.start();
            this.processInput = new BufferedWriter(new OutputStreamWriter(this.mcpProcess.getOutputStream()));
            this.processOutput = new BufferedReader(new InputStreamReader(this.mcpProcess.getInputStream()));

            // 执行MCP协议握手
            performHandshake();

            log.info("MCP客户端初始化完成");
        } catch (Exception e) {
            log.error("MCP客户端初始化失败", e);
            cleanup();
            throw new IOException("MCP客户端初始化失败", e);
        }
    }

    /**
     * 执行MCP协议握手过程
     * @throws Exception 如果握手失败
     */
    private void performHandshake() throws Exception {
        log.info("开始MCP协议握手...");

        // 1. 发送initialize请求
        Map<String, Object> initializeRequest = Map.of(
                "jsonrpc", "2.0",
                "id", requestIdCounter.getAndIncrement(),
                "method", "initialize",
                "params", Map.of(
                        "protocolVersion", "1.0.0",
                        "capabilities", Map.of(),
                        "clientInfo", Map.of(
                                "name", "java-mcp-client",
                                "version", "1.0.0"
                        )
                )
        );

        log.debug("发送initialize请求: {}", objectMapper.writeValueAsString(initializeRequest));
        String initResponse = sendRequestAndGetResponse(initializeRequest);
        log.debug("收到initialize响应: {}", initResponse);

        JsonNode initResponseNode = objectMapper.readTree(initResponse);
        if (initResponseNode.has("error")) {
            throw new Exception("Initialize失败: " + initResponseNode.get("error").get("message").asText());
        }

        // 2. 发送initialized通知
        Map<String, Object> initializedNotification = Map.of(
                "jsonrpc", "2.0",
                "method", "notifications/initialized"
        );

        log.debug("发送initialized通知");
        sendNotification(initializedNotification);

        this.isInitialized = true;
        log.info("MCP协议握手完成");
    }

    /**
     * 发送请求并获取响应
     * @param request 请求对象
     * @return 响应JSON字符串
     * @throws Exception 如果发送或接收失败
     */
    private String sendRequestAndGetResponse(Map<String, Object> request) throws Exception {
        String requestJson = objectMapper.writeValueAsString(request);
        processInput.write(requestJson);
        processInput.newLine();
        processInput.flush();

        String response = processOutput.readLine();
        if (response == null) {
            throw new Exception("MCP服务器意外关闭");
        }

        return response;
    }

    /**
     * 发送通知（不需要响应）
     * @param notification 通知对象
     * @throws Exception 如果发送失败
     */
    private void sendNotification(Map<String, Object> notification) throws Exception {
        String notificationJson = objectMapper.writeValueAsString(notification);
        processInput.write(notificationJson);
        processInput.newLine();
        processInput.flush();
    }

    /**
     * 调用MCP工具
     * @param toolName 工具名称
     * @param arguments 工具参数
     * @return 工具执行结果
     * @throws Exception 如果调用失败
     */
    public String callTool(String toolName, Map<String, Object> arguments) throws Exception {
        if (!isInitialized) {
            throw new IllegalStateException("MCP客户端未初始化，请先调用initialize()");
        }

        log.debug("调用MCP工具: {} 参数: {}", toolName, arguments);

        // 构建MCP工具调用消息
        Map<String, Object> request = Map.of(
                "jsonrpc", "2.0",
                "id", requestIdCounter.getAndIncrement(),
                "method", "tools/call",
                "params", Map.of(
                        "name", toolName,
                        "arguments", arguments != null ? arguments : Map.of()
                )
        );

        try {
            // 发送请求并获取响应
            String responseJson = sendRequestAndGetResponse(request);
            log.debug("工具调用响应: {}", responseJson);

            JsonNode response = objectMapper.readTree(responseJson);

            // 检查错误
            if (response.has("error")) {
                JsonNode error = response.get("error");
                String errorMsg = String.format("MCP工具调用错误 [%d]: %s",
                        error.get("code").asInt(),
                        error.get("message").asText());
                log.error(errorMsg);
                throw new Exception(errorMsg);
            }

            // 解析结果
            JsonNode result = response.get("result");
            if (result == null) {
                throw new Exception("工具调用响应中缺少result字段");
            }

            // 提取content字段中的文本
            JsonNode content = result.get("content");
            if (content == null || !content.isArray() || content.size() == 0) {
                throw new Exception("工具调用响应中缺少content字段或内容为空");
            }

            JsonNode firstContent = content.get(0);
            if (!firstContent.has("text")) {
                throw new Exception("工具调用响应的content中缺少text字段");
            }

            String resultText = firstContent.get("text").asText();
            log.debug("工具调用成功，结果长度: {}", resultText.length());

            return resultText;

        } catch (IOException e) {
            log.error("工具调用网络错误: {}", e.getMessage());
            throw new Exception("MCP通信错误: " + e.getMessage(), e);
        }
    }

    /**
     * 列出可用工具
     * @return 工具列表JSON字符串
     * @throws Exception 如果列出工具失败
     */
    public String listTools() throws Exception {
        if (!isInitialized) {
            throw new IllegalStateException("MCP客户端未初始化，请先调用initialize()");
        }

        log.debug("列出MCP工具");

        Map<String, Object> request = Map.of(
                "jsonrpc", "2.0",
                "id", requestIdCounter.getAndIncrement(),
                "method", "tools/list",
                "params", Map.of()
        );

        try {
            String responseJson = sendRequestAndGetResponse(request);
            log.debug("工具列表响应: {}", responseJson);

            JsonNode response = objectMapper.readTree(responseJson);

            if (response.has("error")) {
                throw new Exception("列出工具失败: " + response.get("error").get("message").asText());
            }

            return responseJson;

        } catch (IOException e) {
            throw new Exception("MCP通信错误: " + e.getMessage(), e);
        }
    }

    /**
     * 检查MCP服务器是否正在运行
     * @return 如果服务器正在运行返回true
     */
    public boolean isServerAlive() {
        return mcpProcess != null && mcpProcess.isAlive() && isInitialized;
    }

    /**
     * 获取服务器进程信息
     * @return 进程信息字符串
     */
    public String getServerStatus() {
        if (mcpProcess == null) {
            return "进程未启动";
        }

        if (!mcpProcess.isAlive()) {
            return "进程已停止 (退出码: " + (mcpProcess.exitValue()) + ")";
        }

        return "进程运行中 (PID: " + mcpProcess.pid() + ", 已初始化: " + isInitialized + ")";
    }

    /**
     * 清理资源
     */
    @PreDestroy
    public void cleanup() {
        log.info("清理MCP客户端资源...");

        try {
            if (processInput != null) {
                processInput.close();
            }
        } catch (IOException e) {
            log.warn("关闭输入流失败", e);
        }

        try {
            if (processOutput != null) {
                processOutput.close();
            }
        } catch (IOException e) {
            log.warn("关闭输出流失败", e);
        }

        if (mcpProcess != null) {
            if (mcpProcess.isAlive()) {
                log.info("终止MCP服务器进程...");
                mcpProcess.destroy();

                try {
                    // 等待进程优雅退出
                    if (!mcpProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                        log.warn("进程未在5秒内退出，强制终止");
                        mcpProcess.destroyForcibly();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mcpProcess.destroyForcibly();
                }
            }
        }

        isInitialized = false;
        log.info("MCP客户端资源清理完成");
    }
}