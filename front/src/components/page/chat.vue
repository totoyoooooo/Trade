<template>
  <div class="chat-main-layout">
    <!-- 左侧会话列表 -->
    <aside class="chat-sidebar">
      <div class="sidebar-header">
        <span class="sidebar-title">会话列表</span>
      </div>
      <el-scrollbar class="sidebar-scroll">
        <div v-for="chat in chatList" :key="chat.id"
             class="sidebar-chat-item"
             :class="{ active: selectedChat && selectedChat.id === chat.id }"
             @click="selectChat(chat)">
          <el-avatar :src="chat.avatar" size="medium" />
          <div class="chat-item-info">
            <div class="chat-item-name">{{ chat.name }}</div>
            <div class="chat-item-last">{{ chat.lastMessage }}</div>
          </div>
          <el-badge :value="chat.unread" :hidden="!chat.unread" class="chat-item-badge" />
          <div class="chat-item-time">{{ chat.timestamp }}</div>
        </div>
      </el-scrollbar>
    </aside>

    <!-- 右侧聊天内容 -->
    <section class="chat-content-area">
      <!-- 聊天头部 -->
      <header v-if="selectedChat" class="chat-header-bar">
        <el-avatar :src="selectedChat.avatar" size="large" />
        <div class="header-user-info">
          <span class="header-user-name">{{ selectedChat.name }}</span>
          <span class="header-user-status">{{ selectedChat.status }}</span>
        </div>
      </header>

      <!-- 聊天消息区 -->
      <main class="chat-message-area" ref="chatWindow">
        <div class="message-list">
          <template v-for="(message, idx) in messages">
            <div v-if="idx === 0 || timeDiffMinutes(message.send_time, messages[idx - 1].send_time) > 3"
                 :key="'time-' + message.id"
                 class="message-time-divider">
              {{ formatTime(message.send_time) }}
            </div>
            <div :key="message.id"
                 class="message-item"
                 :class="{ 'me': message.isMe, 'other': !message.isMe }"
                 @contextmenu.prevent="onMessageRightClick($event, message)">
              <el-avatar :src="message.avatar" size="medium" class="message-avatar" />
              <div class="message-bubble">
                <div class="message-text">{{ message.content }}</div>
                <div class="message-meta">{{ message.sendTime }}</div>
              </div>
            </div>
          </template>
        </div>
      </main>

      <!-- 输入区 -->
      <footer v-if="selectedChat" class="chat-input-bar">
        <el-input v-model="inputText"
                  type="textarea"
                  :autosize="{ minRows: 1, maxRows: 4 }"
                  placeholder="输入消息"
                  @keyup.enter.native="sendMessage"
                  class="input-box" />
        <el-button class="send-btn" @click="sendMessage">发送</el-button>
      </footer>

      <!-- 右键菜单 -->
      <div v-if="recallMenu.visible"
           :style="{
             position: 'fixed',
             top: recallMenu.y + 'px',
             left: recallMenu.x + 'px',
             background: '#fff',
             border: '1px solid #eee',
             boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
             zIndex: 9999,
             borderRadius: '6px'
           }"
           @click.stop>
        <div class="context-menu-item" @click="recallMessage(recallMenu.message)">撤回</div>
      </div>
    </section>
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
    if (this.getCookie('shUserId') === '') {
      this.$message.error('请先登录');
      this.$router.push('/login');
      return;
    } else {
      if (this.$route.query.id) {
        this.addAndGetChat(this.$route.query.id).then(res => {
          if(res.status_code == 200) {
            this.getChatList().then(res => {
              if(res.status_code == 200) this.selectChat(this.chatList[0]);
            });
          }
        });
      } else {
        this.getChatList();
      }
    }
  },
  methods: {
    addAndGetChat(userId){
      var user1Id = userId < this.getCookie('shUserId') ? userId : this.getCookie('shUserId');
      var user2Id = userId < this.getCookie('shUserId') ? this.getCookie('shUserId') : userId;
      return this.$api.addAndGetChat({
        id: user1Id + "" + user2Id,
        user1_id: user1Id,
        user2_id: user2Id,
      });
    },
    getChatList() {
      var ans;
      if(this.$route.query.id){
        ans = this.$api.getChatList(
          "userId=" + this.getCookie('shUserId') + "&type=" + this.$route.query.id
        )
      }else{
        ans = this.$api.getChatList(
          "userId=" + this.getCookie('shUserId') + "&type=-1"
        )
      }
      ans.then(res => {
        if(res.status_code == 200) this.chatList = res.data;
      });
      if(this.chatList.length == 0){
        this.selectChat(null);
      }
      return ans;
    },
    openChat(userId) {
      var user1Id = userId < this.getCookie('shUserId') ? userId : this.getCookie('shUserId');
      var user2Id = userId < this.getCookie('shUserId') ? this.getCookie('shUserId') : userId;
      this.$api.openChat({
        id: user1Id + "" + user2Id,
        user1_id: user1Id,
        user2_id: user2Id,
        getterId: this.getCookie('shUserId')
      }).then(res => {
        if (res.status_code == 200) {
          this.myUser = res.data.myUser;
          this.otherUser = res.data.otherUser;
          this.chatId = res.data.chatId;
          this.messages = res.data.messages;
          this.$nextTick(() => {
            this.scrollToBottom();
          });
          this.$api.clearUnread(
            "chat_id=" + this.chatId + "&sender_id=" + this.getCookie('shUserId')
          ).then(res => {
            if (res.status_code == 200) {
                this.getChatList();
            }
          });
        }
      });
    },
    getCookie(cname) {
      var name = cname + "=";
      var ca = document.cookie.split(';');
      for (var i = 0; i < ca.length; i++) {
        var c = ca[i].trim();
        if (c.indexOf(name) === 0) return c.substring(name.length, c.length);
      }
      return "";
    },
    sendMessage() {
      if (this.inputText.trim()) {
        const newMessage = {
          chat_id: this.chatId,
          content: this.inputText,
          sender_id: this.getCookie('shUserId'),
          send_time: this.getNowTime(),
        };
        this.inputText = '';
        this.$api.sendChatMessage(newMessage).then(res => {
          if (res.status_code == 200) {
            this.$webSocket.sendMessage({
              target: this.otherUser.id,
              data: {
                type: 'chat',
                content: newMessage,
              }
            });
            //this.messages.push(res.data);
            this.getChatList();
            this.selectChat(this.selectedChat);
            this.$nextTick(() => {
              this.scrollToBottom();
            });
          }
        });
      }
    },
    selectChat(chat) {
      this.selectedChat = chat;
      if (!chat) {
        this.chatId = null;
        this.otherUser = null;
        this.myUser = null;
        this.messages = [];
        return;
      }
      if (chat.otherId !== undefined && chat.otherId !== null) {
        this.chatId = chat.id;
        this.openChat(chat.otherId);
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
    this.$webSocket.init("ws://localhost:8080/websocket/" + this.getCookie('shUserId'));
    this.$bus.$on('new-message', (msg) => {
      this.getChatList();
      if (msg.chat_id === this.chatId && this.otherUser) {
        this.selectedChat = this.chatList.find(chat => chat.id === this.selectedChat.id);
        this.selectChat(this.selectedChat);
      }
    });
    this.$bus.$on('revoke-message', (msg) => {
      this.getChatList();
      if (msg.chat_id === this.chatId && this.otherUser) {
        this.selectedChat = this.chatList.find(chat => chat.id === this.selectedChat.id);
        this.selectChat(this.selectedChat);
      }
    });
    this.$bus.$on('online', (msg) => {
      this.getChatList().then(res => {
        if(this.selectedChat != null){
          console.log("更新前" + this.selectedChat.status);
          this.selectedChat = this.chatList.find(chat => chat.id === this.selectedChat.id);
          console.log("更新后" + this.selectedChat.status);
          this.selectChat(this.selectedChat);
        }
      });
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
.chat-main-layout {
  display: flex;
  height: calc(100vh - 60px); /* 顶部 header 高度 */
  margin-top: 60px;           /* 或者用 padding-top: 60px; */
  background: #f7f8fa;
  font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}

.chat-sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #e5e6eb;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 18px 20px 10px 20px;
  border-bottom: 1px solid #e5e6eb;
  background: #fafbfc;
}

.sidebar-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.sidebar-scroll {
  flex: 1;
  padding: 10px 0;
}

.sidebar-chat-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;
  position: relative;
  margin-bottom: 4px;
}

.sidebar-chat-item.active,
.sidebar-chat-item:hover {
  background: #e6f0ff;
}

.chat-item-info {
  flex: 1;
  margin-left: 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.chat-item-name {
  font-size: 15px;
  font-weight: 500;
  color: #222;
}

.chat-item-last {
  font-size: 12px;
  color: #888;
  margin-top: 2px;
}

.chat-item-badge {
  margin-left: 8px;
}

.chat-item-time {
  font-size: 11px;
  color: #bbb;
  margin-left: 10px;
}

.chat-content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f7f8fa;
  min-width: 0;
}

.chat-header-bar {
  display: flex;
  align-items: center;
  padding: 18px 24px;
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
}

.header-user-info {
  margin-left: 14px;
  display: flex;
  flex-direction: column;
}

.header-user-name {
  font-size: 17px;
  font-weight: 600;
  color: #222;
}

.header-user-status {
  font-size: 13px;
  color: #7c7c7c;
  margin-top: 2px;
}

.chat-message-area {
  flex: 1;
  overflow-y: auto;
  padding: 18px 24px;
  background: #f7f8fa;
}

.message-list {
  display: flex;
  flex-direction: column;
}

.message-time-divider {
  text-align: center;
  color: #aaa;
  font-size: 13px;
  margin: 16px 0 8px 0;
}

.message-item {
  display: flex;
  align-items: flex-end;
  margin-bottom: 14px;
}

.message-item.me {
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 10px;
}

.message-bubble {
  max-width: 65%;
  background: #fff;
  border-radius: 14px;
  padding: 10px 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.message-item.me .message-bubble {
  background: #e6f0ff;
  align-items: flex-end;
}

.message-text {
  font-size: 15px;
  color: #222;
  word-break: break-word;
}

.message-meta {
  font-size: 11px;
  color: #aaa;
  margin-top: 4px;
  align-self: flex-end;
}

.chat-input-bar {
  display: flex;
  align-items: flex-end;
  padding: 16px 24px;
  background: #fff;
  border-top: 1px solid #e5e6eb;
  gap: 12px;
}

.input-box {
  flex: 1;
  font-size: 15px;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
  background: #fafbfc;
}

.send-btn {
  background: #409EFF;
  color: #fff;
  border-radius: 8px;
  border: none;
  padding: 0 24px;
  height: 40px;
  font-size: 15px;
  font-weight: 500;
  transition: background 0.2s;
}

.send-btn:hover,
.send-btn:focus {
  background: #337ecc;
}

.context-menu-item {
  padding: 10px 28px;
  cursor: pointer;
  font-size: 15px;
  color: #222;
  border-radius: 6px;
  transition: background 0.2s;
}

.context-menu-item:hover {
  background: #f0f0f0;
}

@media (max-width: 700px) {
  .chat-main-layout {
    flex-direction: column;
    height: auto;
    border-radius: 0;
  }
  .chat-sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #e5e6eb;
  }
  .chat-content-area {
    min-width: 0;
  }
  .chat-header-bar,
  .chat-input-bar {
    padding: 12px;
  }
  .chat-message-area {
    padding: 12px;
  }
}
</style>