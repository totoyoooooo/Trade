<template>
    <div class="ai-assistant-container">
        <!-- 浮动按钮 -->
        <div v-if="!isOpen" class="floating-button" @click="toggleAssistant">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-4l-4 4z" />
            </svg>
            <span class="assistant-label">AI导购</span>
        </div>

        <!-- 悬浮窗主体 -->
        <div v-if="isOpen" class="floating-window" :class="{ 'window-minimized': isMinimized }">
            <!-- 窗口头部 -->
            <div class="window-header">
                <div class="header-left">
                    <div class="ai-avatar">
                        <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.58L19 8l-9 9z"/>
                        </svg>
                    </div>
                    <span class="assistant-title">AI购物助手</span>
                    <!-- 流式模式指示器 -->
                    <div v-if="isStreamMode" class="stream-indicator" title="流式对话模式">
                        <div class="stream-dot"></div>
                        <span class="stream-text">LIVE</span>
                    </div>
                </div>
                <div class="header-actions">
                    <!-- 模式切换 -->
                    <button @click="toggleStreamMode" class="action-btn" :title="isStreamMode ? '切换到普通模式' : '切换到流式模式'">
                        <svg v-if="isStreamMode" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                        </svg>
                        <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                    </button>
                    <!-- 停止流式对话 -->
                    <button v-if="isLoading && isStreamMode" @click="stopStreaming" class="action-btn stop-btn" title="停止对话">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 10h6v4H9z" />
                        </svg>
                    </button>
                    <!-- 新建聊天 -->
                    <button @click="startNewChat" class="action-btn" title="新聊天">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                        </svg>
                    </button>
                    <button @click="toggleMinimize" class="action-btn" title="最小化">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4"/>
                        </svg>
                    </button>
                    <button @click="toggleAssistant" class="action-btn" title="关闭">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                    </button>
                </div>
            </div>

            <!-- 聊天内容 -->
            <div v-if="!isMinimized" class="chat-content">
                <div class="messages-container" ref="messagesContainer">
                    <div
                        v-for="(message, index) in messages"
                        :key="index"
                        class="message"
                        :class="{
                            'user-message': message.isUser,
                            'ai-message': !message.isUser,
                            'streaming-message': message.isStreaming
                        }"
                    >
                        <div class="message-content">
                            <!-- 流式对话指示器 -->
                            <div v-if="message.isStreaming" class="streaming-indicator">
                                <div class="typing-dots">
                                    <span></span><span></span><span></span>
                                </div>
                                <span class="streaming-text">正在回复中...</span>
                            </div>

                            <p v-html="formatMessage(message.text)"></p>

                            <!-- 商品卡片 -->
                            <div v-if="message.productCards && message.productCards.length > 0" class="product-cards">
                                <div
                                    v-for="product in message.productCards"
                                    :key="product.id"
                                    class="product-card"
                                >
                                    <img
                                        :src="product.image"
                                        :alt="product.name"
                                        class="product-image"
                                        loading="lazy"
                                    />
                                    <div class="product-info">
                                        <h4 class="product-name">{{ product.name }}</h4>
                                        <div class="product-details">
                                            <span class="product-price">¥{{ product.price }}</span>
                                            <span class="product-status" :class="product.status">
                                                {{ product.statusText }}
                                            </span>
                                        </div>
                                        <div class="product-meta">
                                            <span class="product-category">{{ product.category }}</span>
                                            <span class="product-views">{{ product.views }}次浏览</span>
                                            <span v-if="product.stock" class="product-stock">库存{{ product.stock }}</span>
                                        </div>
                                        <div v-if="product.sellerName" class="product-seller">
                                            卖家：{{ product.sellerName }}
                                        </div>
                                        <button
                                            v-if="product.canPurchase"
                                            class="purchase-btn"
                                            @click="handlePurchase(product)"
                                        >
                                            立即购买
                                        </button>
                                        <button v-else class="purchase-btn disabled" disabled>
                                            {{ product.disabledReason || '暂不可购买' }}
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="message-time">{{ formatTime(message.timestamp) }}</div>
                    </div>

                    <!-- 传统的打字指示器（非流式模式） -->
                    <div v-if="isLoading && !isStreamMode" class="message ai-message">
                        <div class="message-content">
                            <div class="typing-indicator">
                                <span></span><span></span><span></span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 输入框 -->
                <div class="input-area">
                    <div class="input-container">
                        <input
                            v-model="inputMessage"
                            type="text"
                            placeholder="描述您想要的商品..."
                            class="message-input"
                            @keyup.enter="sendMessage"
                            :disabled="isLoading"
                        />
                        <button
                            @click="sendMessage"
                            class="send-btn"
                            :disabled="isLoading || !inputMessage.trim()"
                        >
                            <svg v-if="!isLoading" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"/>
                            </svg>
                            <div v-else class="loading-spinner"></div>
                        </button>
                    </div>
                    <!-- 模式提示 -->
                    <div class="mode-hint">
                        {{ isStreamMode ? '🚀 流式对话模式 - 实时响应' : '💬 标准模式 - 完整回复' }}
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'AIShoppingAssistant',
    data() {
        return {
            isOpen: false,
            isMinimized: false,
            isLoading: false,
            inputMessage: '',
            messages: [],
            sessionId: '',
            userId: 1,
            // 流式对话相关状态
            isStreamMode: true, // 启用流式模式
            currentStreamMessage: '', // 当前流式接收的消息
            eventSource: null, // SSE连接
            streamingMessageId: null, // 当前流式消息的ID
            // productDetectionInterval: null, // 定时检测间隔ID
        }
    },
    mounted() {
        this.sessionId = this.generateSessionId()
        this.addWelcomeMessage()
    },
    methods: {
        generateSessionId() {
            return 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
        },
        addWelcomeMessage() {
            this.messages.push({
                text: '您好！我是您的AI购物助手，请告诉我您想要什么商品，我来为您推荐！',
                isUser: false,
                timestamp: new Date(),
                productCards: null
            })
        },
        startNewChat() {
            this.cleanupStream()
            this.sessionId = this.generateSessionId()
            this.messages = []
            this.addWelcomeMessage()
            this.$nextTick(this.scrollToBottom)
        },
        toggleAssistant() {
            if (this.isOpen) {
                this.cleanupStream()
            }
            this.isOpen = !this.isOpen
            if (this.isOpen) {
                this.isMinimized = false
                this.$nextTick(this.scrollToBottom)
            }
        },
        toggleMinimize() {
            this.isMinimized = !this.isMinimized
        },
        scrollToBottom() {
            if (this.$refs.messagesContainer) {
                this.$refs.messagesContainer.scrollTop = this.$refs.messagesContainer.scrollHeight
            }
        },

        processProductsForCard(products) {
            if (!products || products.length === 0) return []
            return products.map(product => {
                let imageUrl = '/api/placeholder/120/120'
                try {
                    if (product.pictureList) {
                        const pictures = JSON.parse(product.pictureList)
                        if (pictures.length > 0) imageUrl = pictures[0]
                    }
                } catch (e) {
                    console.error(`解析商品图片失败 (ID: ${product.id}):`, e)
                }
                const price = product.idlePrice || product.price || 0
                return {
                    id: product.id,
                    name: product.idleName || product.name,
                    price: price,
                    category: product.idleTag || product.category || '',
                    views: product.skimCount || product.views || 0,
                    image: imageUrl,
                    status: product.idleStatus === 1 ? 'available' : 'non-sellable',
                    statusText: product.idleStatus === 1 ? '可购买' : '已售出',
                    canPurchase: product.idleStatus === 1,
                    disabledReason: product.idleStatus !== 1 ? '已售出' : '',
                    sellerName: (product.user && product.user.username) ? product.user.username : '',
                    stock: product.idleStatus === 1 ? 1 : 0
                }
            })
        },
        processAIResponse(responseData) {
            if (!responseData || !responseData.message) {
                return { cleanedMessage: '', productCardsForMessage: [] }
            }
            let finalMessage = responseData.message
            const finalProductCards = []
            const products = responseData.products || []
            const productMap = new Map(products.map(p => [p.id, p]))

            const regex = /\[([^\]]+)\]\((\d+)\)/g
            let match
            const replacements = []
            while ((match = regex.exec(finalMessage)) !== null) {
                replacements.push({
                    fullMatch: match[0],
                    text: match[1],
                    id: parseInt(match[2], 10)
                })
            }
            for (let i = replacements.length - 1; i >= 0; i--) {
                const { fullMatch, text, id } = replacements[i]
                if (id === 0) {
                    finalMessage = finalMessage.replace(fullMatch, text)
                } else if (productMap.has(id)) {
                    const product = productMap.get(id)
                    if (!finalProductCards.some(p => p.id === id)) {
                        finalProductCards.unshift(product)
                    }
                    finalMessage = finalMessage.replace(fullMatch, text)
                } else {
                    finalMessage = finalMessage.replace(fullMatch, text)
                }
            }
            return { cleanedMessage: finalMessage, productCardsForMessage: finalProductCards }
        },
        /**
         * 发送消息 - 支持流式和非流式模式
         */
        async sendMessage() {
            if (!this.inputMessage.trim() || this.isLoading) return
            const userMessage = this.inputMessage.trim()

            // 添加用户消息
            this.messages.push({
                text: userMessage,
                isUser: true,
                timestamp: new Date(),
                productCards: null
            })
            this.inputMessage = ''
            this.isLoading = true
            this.$nextTick(this.scrollToBottom)

            try {
                if (this.isStreamMode) {
                    await this.sendStreamMessage(userMessage)
                } else {
                    await this.sendRegularMessage(userMessage)
                }
            } catch (error) {
                this.handleMessageError(error)
            } finally {
                this.isLoading = false
                this.$nextTick(this.scrollToBottom)
            }
        },
        /**
         * 流式发送消息 (SSE)
         */
        async sendStreamMessage(userMessage) {
            const streamMessageId = Date.now();
            this.streamingMessageId = streamMessageId;
            const aiMessage = {
                id: streamMessageId,
                text: '',
                isUser: false,
                timestamp: new Date(),
                productCards: null,
                isStreaming: true
            };
            this.messages.push(aiMessage);
            this.$nextTick(this.scrollToBottom);

            try {
                console.log('开始流式请求...');

                const response = await fetch('/api/ai-agent/chat/stream', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'text/event-stream',
                        'Cache-Control': 'no-cache'
                    },
                    body: JSON.stringify({
                        sessionId: this.sessionId,
                        message: userMessage,
                        userId: this.userId
                    })
                });

                if (!response.ok) {
                    throw new Error(`服务器错误: ${response.status} ${response.statusText}`);
                }

                console.log('开始读取流式响应...');

                const reader = response.body.getReader();
                const decoder = new TextDecoder('utf-8');
                let buffer = '';

                while (true) {
                    const { done, value } = await reader.read();
                    if (done) {
                        console.log('流式响应读取完成');
                        // 处理缓冲区中剩余的数据
                        if (buffer.trim()) {
                            this.processRemainingBuffer(buffer, aiMessage);
                        }
                        break;
                    }

                    buffer += decoder.decode(value, { stream: true });

                    // 现在可以安全地用双换行符分割，因为内容中的换行符已被编码
                    const messages = buffer.split('\n\n');

                    // 处理所有完整的消息（除了最后一个可能不完整的）
                    for (let i = 0; i < messages.length - 1; i++) {
                        const messageBlock = messages[i].trim();
                        if (messageBlock) {
                            this.parseSSEMessage(messageBlock, aiMessage);
                        }
                    }

                    // 保留最后一个可能不完整的消息
                    buffer = messages[messages.length - 1];
                }

                console.log('流式请求处理完成');

            } catch (error) {
                console.error('Fetch Stream 错误:', error);
                aiMessage.text = aiMessage.text || '请求失败，请检查网络或联系管理员。';
                this.handleMessageError(error);
            } finally {
                this.isLoading = false;
                aiMessage.isStreaming = false;
                this.streamingMessageId = null;
                this.$nextTick(this.scrollToBottom);
            }
        },
        /**
         * 常规非流式发送消息
         */
        async sendRegularMessage(userMessage) {
            const response = await this.$api.callShoppingAIAgent({
                sessionId: this.sessionId,
                message: userMessage,
                userId: this.userId
            })

            if (!response.data || typeof response.data !== 'object') {
                throw new Error('响应格式错误')
            }

            const responseData = response.data.data || response.data
            const { cleanedMessage, productCardsForMessage } = this.processAIResponse(responseData)

            this.messages.push({
                text: cleanedMessage || '收到响应，但内容为空',
                isUser: false,
                timestamp: new Date(),
                productCards: this.processProductsForCard(productCardsForMessage)
            })
        },
        /**
         * 解析单个SSE消息块
         */
        parseSSEMessage(messageBlock, aiMessage) {
            const lines = messageBlock.split('\n');
            let event = 'message';
            let dataLines = [];

            for (const line of lines) {
                if (line.startsWith('event:')) {
                    event = line.substring(6).trim();
                } else if (line.startsWith('data:')) {
                    // 收集所有data行
                    dataLines.push(line.substring(5));
                }
                // 忽略注释行和其他字段
            }

            // 合并所有data行并解码换行符
            if (dataLines.length > 0) {
                const rawData = dataLines.join('\n');
                const decodedData = this.decodeNewlines(rawData);
                this.handleStreamEvent(aiMessage, event, decodedData);
            }
        },
        /**
         * 处理剩余缓冲区数据
         */
        processRemainingBuffer(buffer, aiMessage) {
            // 如果缓冲区包含SSE字段，尝试解析
            if (buffer.includes('event:') || buffer.includes('data:')) {
                this.parseSSEMessage(buffer, aiMessage);
            }
        },
        /**
         * 解码换行符
         */
        decodeNewlines(data) {
            if (!data) return data;

            return data
                .replace(/<<CRLF>>/g, '\r\n')
                .replace(/<<LF>>/g, '\n')
                .replace(/<<CR>>/g, '\r');
        },
        /**
         * 事件处理函数
         */
        handleStreamEvent(aiMessage, eventType, data) {
            console.log(`处理事件: ${eventType}`, data);

            switch (eventType) {
                case 'start':
                    aiMessage.text = '正在思考...';
                    break;

                case 'thinking':
                    aiMessage.text = `🤔 ${data}`;
                    break;

                case 'content':
                    if (aiMessage.text === '正在思考...' ||
                        aiMessage.text.startsWith('🔧') ||
                        aiMessage.text.startsWith('🤔')) {
                        aiMessage.text = '';
                    }
                    // 现在data已经被正确解码，包含原始的换行符
                    aiMessage.text += data;
                    break;

                case 'tool':
                    aiMessage.text += `\n\n🔧 ${data}`;
                    break;

                case 'tool_result':
                    aiMessage.text += `\n✅ ${data}`;
                    break;

                case 'products':
                    try {
                        const products = JSON.parse(data);
                        aiMessage.productCards = this.processProductsForCard(products);
                        console.log('商品卡片数据已更新:', aiMessage.productCards);
                    } catch (e) {
                        console.error('解析商品数据失败:', e, 'Original data:', data);
                    }
                    break;

                case 'complete':
                    console.log('对话完成');
                    break;

                case 'error':
                    console.error('收到错误事件:', data);
                    aiMessage.text = data || '抱歉，AI助手暂时不可用';
                    break;

                default:
                    console.log(`未知事件类型: ${eventType}`, data);
            }

            this.$nextTick(this.scrollToBottom);
        },
        handlePurchase(product) {
            console.log('查看商品详情:', product)
            this.$router.replace({ path: '/details', query: { id: product.id } });
        },
        formatTime(timestamp) {
            const now = new Date()
            const time = new Date(timestamp)
            const diffMs = now - time
            const diffMins = Math.floor(diffMs / 60000)
            if (diffMins < 1) return '刚刚'
            if (diffMins < 60) return `${diffMins}分钟前`
            if (diffMins < 1440) return `${Math.floor(diffMins / 60)}小时前`
            return time.toLocaleDateString()
        },
        // formatMessage(text) {
        //     if (!text) return '';
        //     let formattedText = text;
        //
        //     // 将 Markdown 的 **text** 转换为 <strong>text</strong>
        //     formattedText = formattedText.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
        //     // 将换行符 \n 转换为 <br>
        //     formattedText = formattedText.replace(/\n/g, '<br>');
        //
        //     return formattedText
        // },
        formatMessage(text) {
            if (!text) return '';
            let formattedText = text;

            // 1. 将完整的 [商品名称](ID) 转换为 商品名称
            formattedText = formattedText.replace(/\[([^\]]*?)\]\s*\(\s*(\d+)\s*\)/g, '【$1】');

            // 2. 清理残缺的商品链接格式
            // 清理孤立的 "](数字)" 格式
            // formattedText = formattedText.replace(/\]\s*\(\s*\d+\s*\)/g, '');

            // 3. 清理末尾的 "数字)" 格式（可能是被截断的）
            // formattedText = formattedText.replace(/\d+\)/g, '');

            // 4. 将 Markdown 的 **text** 转换为 <strong>text</strong>
            formattedText = formattedText.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

            // 5. 将换行符 \n 转换为 <br>
            formattedText = formattedText.replace(/\n/g, '<br>');

            // 6. 清理多余的空格和标点
            // formattedText = formattedText.replace(/\s{2,}/g, ' '); // 多个空格变一个
            // formattedText = formattedText.replace(/([。！？])\s*([。！？])/g, '$1$2'); // 清理重复标点
            // formattedText = formattedText.trim();

            return formattedText;
        },
        /**
         * 清理流式连接
         */
        cleanupStream() {
            if (this.eventSource) {
                this.eventSource.close()
                this.eventSource = null
            }
            this.streamingMessageId = null
            this.currentStreamMessage = ''
        },
        /**
         * 处理消息错误
         */
        handleMessageError(error) {
            console.error('发送消息失败:', error)
            let errorMessage = '抱歉，我暂时无法回答您的问题，请稍后再试。'

            if (error.response) {
                if (error.response.status === 404) {
                    errorMessage = '服务接口未找到，请检查后端服务。'
                } else if (error.response.status === 500) {
                    errorMessage = '服务器内部错误，请稍后再试。'
                } else if (error.response.data && error.response.data.message) {
                    errorMessage = error.response.data.message
                }
            } else if (error.request) {
                errorMessage = '网络连接失败，请检查您的网络。'
            } else if (error.code === 'ECONNABORTED') {
                errorMessage = '请求超时，请稍后再试。'
            }

            this.messages.push({
                text: errorMessage,
                isUser: false,
                timestamp: new Date(),
                productCards: null
            })
        },
        /**
         * 切换对话模式
         */
        toggleStreamMode() {
            this.isStreamMode = !this.isStreamMode
            console.log('切换到', this.isStreamMode ? '流式' : '普通', '对话模式')
        },

        /**
         * 停止当前的流式对话
         */
        stopStreaming() {
            if (this.isLoading && this.streamingMessageId) {
                this.cleanupStream()
                this.isLoading = false

                // 更新当前流式消息状态
                const streamMessage = this.messages.find(m => m.id === this.streamingMessageId)
                if (streamMessage) {
                    streamMessage.isStreaming = false
                    streamMessage.text += '\n\n[用户中止了对话]'
                }
            }
        },
    },
    // 组件销毁时清理资源
    beforeDestroy() {
        this.cleanupStream();
    }
}
</script>


