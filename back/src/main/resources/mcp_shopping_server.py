#!/usr/bin/env python3
"""
智能二手交易平台MCP服务器 (MCP 1.12.3版本)
提供智能搜索、分类理解、标签解析等高级功能
"""

import asyncio
import json
import logging
import mysql.connector
import re
from typing import Any, Dict, List, Optional, Sequence

# MCP 1.12.3版本的导入
from mcp import ClientSession, StdioServerParameters
from mcp.server import Server
from mcp.server.models import InitializationOptions
from mcp.server.stdio import stdio_server
from mcp.types import (
    Resource, 
    Tool, 
    TextContent, 
    ImageContent, 
    EmbeddedResource,
    LoggingLevel,
    ServerCapabilities,
)

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    filename='mcp_shopping_server.log',
    filemode='a',
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class SmartShoppingMCPServer:
    def __init__(self):
        self.db_config = {
            'host': 'localhost',
            'port': 3306,
            'user': 'root',
            'password': 'lueluelue',  # 请修改为你的数据库密码
            'database': 'db_second_hand_trading',  # 请修改为你的数据库名
            'charset': 'utf8mb4',
            'autocommit': True
        }

        # 分类映射
        self.category_map = {
            1: '数码', 2: '家电', 3: '户外', 4: '图书', 5: '其他'
        }
        
        # 反向分类映射（支持模糊匹配）
        self.category_keywords = {
            '数码': ['手机', '电脑', '平板', '相机', '数码', '电子', '笔记本', 'iPhone', 'iPad', '华为', '小米', '电子产品'],
            '家电': ['冰箱', '洗衣机', '空调', '电视', '微波炉', '家电', '电器', '厨电'],
            '户外': ['帐篷', '登山', '户外', '运动', '健身', '自行车', '滑板', '球类'],
            '图书': ['书', '图书', '教材', '小说', '漫画', '杂志', '文学'],
            '其他': ['其他', '杂项']
        }

    def _get_db_connection(self):
        """获取数据库连接"""
        try:
            return mysql.connector.connect(**self.db_config)
        except mysql.connector.Error as e:
            logger.error(f"数据库连接失败: {e}")
            raise

    def _parse_tags(self, tag_string: str) -> List[str]:
        """解析标签字符串 #tag1#tag2#tag3 -> [tag1, tag2, tag3]"""
        if not tag_string:
            return []
        
        # 使用正则提取#包围的标签
        tags = re.findall(r'#([^#]+)', tag_string)
        return [tag.strip() for tag in tags if tag.strip()]

    def _guess_category_from_text(self, text: str) -> Optional[int]:
        """根据文本内容猜测分类"""
        text_lower = text.lower()
        
        for category_id, category_name in self.category_map.items():
            keywords = self.category_keywords.get(category_name, [])
            for keyword in keywords:
                if keyword.lower() in text_lower:
                    return category_id
        return None

    def _build_smart_search_query(self, 
                                keyword: str = "", 
                                category: str = "",
                                min_price: Optional[float] = None,
                                max_price: Optional[float] = None,
                                sort_by: str = "time",
                                limit: int = 10) -> tuple:
        """构建智能搜索查询"""
        
        sql_parts = ["SELECT *, CASE"]
        
        # 添加相关性评分
        relevance_parts = []
        params = []
        
        if keyword:
            # 商品名称匹配（权重最高）
            relevance_parts.append("WHEN idle_name LIKE %s THEN 10")
            params.append(f"%{keyword}%")
            
            # 详情描述匹配
            relevance_parts.append("WHEN idle_details LIKE %s THEN 5")  
            params.append(f"%{keyword}%")
            
            # 标签匹配
            relevance_parts.append("WHEN tag LIKE %s THEN 8")
            params.append(f"%{keyword}%")
        
        relevance_parts.append("ELSE 0 END as relevance_score")
        sql_parts[0] += " " + " ".join(relevance_parts)
        sql_parts.append("FROM sh_idle_item WHERE idle_status = 1")
        
        # 关键词搜索条件
        if keyword:
            sql_parts.append("AND (idle_name LIKE %s OR idle_details LIKE %s OR tag LIKE %s)")
            keyword_pattern = f"%{keyword}%"
            params.extend([keyword_pattern, keyword_pattern, keyword_pattern])
        
        # 分类搜索
        if category:
            # 尝试通过名称找到分类ID
            category_id = None
            for cid, cname in self.category_map.items():
                if category.lower() in cname.lower() or cname.lower() in category.lower():
                    category_id = cid
                    break
            
            if category_id:
                if not category_id == 5:
                    sql_parts.append("AND idle_label = %s")
                    params.append(category_id)
            else:
                # 如果没找到精确分类，尝试关键词匹配
                guessed_category = self._guess_category_from_text(category)
                if guessed_category:
                    sql_parts.append("AND idle_label = %s")
                    params.append(guessed_category)
        
        # 价格范围
        if min_price is not None:
            sql_parts.append("AND idle_price >= %s")
            params.append(min_price)
            
        if max_price is not None:
            sql_parts.append("AND idle_price <= %s")
            params.append(max_price)
        
        # 排序
        if sort_by == "price_low":
            sql_parts.append("ORDER BY idle_price ASC")
        elif sort_by == "price_high":
            sql_parts.append("ORDER BY idle_price DESC")
        elif sort_by == "popular":
            sql_parts.append("ORDER BY skim_count DESC")
        elif sort_by == "relevance" and keyword:
            sql_parts.append("ORDER BY relevance_score DESC, release_time DESC")
        else:  # time
            sql_parts.append("ORDER BY release_time DESC")
            
        sql_parts.append("LIMIT %s")
        params.append(limit)
        
        return " ".join(sql_parts), params

    def _format_product_result(self, products: List[Dict]) -> str:
        """格式化商品结果，包含智能信息解析"""
        if not products:
            return "没有找到相关商品。"
        
        result_lines = [f"找到 {len(products)} 个商品:\n"]
        
        for i, product in enumerate(products, 1):
            # 解析标签
            tags = self._parse_tags(product.get('tag', ''))
            tag_display = f" 🏷️ {', '.join(tags)}" if tags else ""
            
            # 获取分类名称
            category_name = self.category_map.get(product.get('idle_label'), '未知')
            
            # 相关性评分
            relevance = product.get('relevance_score', 0)
            relevance_display = f" (相关度: {relevance})" if relevance > 0 else ""
            
            result_lines.append(
                f"{i}. 【{product['idle_name']}】\n"
                f"   ID: {product['id']}\n"
                f"   💰 价格: ¥{product['idle_price']}\n"
                f"   📍 地区: {product['idle_place']}\n"
                f"   📂 分类: {category_name}\n"
                f"   👀 浏览: {product.get('skim_count', 0)}次\n"
                f"   🕒 发布: {product['release_time']}\n"
                f"   📝 描述: {product['idle_details'][:100]}{'...' if len(product['idle_details']) > 100 else ''}\n"
                f"{tag_display}{relevance_display}\n"
            )
        
        return "\n".join(result_lines)

    def _execute_query(self, sql: str, params: Optional[List] = None) -> List[Dict[str, Any]]:
        """执行SQL查询并返回结果"""
        try:
            conn = self._get_db_connection()
            cursor = conn.cursor(dictionary=True)

            if params:
                cursor.execute(sql, params)
            else:
                cursor.execute(sql)

            results = cursor.fetchall()

            # 处理特殊数据类型
            for row in results:
                for key, value in row.items():
                    if isinstance(value, bytes):
                        row[key] = value.decode('utf-8', errors='ignore')
                    elif hasattr(value, 'isoformat'):
                        row[key] = value.isoformat()

            cursor.close()
            conn.close()
            return results

        except mysql.connector.Error as e:
            logger.error(f"SQL执行失败: {sql}, 错误: {e}")
            raise Exception(f"查询执行失败: {str(e)}")

