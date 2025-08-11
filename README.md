# 后端服务器运行文档

## 项目概述

系统架构包括：

- **Java后端服务**：提供Web API和MCP客户端功能
- **Python MCP服务器**：处理数据库操作和智能搜索

## 系统架构

```
前端 → Java后端 → MCP客户端 → Python MCP服务器 → MySQL数据库
                ↓
              AI模型 (通义千问/DeepSeek/本地Ollama)
```

## 配置说明

### 1. 服务器配置

详情参见[src/main/resources/README.md](back/src/main/resources/README.md)

### 2. AI模型配置

在 `application.properties` 中配置AI模型：

```properties
# AI API configuration - 选择其中一种配置方式

# 方式1: 使用阿里云通义千问 (推荐)
ai.model.api-key=${API_KEY}
ai.model.base-url=https://dashscope.aliyuncs.com/compatible-mode
ai.model.model-name=qwen2.5-72b-instruct

# 方式2: 使用DeepSeek
# ai.model.api-key=${API_KEY}
# ai.model.base-url=https://dashscope.aliyuncs.com/compatible-mode
# ai.model.model-name=deepseek-r1

# 方式3: 使用本地Ollama
# ai.model.api-key=
# ai.model.base-url=http://localhost:11434
# ai.model.model-name=qwen2.5:3b
```

在`pom.xml`中加入依赖

```xml
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
</dependency>
```



### 3. MCP配置

```properties
# MCP服务器配置
mcp.python-path=D:/Tool/SoftWare/Miniconda/envs/py3-11_torch2-5+cu121/python.exe
mcp.script-path=classpath:mcp_shopping_server.py
```

**重要说明：**

- `mcp.python-path`：指向安装了MCP依赖的Python解释器路径
- `mcp.script-path`：MCP服务器脚本路径（通常在resources目录下）

### 4. 环境变量配置

设置API密钥环境变量：

#### IDEA配置:

在IDE的运行配置中添加环境变量：

- **变量名**: `API_KEY`
- **变量值**: `your_actual_api_key`



### 5. 验证系统运行

#### 测试MCP连接

运行测试脚本[src/main/resources/test_java_mcp.bat](back/src/main/resources/test_java_mcp.bat)：

```cmd
cd back/src/main/resources
test_java_mcp.bat
```

预期输出包含AI代理的响应结果，也可手动更改其中内容**自定义**发送消息。

#### 手动测试API

使用curl测试AI代理接口：

```bash
curl -X POST http://localhost:8080/ai-agent/chat \
  -H "Content-Type: application/json" \
  -d "{\"sessionId\": \"1234\", \"message\": \"给我推荐一些机器人\", \"userId\": 12345}"
```



## 常见问题排查

### 1. MCP服务器无法启动

- 检查Python环境和依赖安装
- 确认数据库连接配置正确
- 查看 `back/mcp_shopping_server.log` 日志

### 2. AI模型调用失败

- 确认API_KEY环境变量设置
- 检查网络连接和API地址
- 验证模型名称配置

### 3. 数据库连接错误

- 确认MySQL服务运行状态
- 检查用户名密码和权限
- 验证数据库名称存在

### 4. 端口冲突

- 默认端口8080，如冲突请修改配置
- 确认MCP服务器端口未被占用

## 日志查看

- **应用日志**：控制台输出
- **MCP服务器日志**：`back/mcp_shopping_server.log`
- **数据库操作日志**：在MCP日志中查看SQL执行记录

## 开发建议

1. **本地开发**：建议使用本地Ollama模型，避免API调用限制
2. **生产环境**：推荐使用通义千问或DeepSeek等云端模型
3. **数据库优化**：为搜索字段添加索引提高查询性能
4. **缓存策略**：考虑添加Redis缓存热门搜索结果
5. **监控告警**：添加MCP服务器健康检查和自动重启机制

## 部署注意事项

1. **环境变量**：生产环境务必通过安全方式管理API密钥
2. **数据库安全**：修改默认密码，限制网络访问
3. **防火墙配置**：开放必要端口，关闭调试接口
4. **SSL证书**：生产环境启用HTTPS
5. **备份策略**：定期备份数据库和配置文件