<style scoped>
.ai-assistant-container {
    position: fixed;
    bottom: 20px;
    right: 20px;
    z-index: 1000;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 悬浮按钮 */
.floating-button {
    display: flex;
    align-items: center;
    gap: 8px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 12px 20px;
    border-radius: 50px;
    cursor: pointer;
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
    transition: all 0.3s ease;
    user-select: none;
}

.floating-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 30px rgba(102, 126, 234, 0.6);
}

.assistant-label {
    font-size: 14px;
    font-weight: 500;
}

/* 悬浮窗 */
.floating-window {
    width: 400px;
    height: 630px;
    background: white;
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    transition: all 0.3s ease;
    border: 1px solid rgba(0, 0, 0, 0.05);
}

.window-minimized {
    height: 50px;
}

/* 窗口头部 */
.window-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0px 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    min-height: 50px;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 8px;
}

.ai-avatar {
    width: 24px;
    height: 24px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.assistant-title {
    font-size: 14px;
    font-weight: 600;
}

.header-actions {
    display: flex;
    gap: 4px;
}

.action-btn {
    width: 28px;
    height: 28px;
    border: none;
    background: rgba(255, 255, 255, 0.1);
    color: white;
    border-radius: 6px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.2s;
}

.action-btn:hover {
    background: rgba(255, 255, 255, 0.2);
}

/* 聊天内容 */
.chat-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    height: calc(100% - 60px);
}

