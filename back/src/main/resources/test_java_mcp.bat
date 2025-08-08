@echo off
chcp 65001 >nul
@REM CMD命令行:
@REM 模拟前端发送会话请求，验证java-mcp - mcp server 链路是否正常
echo 正在发送请求到 AI Agent...

curl -X POST http://localhost:8080/ai-agent/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"sessionId\": \"1234\", \"message\": \"给我推荐一些机器人\", \"userId\": 12345}"



echo.
echo 请求已完成。请查看上方输出结果。
echo 如果没有自动显示结果，请按任意键查看。
pause >nul