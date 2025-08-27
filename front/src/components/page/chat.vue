<template>
  <div class="chat-container">
    <!-- 左侧聊天列表 -->
    <el-aside width="250px" class="chat-list">
      <el-scrollbar>
        <div v-for="chat in chatList" :key="chat.id" class="chat-item" @click="selectChat(chat)"
          :class="{ 'active-chat': selectedChat && selectedChat.id === chat.id }">
          <el-badge :value="chat.unread" :hidden="!chat.unread" class="unread-badge">
            <el-avatar :src="chat.avatar" size="large" />
          </el-badge>
          <div class="chat-info">
            <div class="name">{{ chat.name }}</div>
            <div class="last-message">{{ chat.lastMessage }}</div>
          </div>
          <div class="timestamp">{{ chat.timestamp }}</div>
        </div>
      </el-scrollbar>
    </el-aside>

    <!-- 右侧聊天窗口 -->
    <div class="chat-right">
      <!-- 聊天头部 -->
      <div class="chat-header" v-if="selectedChat">
        <el-avatar :src="selectedChat.avatar" size="large" />
        <div class="header-info">
          <div class="name">{{ selectedChat.name }}</div>
          <div class="status">{{ selectedChat.status }}</div>
        </div>
      </div>

      <!-- 聊天内容 -->
      <div class="chat-window" ref="chatWindow">
        <div class="message-container">
          <div v-for="(message, idx) in messages">
            <div v-if="idx === 0 || timeDiffMinutes(message.send_time, messages[idx - 1].send_time) > 3"
              :key="'time-' + message.id" class="chat-time-divider">
              {{ formatTime(message.send_time) }}
            </div>
            <div :key="message.id" class="message"
              :class="{ 'message-me': message.isMe, 'message-other': !message.isMe }"
              @contextmenu.prevent="onMessageRightClick($event, message)">
              <div class="avatar">
                <el-avatar :src="message.avatar" size="large" />
              </div>
              <div class="message-text">{{ message.content }}</div>
              <div class="timestamp">{{ message.sendTime }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="input-form" v-if="selectedChat" style="display: flex; gap: 8px;">
        <el-input v-model="inputText" type="textarea" :autosize="{ minRows: 1, maxRows: 4 }" placeholder="输入消息"
          @keyup.enter.native="sendMessage" class="input" style="flex: 1;" />
        <el-button class="custom-button" @click="sendMessage" style="align-self: flex-end; height: 40px;">发送</el-button>
      </div>
      <div v-if="recallMenu.visible" :style="{
        position: 'fixed',
        top: recallMenu.y + 'px',
        left: recallMenu.x + 'px',
        background: '#fff',
        border: '1px solid #eee',
        boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
        zIndex: 9999,
        borderRadius: '4px'
      }" @click.stop>
        <div class="context-menu-item" style="padding: 8px 20px; cursor: pointer;"
          @click="recallMessage(recallMenu.message)">撤回</div>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  name: 'Chat',
  data() {
    return {
      //对方信息
      otherUser: null,
      //我方信息
      myUser: null,
      //当前聊天ID
      chatId: null,
      // 输入框内容
      inputText: '',
      // 消息列表 
      messages: [],
      chatList: [],
      selectedChat: null,
      recallMenu: {
        visible: false,
        x: 0,
        y: 0,
        message: null,
      },
    };
  },
  created() {
    console.log('chat.vue created() hook triggered.');
    const userIdFromCookie = this.getCookie('shUserId');
    console.log('chat.vue - Current userId from cookie:', userIdFromCookie);
    if(userIdFromCookie === ''){
      this.$message.error('请先登录');
      this.$router.push('/login');
      return;
    }else{
      if(this.$route.query.id){
        console.log('chat.vue - Route query ID found:', this.$route.query.id);
        this.getNewChatList(Number(userIdFromCookie), Number(this.$route.query.id)).then(() => {
          if (this.chatList.length > 0) {
            console.log('Chat created with new chat list:', this.chatList);
            this.selectChat(this.chatList[0]);
          }
        });
      }else{
        console.log('chat.vue - No route query ID, fetching general chat list.');
        this.getChatList(Number(userIdFromCookie));
      }
    }
  },
  methods: {
    getNewChatList(userId, type) {
      console.log('Calling getNewChatList with userId:', userId, 'type:', type);
      return this.$api.getChatList(
        { userId: userId, type: type }
      ).then(res => {
        console.log('getNewChatList response:', res);
        if(res.status_code == 200){
          this.chatList = res.data;
        } else {
          this.$message.error('获取新聊天列表失败');
        }
      }).catch(error => {
        console.error('getNewChatList request failed:', error);
        this.$message.error('获取新聊天列表请求失败');
      });
    },
    getChatList(userId) {
      console.log('Calling getChatList with userId:', userId);
      return this.$api.getChatList(
        { userId: userId, type: -1 }
      ).then(res => {
        console.log('getChatList response:', res);
        if(res.status_code == 200){
          this.chatList = res.data;
        } else {
          this.$message.error('获取聊天列表失败');
        }
      }).catch(error => {
        console.error('getChatList request failed:', error);
        this.$message.error('获取聊天列表请求失败');
      });
      if(this.chatList.length == 0){
        this.selectChat(null);
      }
      // No return ans;
    },
    openChat(userId){
      var user1Id = userId < this.getCookie('shUserId') ? userId : Number(this.getCookie('shUserId'));
      var user2Id = userId < this.getCookie('shUserId') ? Number(this.getCookie('shUserId')) : userId; 
      console.log('Calling openChat with user1Id:', user1Id, 'user2Id:', user2Id, 'getterId:', Number(this.getCookie('shUserId')));
      this.$api.openChat({
        id: user1Id + "" + user2Id,
        user1_id: user1Id,
        user2_id: user2Id,
        getterId: Number(this.getCookie('shUserId'))
      }).then(res => {
        console.log('openChat response:', res);
        if(res.status_code == 200){
          this.myUser = res.data.myUser;
          this.otherUser = res.data.otherUser;
          this.chatId = res.data.chatId;
          this.messages = res.data.messages;
          this.$nextTick(() => {
            this.scrollToBottom();
          });
          this.$api.clearUnread(
            { chat_id: this.chatId, sender_id: Number(this.getCookie('shUserId')) }
          ).then(res => {
            console.log('clearUnread response:', res);
            if(res.status_code == 200){
              if(this.$route.query.id){
                this.getNewChatList(Number(this.getCookie('shUserId')), Number(this.$route.query.id));
              }else{
                this.getChatList(Number(this.getCookie('shUserId')));
              }
            } else {
              this.$message.error('清除未读消息失败');
            }
          }).catch(error => {
            console.error('clearUnread request failed:', error);
            this.$message.error('清除未读消息请求失败');
          });
        } else {
          this.$message.error('打开聊天失败');
        }
      }).catch(error => {
        console.error('openChat request failed:', error);
        this.$message.error('打开聊天请求失败');
      });
    },
    getCookie(cname){
      console.log('chat.vue - Attempting to get cookie:', cname);
      var name = cname + "=";
      var ca = document.cookie.split(';');
      for(var i=0; i<ca.length; i++){
        var c = ca[i].trim();
        if (c.indexOf(name)===0) {
          const cookieValue = c.substring(name.length,c.length);
          console.log('chat.vue - Cookie found:', cname, 'value:', cookieValue);
          return cookieValue;
        }
      }
      console.log('chat.vue - Cookie not found:', cname);
      return "";
    },
    sendMessage() {
      if (this.inputText.trim()) {
        const newMessage = {
          chat_id: this.chatId,
          content: this.inputText,
          sender_id: Number(this.getCookie('shUserId')),
          send_time: this.getNowTime(),
        };
        this.inputText = '';
        console.log('Sending message:', newMessage);
        this.$api.sendChatMessage(newMessage).then(res => {
            console.log('sendChatMessage response:', res);
            if(res.status_code == 200){
              this.$webSocket.sendMessage({
                target: this.otherUser.id,
                data: {
                  type: 'chat',
                  content: res.data,
                }
              });
              this.messages.push(res.data);
              // 重新获取聊天列表，以便更新最新消息和未读数
              this.getChatList(Number(this.getCookie('shUserId')));
              this.$nextTick(() => {
                this.scrollToBottom();
              });
            } else {
              this.$message.error('发送消息失败');
            }
          }
        ).catch(
          error => {
            console.error('sendChatMessage request failed:', error);
            this.$message.error('发送消息请求失败');
          }
        );
        
      }
    },
    selectChat(chat) {
      console.log('Selected chat:', chat);
      this.selectedChat = chat;
      if(chat.otherId !== undefined && chat.otherId !== null){
        this.chatId = chat.id;
        this.openChat(chat.otherId);
      }  else if (chat.user1_id && chat.user2_id && this.getCookie('shUserId')) { // Fallback if otherId is not directly available, deduce from user1_id/user2_id
        const currentUserId = Number(this.getCookie('shUserId'));
        const otherUserId = chat.user1_id === currentUserId ? chat.user2_id : chat.user1_id;
        this.chatId = chat.id;
        this.openChat(otherUserId);
      }
    },
    getNowTime() {
      const now = new Date();
      return `${now.getFullYear()}-${now.getMonth() + 1}-${now.getDate()} ${now.getHours()}:${now.getMinutes()}:${now.getSeconds()}`;
    },
    scrollToBottom() {
      const chatWindow = this.$refs.chatWindow;
      if (chatWindow) {
        chatWindow.scrollTop = chatWindow.scrollHeight;
      }
    },
    formatTime(timeStr) {
      const date = new Date(timeStr.replace(/-/g, '/'));
      const y = date.getFullYear();
      const m = (date.getMonth() + 1).toString().padStart(2, '0');
      const d = date.getDate().toString().padStart(2, '0');
      const h = date.getHours().toString().padStart(2, '0');
      const min = date.getMinutes().toString().padStart(2, '0');
      return `${y}-${m}-${d} ${h}:${min}`;
    },
    timeDiffMinutes(time1, time2) {
      // 返回两个时间字符串的分钟差
      const t1 = new Date(time1.replace(/-/g, '/')).getTime();
      const t2 = new Date(time2.replace(/-/g, '/')).getTime();
      return Math.abs(t1 - t2) / 1000 / 60;
    },
    onMessageRightClick(e, message) {
      // 只允许自己消息右键弹出菜单
      if (!message.isMe) return;
      this.recallMenu.visible = true;
      this.recallMenu.x = e.clientX;
      this.recallMenu.y = e.clientY;
      this.recallMenu.message = message;
      document.addEventListener('click', this.hideRecallMenu);
    },
    hideRecallMenu() {
      this.recallMenu.visible = false;
      document.removeEventListener('click', this.hideRecallMenu);
    },
    recallMessage(message) {
      this.hideRecallMenu();
      const now = new Date();
      const send = new Date(message.send_time.replace(/-/g, '/'));
      const diff = (now.getTime() - send.getTime()) / 1000 / 60;
      if (diff > 2) {
        this.$message.warning('超过2分钟，不能撤回');
        return;
      }
      this.$api.revokeMessage("id=" + message.id).then(res => {
        if (res.status_code === 200) {
          this.$message.success('消息已撤回');
          this.getChatList();
          this.selectedChat = this.chatList.find(chat => chat.id === this.selectedChat.id);
          this.selectChat(this.selectedChat);
          this.$webSocket.sendMessage({
            target: this.otherUser.id,
            data: {
              type: 'revoke',
              content: {
                chat_id: this.chatId,
                message_id: message.id,
              },
            }
          });
        } else {
          this.$message.error(res.msg || '撤回失败');
        }
      }).catch(() => {
        this.$message.error('撤回失败');
      });
    },
  },
  mounted() {
    console.log('chat.vue mounted() hook triggered.');
    const userIdForWs = this.getCookie('shUserId');
    if (userIdForWs) {
      this.$webSocket.init("ws://localhost:8080/websocket/" + Number(userIdForWs));
    } else {
      console.error('WebSocket initialization failed: userId not found.');
    }
    this.$bus.$on('new-message', (msg) => {
      console.log('Received new-message event:', msg);
      this.getChatList(Number(this.getCookie('shUserId'))); // Refresh chat list on new message
      if (msg.chat_id === this.chatId && this.otherUser) {
        // If the new message is for the currently open chat, refresh it
        this.openChat(this.otherUser.id);
      }
    });
    this.$bus.$on('revoke-message', (msg) => {
      this.getChatList(Number(this.getCookie('shUserId')));
      if (msg.chat_id === this.chatId && this.otherUser) {
        this.openChat(this.otherUser.id);
      }
    });
    this.$bus.$on('online', (msg) => {
      this.getChatList(Number(this.getCookie('shUserId')));
      if(this.selectedChat != null){
        this.openChat(this.selectedChat.otherId);
      }
    });
  },
  beforeDestroy() {
    this.$bus.$off('new-message');
    this.$bus.$off('revoke-message');
    this.$bus.$off('online');
  },
};
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 100vh;
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
  border: 2px solid #ddd;
  border-radius: 10px;
  overflow: hidden;
}