def create_server() -> Server:
    """创建智能MCP服务器"""
    server = Server("smart-shopping-assistant")
    shopping_server = SmartShoppingMCPServer()

    @server.list_tools()
    async def handle_list_tools() -> list[Tool]:
        """列出可用的工具"""
        return [
            Tool(
                name="smart_search",
                description="智能商品搜索，支持模糊匹配、分类识别、标签解析、相关度排序等功能",
                inputSchema={
                    "type": "object",
                    "properties": {
                        "query": {
                            "type": "string",
                            "description": "搜索查询，可以是商品名称、描述关键词、分类名称等"
                        },
                        "category": {
                            "type": ["string", "null"], # 允许 category 为 null
                            "description": "商品分类：数码、家电、户外、图书、其他，或相关关键词"
                        },
                        "min_price": {
                            "type": ["number", "null"], # 关键改动：允许 min_price 为 null
                            "description": "最低价格"
                        },
                        "max_price": {
                            "type": ["number", "null"], # 关键改动：允许 max_price 为 null
                            "description": "最高价格"
                        },
                        "sort_by": {
                            "type": ["string", "null"], # 允许 sort_by 为 null
                            "enum": ["time", "price_low", "price_high", "popular", "relevance"],
                            "description": "排序方式：时间、价格低到高、价格高到低、热门、相关度"
                        },
                        "limit": {
                            "type": ["integer", "null"], # 关键改动：允许 limit 为 null
                            "description": "结果数量限制", 
                            "default": 10
                        }
                    },
                    "required": ["query"],
                    "additionalProperties": False
                }
            ),
            Tool(
                name="browse_categories",
                description="浏览各分类下的商品",
                inputSchema={
                    "type": "object", 
                    "properties": {
                        "category": {
                            "type": "string",
                            "enum": ["数码", "家电", "户外", "图书", "其他"],
                            "description": "要浏览的商品分类"
                        },
                        "limit": {
                            "type": ["integer", "null"], # 建议同样允许 null
                            "description": "结果数量", 
                            "default": 10
                        }
                    },
                    "required": ["category"],
                    "additionalProperties": False
                }
            ),
            Tool(
                name="get_popular_items",
                description="获取热门商品（按浏览次数排序）",
                inputSchema={
                    "type": "object",
                    "properties": {
                        "limit": {
                            "type": ["integer", "null"], # 建议同样允许 null
                            "description": "结果数量", 
                            "default": 10
                        }
                    },
                    "additionalProperties": False
                }
            ),
            Tool(
                name="analyze_tags",
                description="分析系统中的所有标签，了解商品标签分布",
                inputSchema={
                    "type": "object",
                    "properties": {
                        "limit": {
                            "type": ["integer", "null"], # 建议同样允许 null
                            "description": "返回最热门的标签数量", 
                            "default": 20
                        }
                    },
                    "additionalProperties": False
                }
            )
        ]

    @server.call_tool()
    async def handle_call_tool(name: str, arguments: dict | None) -> list[TextContent | ImageContent | EmbeddedResource]:
        """处理工具调用"""
        if arguments is None:
            arguments = {}

        try:
            if name == "smart_search":
                query = arguments.get("query", "")
                category = arguments.get("category", "")
                min_price = arguments.get("min_price")
                max_price = arguments.get("max_price")
                sort_by = arguments.get("sort_by", "relevance" if query else "time")
                limit = arguments.get("limit", 10)

                sql, params = shopping_server._build_smart_search_query(
                    keyword=query, category=category, min_price=min_price,
                    max_price=max_price, sort_by=sort_by, limit=limit
                )
                
                results = shopping_server._execute_query(sql, params)
                formatted_result = shopping_server._format_product_result(results)
                
                return [TextContent(type="text", text=formatted_result)]

            elif name == "browse_categories":
                category_name = arguments.get("category", "")
                limit = arguments.get("limit", 10)
                
                # 找到分类ID
                category_id = None
                for cid, cname in shopping_server.category_map.items():
                    if cname == category_name:
                        category_id = cid
                        break
                
                if not category_id:
                    return [TextContent(type="text", text=f"未找到分类：{category_name}")]
                
                sql = "SELECT * FROM sh_idle_item WHERE idle_status = 1 AND idle_label = %s ORDER BY release_time DESC LIMIT %s"
                results = shopping_server._execute_query(sql, [category_id, limit])
                formatted_result = shopping_server._format_product_result(results)
                
                return [TextContent(type="text", text=f"【{category_name}】分类商品:\n\n{formatted_result}")]

            elif name == "get_popular_items":
                limit = arguments.get("limit", 10)
                
                sql = "SELECT * FROM sh_idle_item WHERE idle_status = 1 ORDER BY skim_count DESC LIMIT %s"
                results = shopping_server._execute_query(sql, [limit])
                formatted_result = shopping_server._format_product_result(results)
                
                return [TextContent(type="text", text=f"热门商品 Top {limit}:\n\n{formatted_result}")]

            elif name == "analyze_tags":
                limit = arguments.get("limit", 20)
                
                # 获取所有标签并统计
                sql = "SELECT tag FROM sh_idle_item WHERE idle_status = 1 AND tag IS NOT NULL AND tag != ''"
                results = shopping_server._execute_query(sql)
                
                tag_count = {}
                for row in results:
                    tags = shopping_server._parse_tags(row['tag'])
                    for tag in tags:
                        tag_count[tag] = tag_count.get(tag, 0) + 1
                
                # 排序并取前N个
                sorted_tags = sorted(tag_count.items(), key=lambda x: x[1], reverse=True)[:limit]
                
                result_text = f"标签分析 (共 {len(tag_count)} 个不同标签):\n\n"
                for i, (tag, count) in enumerate(sorted_tags, 1):
                    result_text += f"{i:2d}. {tag} ({count}个商品)\n"
                
                return [TextContent(type="text", text=result_text)]

            else:
                raise ValueError(f"未知工具: {name}")

        except Exception as e:
            error_msg = f"工具调用失败: {str(e)}"
            logger.error(error_msg)
            raise RuntimeError(error_msg)

    return server

async def main():
    """启动智能MCP服务器"""
    try:
        server = create_server()
        logger.info("启动智能二手交易平台MCP服务器...")

        async with stdio_server() as streams:
            await server.run(
                streams[0], streams[1], 
                InitializationOptions(
                    server_name="smart-second-hand-shopping-mcp-server",
                    server_version="2.0.0",
                    capabilities=ServerCapabilities(tools={})
                )
            )

    except KeyboardInterrupt:
        logger.info("服务器被用户中断")
    except Exception as e:
        logger.error(f"服务器启动失败: {e}")
        raise

if __name__ == "__main__":
    asyncio.run(main())