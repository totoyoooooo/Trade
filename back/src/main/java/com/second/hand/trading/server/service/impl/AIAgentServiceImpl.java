package com.second.hand.trading.server.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.second.hand.trading.server.client.MCPClient;
import com.second.hand.trading.server.model.IdleItemModel;
import com.second.hand.trading.server.service.AIAgentService;
import com.second.hand.trading.server.service.IdleItemService;
import com.second.hand.trading.server.vo.ChatResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AIAgentServiceImpl implements AIAgentService {
    @Value("${ai.model.api-key}")
    private String apiKey;

    @Value("${ai.model.base-url}")
    private String baseUrl;

    @Value("${ai.model.model-name}")
    private String modelName;

    @Value("${mcp.python-path}")
    private String pythonPath;

    @Value("${mcp.script-path}")
    private String scriptPath;

    /**
     * 商品ID标识符的正则表达式
     * 格式：[商品名称](商品ID)
     */
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("\\[(.+?)]\\((\\d+)\\)");

    // 就绪标志，防止初始化失败系统崩溃
    private final AtomicBoolean mcpReady = new AtomicBoolean(false);

    @Autowired
    private IdleItemService idleItemService;
    private final MCPClient mcpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIAgentServiceImpl(MCPClient mcpClient) {
        this.mcpClient = mcpClient;
    }

    @PostConstruct
    public void initMCPClient() {
        try {
            // 启动MCP服务器进程
            URL resourceUrl = getClass().getClassLoader().getResource("mcp_shopping_server.py");
            if (resourceUrl == null) {
                log.error("无法在类路径中找到Python脚本: mcp_shopping_server.py");
                throw new RuntimeException("启动失败：找不到 mcp_shopping_server.py");
            }
            // 使用 toURI() 和 Paths.get() 来正确处理路径
            String pythonScriptPath = Paths.get(resourceUrl.toURI()).toString();

            log.info("使用命令 {} {}", pythonPath, pythonScriptPath);

            ProcessBuilder pb = new ProcessBuilder(pythonPath, pythonScriptPath);
            pb.environment().put("PYTHONPATH", System.getProperty("user.dir"));
            pb.redirectErrorStream(true);

            mcpClient.initialize(pb);
            mcpReady.set(true);
            log.info("智能MCP服务器已启动");
        } catch (Exception e) {
            log.error("智能MCP服务器启动失败", e);
            // throw new RuntimeException("智能MCP服务器启动失败", e);
        }
    }

    @Override
    public boolean isMcpServerReady() {
        return mcpReady.get();
    }

    /**
     * 在沟通过程中采用基本格式为
     * "messages":[{
     *     "role": "system | user | assistant | tool",
     *     "content": "",
     *     "tool_calls": ""
     * }]
     *  - system    表示系统提示
     *  - user      表示用户消息
     *  - assistant 表示回复消息
     *  - tool      表示工具调用
     *
     *
     * @param sessionId 会话ID唯一标识每一次会话
     * @param userMessage 用户当前发送的消息
     * @param userId
     * @return
     */
    @Override
    public ChatResponseVo chatWithAgent(String sessionId, String userMessage, Long userId) {
        try {
            List<Map<String, Object>> messages = getSessionMessages(sessionId);

            // 如果是新会话，加 system prompt
            if (messages.isEmpty()) {
                // 构建系统提示，告诉AI如何使用新的智能MCP工具
                String systemPrompt = buildSystemPrompt();

                // 构建用户消息
                String userContent = String.format("""
                用户消息: %s
                用户ID: %s

                请根据用户需求，主动使用合适的智能工具查询商品信息，然后给出专业的购买建议。
                优先使用 smart_search 进行智能搜索，它支持模糊匹配和相关度排序。
                在回答中如果要推荐具体商品，请使用格式：[商品名称](商品ID)，默认ID为0，此格式会自动渲染为商品卡片

                """, userMessage, userId);
                // 初始消息历史
                messages.add(Map.of("role", "system", "content", systemPrompt));
                messages.add(Map.of("role", "user", "content", userContent));

            } else {
                // 先前已经对话过 追加用户消息
                appendMessage(messages, "user",
                        String.format("用户消息: %s\n用户ID: %s", userMessage, userId));
            }

            // 调用支持工具的LLM
            String reply = callLLMWithTools(messages);
            log.info("After MCP response, llm's reply: {}", reply);

            // 保存历史
            saveSessionMessages(sessionId, messages);

            // 从回复中提取商品ID
            List<Long> productIds = extractProductIds(reply);

            // 根据ID查询商品详细信息，并随着message一同给前端，以便构造商品卡片
            List<IdleItemModel> products = queryIdleItemDetails(productIds);

            return ChatResponseVo.builder()
                    .message(reply)
                    .success(true)
                    .products(products)
                    .productsCount(products.size())
                    .build();
        } catch (Exception e) {
            log.error("AI Agent服务异常: sessionId={}, message={}", sessionId, userMessage, e);
            throw new RuntimeException("AI服务暂时不可用", e);
        }
    }


    /**
     * 流式处理与Agent的聊天。
     * 这个方法会通过 SSE (Server-Sent Events) 将数据流式传输给客户端。
     *
     * @param sessionId   会话ID
     * @param userMessage 用户消息
     * @param userId      用户ID
     * @param out         HTTP响应的OutputStream，用于写入SSE数据
     */
    public void chatWithAgentStream(String sessionId, String userMessage, Long userId, OutputStream out) {
        List<Map<String, Object>> messages = getSessionMessages(sessionId);
        if (messages.isEmpty()) {
            messages.add(Map.of("role", "system", "content", buildSystemPrompt()));
            // 构建用户消息
            String userContent = String.format("""
                用户消息: %s
                用户ID: %s

                请根据用户需求，主动使用合适的智能工具查询商品信息，然后给出专业的购买建议。
                优先使用 smart_search 进行智能搜索，它支持模糊匹配和相关度排序。
                在回答中如果要推荐具体商品，请使用格式：[商品名称](商品ID)，默认ID为0，此格式会自动渲染为商品卡片

                """, userMessage, userId);
            messages.add(Map.of("role", "user", "content", userContent));
        }
        appendMessage(messages, "user",
                String.format("用户消息: %s\n用户ID: %s", userMessage, userId));

        // TODO: 调用流式 LLM API，并将生成内容逐步写入 out
        // 未来接入 DeepSeek / OpenAI Stream API 时替换此处
    }

    private String buildSystemPrompt() {
        return """
        你是一个智能的二手商品购物助手。你可以使用以下智能MCP工具来帮助用户：

        1. smart_search - 智能商品搜索（主要工具，支持模糊匹配、分类识别、相关度排序）
        2. browse_categories - 按分类浏览商品
        3. get_popular_items - 获取热门商品
        4. analyze_tags - 分析商品标签分布

        商品分类系统：
        - 数码：手机、电脑、平板、相机等电子产品
        - 家电：冰箱、洗衣机、空调、电视等家用电器
        - 户外：帐篷、登山用品、运动器材、自行车等
        - 图书：各类书籍、教材、小说、漫画等
        - 其他：不属于上述分类的商品

        标签系统：商品使用 #标签1#标签2 的格式，系统会自动解析和搜索

        智能搜索功能：
        - 支持模糊关键词搜索
        - 自动识别分类（输入"手机"会匹配数码分类）
        - 相关度评分排序
        - 支持价格范围筛选
        - 多种排序方式：时间、价格、热门度、相关度

        工作流程：
        1. 理解用户需求（商品类型、价格范围、特定要求）
        2. 优先使用 smart_search 进行智能搜索
        3. 必要时使用其他工具（分类浏览、热门商品、标签分析）
        4. 不使用的参数可以直接不写
        5. 基于真实查询结果给出个性化建议
        6. 考虑价格、品质、热门度、发布时间等因素

        重要原则：
        - 主要使用 smart_search 工具，它最智能
        - 基于实际查询结果回答，不要编造信息以及id
        - 推荐商品时使用格式 [商品名称](商品ID) 没有准确ID，请勿推荐，此格式会自动渲染为商品卡片
          例如：
          \\"根据你的要求，我为你找到了以下几款机器人：[机器人aa] - 相关度评分: 10 这款机器人的描述为 aaa，它属于 科幻 类别。价格为 ¥1000.00。
             根据这些信息，我推荐你考虑 [机器人aa](149) 你可以根据自己的需求进一步决定\\"    
          上面的答复将会渲染成
          \\"根据你的要求，我为你找到了以下几款机器人：机器人aa - 相关度评分: 10 这款机器人的描述为 aaa，它属于 科幻 类别。价格为 ¥1000.00。
             根据这些信息，我推荐你考虑 
             #149对应的商品详情卡片# 
             你可以根据自己的需求进一步决定\\" 
        - 如果用户问分类相关，使用 browse_categories
        - 如果用户想了解热门商品，使用 get_popular_items
        - 如果用户询问标签或想了解市场趋势，使用 analyze_tags
        """;
    }

    /**
     * 核心：循环式 Agent 调用
     * - messages 历史包含 system/user/assistant/tool
     * - 每次将当前历史发给 LLM
     * - 若返回包含 tool_calls，则调用对应 MCP 工具并把工具输出以 role=tool 加入历史
     * - 继续循环直到没有 tool_calls 或达到 maxAgentSteps
     */
    private String callLLMWithTools(List<Map<String, Object>> messages) {
        try {
            // 准备智能MCP工具定义
            List<Map<String, Object>> tools = prepareSmartMCPTools();

            // 定义最大的agent思考轮次
            final int maxAgentSteps = 15;
            String lastAssistantContent = "";

            for (int step = 0; step < maxAgentSteps; step++) {

                // 可以在开始前先剪切一部分token，（未测试过）
                // trimMessages(messages);

                // 构建请求
                // 其中 tools 用以告诉AI可以使用的工具
                Map<String, Object> request = Map.of(
                        "model", modelName,
                        "messages", messages,
                        "tools", tools,
                        "tool_choice", "auto",
                        "max_tokens", 4000,
                        "temperature", 0.1
                );
                // DEBUG
                // log.info("Step{} - AI Agent Request: {}", step, request);
                // 调用API
                String responseJson = callLLMAPI(request);
                log.info("Step{} - AI Agent Response: {}", step, responseJson);
                JsonNode root = objectMapper.readTree(responseJson);
                JsonNode choice = root.get("choices").get(0);
                JsonNode message = choice.get("message");

                String assistantContent = message.has("content") && !message.get("content").isNull()
                        ? message.get("content").asText() : "";

                lastAssistantContent = assistantContent;

                //检查是否有工具调用
                JsonNode toolCalls = message.get("tool_calls");
                if (toolCalls == null || toolCalls.isEmpty()) {
                    if (assistantContent != null && !assistantContent.isEmpty()) {
                        appendMessage(messages, "assistant", assistantContent);
                    }
                    // 没有工具调用，说明 Agent 认为已经可以回答了 -> 返回 assistant 内容
                    log.info("Agent 在第 {} 步完成回答。", step + 1);
                    return assistantContent;
                }
                // 把 assistant 消息加入历史（LLM的“想法/说明/下一步计划”）
                // appendMessage(messages, "assistant", assistantContent);

                Map<String, Object> assistantMsg = new HashMap<>();
                assistantMsg.put("role", "assistant");
                assistantMsg.put("content", assistantContent);

                List<Map<String, Object>> toolCallsList = objectMapper.convertValue(toolCalls, List.class);
                assistantMsg.put("tool_calls", toolCallsList);

                messages.add(assistantMsg);

                // 有工具调用 -> 逐个调用 MCP 工具，并把工具结果以 role=tool 加入 messages
                for ( JsonNode toolCall : toolCalls ) {
                    try {
                        String toolName = toolCall.get("function").get("name").asText();
                        String toolCallId = toolCall.has("id")
                                ? toolCall.get("id").asText()
                                : "tool_call_" + System.nanoTime();

                        // arguments 有时是字符串，需要再次反序列化
                        JsonNode argumentsNode = toolCall.get("function").get("arguments");
                        JsonNode arguments = argumentsNode;
                        if (argumentsNode != null && argumentsNode.isTextual()) {
                            arguments = objectMapper.readTree(argumentsNode.asText());
                        }

                        log.info("Agent 请求工具: {} 参数: {}", toolName, arguments);

                        // 调用 MCP 工具
                        String toolResult = callMCPTool(toolName, arguments);

                        log.info("工具 {} 返回长度: {}", toolName, toolResult == null ? 0 : toolResult.length());
                        if (toolResult != null) {
                            log.info("{}",String.join("", toolResult.split("\n")));
                        }

                        messages.add(Map.of(
                                "role", "tool",
                                "tool_call_id", toolCallId,
                                "content", toolResult == null ? "" : toolResult
                        ));

                    } catch (Exception toolEx) {
                        log.error("处理单个工具调用失败", toolEx);
                        // 把错误信息作为工具输出注入，让 LLM 知道工具失败了
                        messages.add(Map.of(
                                "role", "tool",
                                "tool_call_id", "error_" + System.nanoTime(),
                                "content", "工具调用失败: " + toolEx.getMessage()
                        ));
                    }
                }
                // 继续下一轮：将带有工具结果的 messages 发回 LLM 以决定下一步。
            }

            log.warn("达到最大 Agent 步数 {}，仍未得到无工具调用的最终回复，返回最后一次 assistant 内容。", maxAgentSteps);
            return lastAssistantContent;


            // 单轮次调用 用户 -> AI -> 工具 -> AI -> 最终答复
            // 构建系统提示，告诉AI如何使用新的智能MCP工具
            //String systemPrompt = buildSystemPrompt();

            // 构建用户消息
            /*
            String fullMessage = String.format("""
                %s

                用户消息: %s
                用户ID: %s

                请根据用户需求，主动使用合适的智能工具查询商品信息，然后给出专业的购买建议。
                优先使用 smart_search 进行智能搜索，它支持模糊匹配和相关度排序。
                在回答中如果要推荐具体商品，请使用格式：[商品名称](商品ID)
                """, systemPrompt, userMessage, userId);
            */
            // 调用支持工具的LLM
            // return callLLMWithTools(fullMessage);


        } catch (Exception e) {
            log.error("LLM调用失败", e);
            throw new RuntimeException("LLM调用失败", e);
        }
    }

    private List<Map<String, Object>> prepareSmartMCPTools() {
        return List.of(
                // 智能搜索工具 - 主要工具
                Map.of(
                        "type", "function",
                        "function", Map.of(
                                "name", "smart_search",
                                "description", "智能商品搜索，支持模糊匹配、分类识别、标签解析、相关度排序等功能",
                                "parameters", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "query", Map.of(
                                                        "type", "string",
                                                        "description", "搜索查询，可以是商品名称、描述关键词、分类名称等"
                                                ),
                                                "category", Map.of(
                                                        "type", "string",
                                                        "description", "商品分类：数码、家电、户外、图书、其他，或相关关键词"
                                                ),
                                                "min_price", Map.of(
                                                        "type", "number",
                                                        "description", "最低价格"
                                                ),
                                                "max_price", Map.of(
                                                        "type", "number",
                                                        "description", "最高价格"
                                                ),
                                                "sort_by", Map.of(
                                                        "type", "string",
                                                        "description", "排序方式：time(时间), price_low(价格低到高), price_high(价格高到低), popular(热门), relevance(相关度)",
                                                        "enum", List.of("time", "price_low", "price_high", "popular", "relevance")
                                                ),
                                                "limit", Map.of(
                                                        "type", "integer",
                                                        "description", "结果数量限制，默认10"
                                                )
                                        ),
                                        "required", List.of("query")
                                )
                        )
                ),

                // 分类浏览工具
                Map.of(
                        "type", "function",
                        "function", Map.of(
                                "name", "browse_categories",
                                "description", "浏览各分类下的商品",
                                "parameters", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "category", Map.of(
                                                        "type", "string",
                                                        "description", "要浏览的商品分类",
                                                        "enum", List.of("数码", "家电", "户外", "图书", "其他")
                                                ),
                                                "limit", Map.of(
                                                        "type", "integer",
                                                        "description", "结果数量，默认10"
                                                )
                                        ),
                                        "required", List.of("category")
                                )
                        )
                ),

                // 热门商品工具
                Map.of(
                        "type", "function",
                        "function", Map.of(
                                "name", "get_popular_items",
                                "description", "获取热门商品（按浏览次数排序）",
                                "parameters", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "limit", Map.of(
                                                        "type", "integer",
                                                        "description", "结果数量，默认10"
                                                )
                                        )
                                )
                        )
                ),

                // 标签分析工具
                Map.of(
                        "type", "function",
                        "function", Map.of(
                                "name", "analyze_tags",
                                "description", "分析系统中的所有标签，了解商品标签分布和市场趋势",
                                "parameters", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "limit", Map.of(
                                                        "type", "integer",
                                                        "description", "返回最热门的标签数量，默认20"
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private String callLLMAPI(Map<String, Object> request) throws Exception {
        String json = objectMapper.writeValueAsString(request);

        HttpURLConnection conn = (HttpURLConnection) new URL(baseUrl + "/v1/chat/completions").openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream is = null;
        if (code >= 200 && code < 300) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();
            log.error("API 返回非 2xx 状态: {}，请求体: {}", code, json);
        }

        try (InputStream in = is) {
            String resp = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            if (code < 200 || code >= 300) {
                log.error("错误响应: {}", resp);
                throw new RuntimeException("API 错误: HTTP " + code + " - " + resp);
            }
            return resp;
        }
    }

    private String processLLMResponse(String response) throws Exception {
        JsonNode jsonResponse = objectMapper.readTree(response);
        JsonNode message = jsonResponse.get("choices").get(0).get("message");

        // 检查是否有工具调用
        JsonNode toolCalls = message.get("tool_calls");
        if (toolCalls != null && toolCalls.size() > 0) {
            return handleToolCalls(toolCalls, message);
        } else {
            // 直接返回文本回复
            return message.get("content").asText();
        }
    }

    private String handleToolCalls(JsonNode toolCalls, JsonNode originalMessage) throws Exception {
        List<Map<String, Object>> messages = new ArrayList<>();

        // 添加原始助手消息
        String originalContent = originalMessage.get("content") != null ?
                originalMessage.get("content").asText() : "";
        messages.add(Map.of("role", "assistant", "content", originalContent));

        // 处理每个工具调用
        for (JsonNode toolCall : toolCalls) {
            String toolName = toolCall.get("function").get("name").asText();
            JsonNode arguments = objectMapper.readTree(toolCall.get("function").get("arguments").asText());

            log.info("调用MCP工具: {} 参数: {}", toolName, arguments);

            // 调用智能MCP工具
            String toolResult = callMCPTool(toolName, arguments);

            log.info("工具调用结果长度: {}", toolResult.length());

            // 添加工具调用结果到消息历史
            messages.add(Map.of(
                    "role", "tool",
                    "tool_call_id", toolCall.get("id").asText(),
                    "content", toolResult
            ));
        }

        // 再次调用LLM生成基于工具结果的回答
        Map<String, Object> followUpRequest = Map.of(
                "model", modelName,
                "messages", messages,
                "max_tokens", 4000,
                "temperature", 0.1
        );

        String finalResponse = callLLMAPI(followUpRequest);
        JsonNode finalMessage = objectMapper.readTree(finalResponse)
                .get("choices").get(0).get("message");

        return finalMessage.get("content").asText();
    }

    private String callMCPTool(String toolName, JsonNode arguments) {
        try {
            Map<String, Object> args = objectMapper.convertValue(arguments, Map.class);
            return mcpClient.callTool(toolName, args);
        } catch (Exception e) {
            log.error("智能MCP工具调用失败: " + toolName, e);
            return "工具调用失败: " + e.getMessage();
        }
    }


    // 会话存储 - 简单用内存 Map
    private final Map<String, List<Map<String, Object>>> sessionStore = new ConcurrentHashMap<>();

    private List<Map<String, Object>> getSessionMessages(String sessionId) {
        return sessionStore.computeIfAbsent(sessionId, k -> new ArrayList<>());
    }

    private void saveSessionMessages(String sessionId, List<Map<String, Object>> messages) {
        sessionStore.put(sessionId, messages);
    }

    private void appendMessage(List<Map<String, Object>> messages, String role, String content) {
        messages.add(Map.of("role", role, "content", content));
    }

    private void trimMessages(List<Map<String, Object>> messages) {
        final int maxInputTokens = 110_000; // 留点余量
        int totalTokens = estimateTokens(messages);
        if (totalTokens <= maxInputTokens) return;

        Map<String, Object> first = messages.get(0);
        boolean keepSystem = "system".equals(first.get("role"));
        List<Map<String, Object>> trimmed = new ArrayList<>();
        if (keepSystem) trimmed.add(first);

        // 从后往前加，直到达到限制
        int countTokens = keepSystem ? estimateTokens(List.of(first)) : 0;
        for (int i = messages.size() - 1; i >= (keepSystem ? 1 : 0); i--) {
            int msgTokens = estimateTokens(List.of(messages.get(i)));
            if (countTokens + msgTokens > maxInputTokens) break;
            trimmed.add(keepSystem ? 1 : 0, messages.get(i));
            countTokens += msgTokens;
        }

        messages.clear();
        messages.addAll(trimmed);
    }

    // 简单 token 估算（可用更精确的分词器）
    private int estimateTokens(List<Map<String, Object>> msgs) {
        int count = 0;
        for (Map<String, Object> msg : msgs) {
            String content = String.valueOf(msg.get("content"));
            count += content.length() / 3; // 粗略估算
        }
        return count;
    }



    /**
     * 从LLM回复中提取商品ID
     */
    private List<Long> extractProductIds(String response) {
        List<Long> productIds = new ArrayList<>();
        Matcher matcher = PRODUCT_ID_PATTERN.matcher(response);

        while (matcher.find()) {
            try {
                Long productId = Long.parseLong(matcher.group(2));
                if (!productIds.contains(productId)) {
                    productIds.add(productId);
                }
            } catch (NumberFormatException e) {
                log.warn("解析商品ID失败: {}", matcher.group(2));
            }
        }

        log.info("从回复中提取到商品ID: {}", productIds);
        return productIds;
    }


    /**
     * 查询商品详细信息
     */
    private List<IdleItemModel> queryIdleItemDetails(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Collections.emptyList();
        }

        return productIds.stream()
                .map(this::getIdleItemModel)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取单个商品信息
     */
    private IdleItemModel getIdleItemModel(Long productId) {
        try {
            // 调用ProductService查询商品详情
            IdleItemModel product = idleItemService.getIdleItem(productId);
            if (product == null) {
                log.warn("商品不存在: {}", productId);
                return null;
            }

            IdleItemModel idleItemModel = new IdleItemModel();
            idleItemModel.setId(product.getId());
            idleItemModel.setIdleName(product.getIdleName());
            idleItemModel.setIdlePrice(product.getIdlePrice());
            idleItemModel.setIdlePlace(product.getIdlePlace());
            idleItemModel.setPictureList(product.getPictureList());
            idleItemModel.setIdleLabel(product.getIdleLabel());
            idleItemModel.setIdleStatus(product.getIdleStatus());
            idleItemModel.setSkimCount(product.getSkimCount());
            idleItemModel.setIdleTag(product.getIdleTag());
            idleItemModel.setIdleDetails(product.getIdleDetails());

            return idleItemModel;

        } catch (Exception e) {
            log.error("查询商品信息失败: productId={}", productId, e);
            return null;
        }
    }



}