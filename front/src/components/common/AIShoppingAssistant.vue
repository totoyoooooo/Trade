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
                </div>
                <div class="header-actions">
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
                        :class="{ 'user-message': message.isUser, 'ai-message': !message.isUser }"
                    >
                        <div class="message-content">
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

                    <div v-if="isLoading" class="message ai-message">
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
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"/>
                            </svg>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import axios from 'axios'

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
            userId: 1
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
            this.sessionId = this.generateSessionId()
            this.messages = []
            this.addWelcomeMessage()
            this.$nextTick(this.scrollToBottom)
        },
        toggleAssistant() {
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
        async sendMessage() {
            if (!this.inputMessage.trim() || this.isLoading) return
            const userMessage = this.inputMessage.trim()

            this.messages.push({
                text: userMessage,
                isUser: true,
                timestamp: new Date(),
                productCards: null
            })
            this.inputMessage = ''
            this.isLoading = true
            this.$nextTick(this.scrollToBottom)

            this.$api.callShoppingAIAgent({
                sessionId: this.sessionId,
                message: userMessage,
                userId: this.userId
            }).then( response => {
                console.log(response)
                if (!response.data || typeof response.data !== 'object') {
                    throw new Error('响应格式错误')
                }
                const reponseData = response.data.data || response.data
                const { cleanedMessage, productCardsForMessage } = this.processAIResponse(reponseData)
                this.messages.push({
                    text: cleanedMessage || '收到响应，但内容为空',
                    inUser: false,
                    timestamp: new Date(),
                    productCards: this.processProductsForCard(productCardsForMessage)
                })
            }).catch (error => {
                console.error('发送消息失败:', error)
                let errorMessage = '抱歉，我暂时无法回答您的问题，请稍后再试。'
                if (error.response) {
                    if (error.response.status === 404) errorMessage = '服务接口未找到，请检查后端服务。'
                    else if (error.response.status === 500) errorMessage = '服务器内部错误，请稍后再试。'
                    else if (error.response.data && error.response.data.message) errorMessage = error.response.data.message
                } else if (error.request) {
                    errorMessage = '网络连接失败，请检查您的网络。'
                } else if (error.code === 'ECONNABORTED') {
                    errorMessage = '请求超时，请稍后再试。'
                }
                this.messages.push({ text: errorMessage, isUser: false, timestamp: new Date(), productCards: null })
            }).finally(() => {
                this.isLoading = false
                this.$nextTick(this.scrollToBottom)
            })
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
        formatMessage(text) {
            if (!text) return '';
            let formattedText = text;

            // 将 Markdown 的 **text** 转换为 <strong>text</strong>
            formattedText = formattedText.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
            // 将换行符 \n 转换为 <br>
            formattedText = formattedText.replace(/\n/g, '<br>');

            return formattedText
        }
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
</style>