.messages-container {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.message {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.user-message {
    align-items: flex-end;
}

.ai-message {
    align-items: flex-start;
}

.message-content {
    max-width: 85%;
    min-height: 23px;
    padding: 1px 16px;
    border-radius: 18px;
    word-wrap: break-word;
}

.user-message .message-content {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.ai-message .message-content {
    background: #f1f3f4;
    color: #333;
}

.message-time {
    font-size: 11px;
    color: #999;
    padding: 0 16px;
}

/* 商品卡片容器 */
.product-cards {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 10px;
}

/* 商品卡片 */
.product-card {
    display: flex;
    gap: 12px;
    background: white;
    border: 1px solid #e1e5e9;
    border-radius: 12px;
    padding: 12px;
    margin-top: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.product-image {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 8px;
    background: #f5f5f5;
}

.product-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.product-name {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin: 0;
}

.product-details {
    display: flex;
    align-items: center;
    gap: 8px;
}

.product-price {
    font-size: 16px;
    font-weight: 700;
    color: #e74c3c;
}

.product-status {
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 12px;
    font-weight: 500;
}

.product-status.available {
    background: #d4edda;
    color: #155724;
}

.product-status.non-sellable {
    background: #f8d7da;
    color: #721c24;
}

.product-meta {
    display: flex;
    gap: 12px;
    font-size: 12px;
    color: #666;
    flex-wrap: wrap;
}

.product-seller {
    font-size: 12px;
    color: #888;
    margin-top: 4px;
}

.purchase-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 8px;
    font-size: 12px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    margin-top: auto;
}

.purchase-btn:hover:not(.disabled) {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.purchase-btn.disabled {
    background: #ccc;
    cursor: not-allowed;
}

/* 输入区域 */
.input-area {
    padding: 10px;
    border-top: 1px solid #e1e5e9;
    background: white;
}

.input-container {
    display: flex;
    gap: 8px;
    align-items: flex-end;
    margin-bottom: 2px;
}

.message-input {
    flex: 1;
    padding: 12px 16px;
    border: 1px solid #e1e5e9;
    border-radius: 24px;
    outline: none;
    font-size: 14px;
    resize: none;
    transition: border-color 0.2s;
}

.message-input:focus {
    border-color: #667eea;
}

.send-btn {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
}

.send-btn:hover:not(:disabled) {
    transform: scale(1.05);
}

.send-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

/* 打字指示器 */
.typing-indicator {
    display: flex;
    gap: 4px;
    align-items: center;
}

.typing-indicator span {
    width: 8px;
    height: 8px;
    background: #999;
    border-radius: 50%;
    animation: typing 1.4s ease-in-out infinite;
}

.typing-indicator span:nth-child(2) {
    animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
    animation-delay: 0.4s;
}

@keyframes typing {
    0%, 60%, 100% {
        transform: translateY(0);
        opacity: 0.5;
    }
    30% {
        transform: translateY(-10px);
        opacity: 1;
    }
}

/* 响应式适配 */
@media (max-width: 480px) {
    .floating-window {
        width: calc(100vw - 40px);
        height: calc(100vh - 100px);
    }

    .ai-assistant-container {
        bottom: 10px;
        right: 10px;
        left: 10px;
    }

    .floating-button {
        width: 100%;
        justify-content: center;
    }
}

/* 滚动条样式 */
.messages-container::-webkit-scrollbar {
    width: 4px;
}

.messages-container::-webkit-scrollbar-track {
    background: transparent;
}

.messages-container::-webkit-scrollbar-thumb {
    background: #ccc;
    border-radius: 4px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
    background: #999;
}

/* 新增商品高亮样式 */
.product-highlight {
    color: #667eea;
    font-weight: 600;
    text-decoration: underline;
    cursor: pointer;
}

.product-highlight:hover {
    color: #764ba2;
}

/* 流式模式指示器 */
.stream-indicator {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-left: 8px;
    padding: 2px 6px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 10px;
    font-size: 10px;
    font-weight: 500;
}

.stream-dot {
    width: 6px;
    height: 6px;
    background: #00ff88;
    border-radius: 50%;
    animation: pulse 2s infinite;
}

.stream-text {
    color: #00ff88;
    font-size: 9px;
    font-weight: 600;
}

@keyframes pulse {
    0%, 100% {
        opacity: 1;
        transform: scale(1);
    }
    50% {
        opacity: 0.5;
        transform: scale(0.8);
    }
}

/* 停止按钮样式 */
.stop-btn {
    background: rgba(255, 87, 87, 0.2) !important;
}

.stop-btn:hover {
    background: rgba(255, 87, 87, 0.3) !important;
}

/* 流式消息样式 */
.streaming-message .message-content {
    position: relative;
    background: linear-gradient(90deg, #f1f3f4 0%, #f8f9fa 50%, #f1f3f4 100%);
    background-size: 200% 100%;
    animation: shimmer 2s infinite;
    border-left: 3px solid #667eea;
}

@keyframes shimmer {
    0% {
        background-position: -200% 0;
    }
    100% {
        background-position: 200% 0;
    }
}

/* 流式指示器 */
.streaming-indicator {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    padding: 4px 0;
    font-size: 12px;
    color: #667eea;
    font-weight: 500;
}

.typing-dots {
    display: flex;
    gap: 3px;
    align-items: center;
}

.typing-dots span {
    width: 4px;
    height: 4px;
    background: #667eea;
    border-radius: 50%;
    animation: typing-dots 1.4s ease-in-out infinite;
}

.typing-dots span:nth-child(2) {
    animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
    animation-delay: 0.4s;
}

@keyframes typing-dots {
    0%, 60%, 100% {
        transform: translateY(0);
        opacity: 0.4;
    }
    30% {
        transform: translateY(-8px);
        opacity: 1;
    }
}

.streaming-text {
    color: #667eea;
    font-size: 11px;
}

/* 发送按钮加载状态 */
.loading-spinner {
    width: 16px;
    height: 16px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    border-top: 2px solid white;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

/* 模式提示 */
.mode-hint {
    font-size: 10px;
    color: #999;
    text-align: center;
    padding: 4px 0;
    background: rgba(102, 126, 234, 0.05);
    border-radius: 4px;
    margin-top: 4px;
}

/* 流式消息内容动画 */
.streaming-message .message-content p {
    position: relative;
}

.streaming-message .message-content p::after {
    content: '|';
    color: #667eea;
    animation: blink 1s infinite;
    margin-left: 2px;
}

@keyframes blink {
    0%, 50% { opacity: 1; }
    51%, 100% { opacity: 0; }
}

/* 工具调用状态显示 */
.tool-status {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 2px 8px;
    background: rgba(102, 126, 234, 0.1);
    border-radius: 12px;
    font-size: 11px;
    color: #667eea;
    margin: 4px 0;
}

.tool-status::before {
    content: '🔧';
    font-size: 10px;
}

/* 响应式适配 */
@media (max-width: 480px) {
    .stream-indicator {
        display: none; /* 小屏幕隐藏流式指示器 */
    }

    .header-actions {
        gap: 2px;
    }

    .action-btn {
        width: 24px;
        height: 24px;
    }

    .mode-hint {
        font-size: 9px;
        padding: 2px 0;
    }
}

/* 连接状态指示 */
.connection-status {
    position: absolute;
    top: -2px;
    right: -2px;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #00ff88;
    border: 1px solid white;
    box-shadow: 0 0 4px rgba(0, 255, 136, 0.5);
}

.connection-status.disconnected {
    background: #ff4757;
    box-shadow: 0 0 4px rgba(255, 71, 87, 0.5);
}

.connection-status.connecting {
    background: #ffa502;
    box-shadow: 0 0 4px rgba(255, 165, 2, 0.5);
    animation: pulse 1s infinite;
}

/* 改进的消息容器滚动 */
.messages-container {
    scroll-behavior: smooth;
}

/* 流式消息的渐入效果 */
.streaming-message {
    animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* 增强的按钮悬停效果 */
.action-btn {
    transition: all 0.2s ease;
    position: relative;
    overflow: hidden;
}

.action-btn:hover {
    transform: scale(1.05);
}

.action-btn:active {
    transform: scale(0.95);
}

/* 消息时间戳增强 */
.streaming-message .message-time {
    color: #667eea;
    font-weight: 500;
}

.streaming-message .message-time::before {
    content: '⚡ ';
    font-size: 10px;
}
</style>