.chat-list {
  background-color: #f0f0f0;
  border-right: 1px solid #ddd;
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  position: relative;
  /* 用于绝对定位时间戳 */
}

.chat-item:hover {
  background-color: #e0e0e0;
}

.active-chat {
  background-color: #d0d0d0;
  border-left: 4px solid #409EFF;
}

.avatar {
  margin-right: 10px;
}

.chat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.chat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin-left: 10px;
}

.name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
}

.last-message {
  font-size: 12px;
  color: #888;
}

.timestamp {
  font-size: 12px;
  color: #888;
  position: absolute;
  right: 10px;
}

.chat-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 10px;
  background-color: #f0f0f0;
  border-bottom: 1px solid #ddd;
}

.chat-header .avatar {
  margin-right: 10px;
}

.chat-header .header-info {
  display: flex;
  flex-direction: column;
}

.chat-header .header-info .name {
  font-size: 16px;
  font-weight: bold;
  margin-left: 10px;
}

.chat-header .header-info .status {
  font-size: 12px;
  color: #888;
  margin-left: 10px;
}

.chat-window {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background-color: #f9f9f9;
}

.message-container {
  display: flex;
  flex-direction: column;
}

.message {
  display: flex;
  margin-bottom: 10px;
  align-items: flex-start;
}

.message-me {
  flex-direction: row-reverse;
}

.message-other {
  flex-direction: row;
}

.avatar {
  margin-right: 10px;
}

.message-me .avatar {
  margin-left: 10px;
  margin-right: 0;
}

.message-text {
  padding: 10px;
  border-radius: 10px;
  background-color: #f4f4f4;
  max-width: 70%;
  word-wrap: break-word;
}

.message-me .message-text {
  background-color: lightblue;
}

.input-form {
  padding: 10px;
  background-color: #fff;
  border-top: 1px solid #ddd;
}

.input {
  width: 100%;
}

.custom-button {
  background-color: #007bff !important;
  color: #fff !important;
  border: none !important;
}

.custom-button:hover,
.custom-button:focus {
  background-color: #0056b3 !important;
  color: #fff !important;
}

.unread-badge {
  margin-right: 8px;
}

.chat-time-divider {
  text-align: center;
  color: #999;
  font-size: 13px;
  margin: 10px 0;
}

.context-menu-item:hover {
  background: #e0e1e2;
}

::v-deep(.input textarea) {
  resize: none !important;
}
</style>