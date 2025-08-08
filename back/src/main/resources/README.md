## 环境准备
### python 环境配置
选择合适的python解释器环境
```bash
# 安装Python依赖
pip install -r requirements.txt
```

### 数据库配置
在MCP服务器(mcp_shopping_server.py)中修改数据库连接：
```python
# 在 mcp_shopping_server.py 中修改
self.db_config = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': 'your_actual_password',  # 修改为你的密码
    'database': 'db_second_hand_trading',
    'charset': 'utf8mb4',
    'autocommit': True
}
```

## 相关测试

```bash
python mcp_shopping_server.py
```

在终端输入MCP服务器的JSON-RPC语句，在back/mcp_shopping_server.log中相应查看输出，查看MCP服务器是否正常

### 1. 初始化连接

```json
{"jsonrpc": "2.0", "id": 1, "method": "initialize", "params": {"protocolVersion": "1.0.0", "capabilities": {}, "clientInfo": {"name": "test-client", "version": "1.0.0"}}}
```

### 2. 发送initialized通知

```json
{"jsonrpc": "2.0", "method": "notifications/initialized"}
```

### 3. 列出可用工具

```json
{"jsonrpc": "2.0", "id": 2, "method": "tools/list"}
```

### 4. 智能搜索测试

#### 基本关键词搜索

```json
{"jsonrpc": "2.0", "id": 3, "method": "tools/call", "params": {"name": "smart_search", "arguments": {"query": "手机"}}}
```

#### 带价格范围的搜索

```json
{"jsonrpc": "2.0", "id": 4, "method": "tools/call", "params": {"name": "smart_search", "arguments": {"query": "手机", "min_price": 1000, "max_price": 5000}}}
```

#### 分类+关键词搜索

```json
{"jsonrpc": "2.0", "id": 5, "method": "tools/call", "params": {"name": "smart_search", "arguments": {"query": "苹果", "category": "数码", "sort_by": "price_low"}}}
```

#### 按相关度排序搜索

```json
{"jsonrpc": "2.0", "id": 6, "method": "tools/call", "params": {"name": "smart_search", "arguments": {"query": "iPhone", "sort_by": "relevance", "limit": 5}}}
```

### 5. 分类浏览测试

#### 浏览数码产品

```json
{"jsonrpc": "2.0", "id": 7, "method": "tools/call", "params": {"name": "browse_categories", "arguments": {"category": "数码", "limit": 5}}}
```

#### 浏览家电产品

```json
{"jsonrpc": "2.0", "id": 8, "method": "tools/call", "params": {"name": "browse_categories", "arguments": {"category": "家电"}}}
```

### 6. 热门商品测试

```json
{"jsonrpc": "2.0", "id": 9, "method": "tools/call", "params": {"name": "get_popular_items", "arguments": {"limit": 10}}}
```

### 7. 标签分析测试

```json
{"jsonrpc": "2.0", "id": 10, "method": "tools/call", "params": {"name": "analyze_tags", "arguments": {"limit": 15}}}
```

### 8. 复杂搜索场景

#### 搜索便宜的户外用品

```json
{"jsonrpc": "2.0", "id": 11, "method": "tools/call", "params": {"name": "smart_search", "arguments": {"query": "户外", "max_price": 500, "sort_by": "price_low"}}}
```

#### 搜索高价值数码产品

```json
{"jsonrpc": "2.0", "id": 12, "method": "tools/call", "params": {"name": "smart_search", "arguments": {"category": "数码", "min_price": 3000, "sort_by": "price_high"}}}
```

#### 搜索特定标签

```json
{"jsonrpc": "2.0", "id": 13, "method": "tools/call", "params": {"name": "smart_search", "arguments": {"query": "说唱"}}}
```


### 1. 智能搜索功能

- **模糊匹配**：支持在商品名称、描述、标签中进行模糊搜索
- **相关性评分**：根据匹配位置给出评分（商品名 > 标签 > 描述）
- **智能分类识别**：输入"手机"会自动识别为数码分类

### 2. 标签系统增强

- **自动解析**：`#哈哈#呵呵#测试` → `['哈哈', '呵呵', '测试']`
- **标签统计**：`analyze_tags` 工具分析所有标签使用情况
- **标签搜索**：搜索时会匹配标签内容

### 3. 分类系统完善

- **分类映射**：`idle_label` 1-5 对应 数码、家电、户外、图书、其他
- **智能分类**：输入分类关键词自动匹配到正确分类ID
- **分类浏览**：`browse_categories` 工具按分类浏览商品

### 4. 多种排序方式

- **时间排序**：最新发布优先
- **相关度排序**：搜索时按匹配度排序
- **价格排序**：低到高或高到低
- **热门排序**：按浏览次数排序

### 5. 用户友好的输出

- 格式化显示商品信息
- 显示分类名称而非ID
- 解析并显示标签
- 显示相关度评分

