<template>
    <div class="header">
        <div class="header-container">
            <!-- Logo和品牌区域 -->
            <div class="app-name">
                <div class="logo-container">
                    <img src="/saki.png" alt="" class="logo-image">
                    <router-link to="/" class="brand-link">
                        <span class="brand-text">校园二手物品交易平台</span>
                    </router-link>
                </div>
            </div>
            
            <!-- 搜索区域 -->
            <div class="search-container">
                <div class="search-wrapper">
                    <el-input 
                        placeholder="搜物品..." 
                        v-model="searchValue" 
                        @keyup.enter.native="searchIdle"
                        class="modern-search-input"
                        prefix-icon="el-icon-search">
                        <el-button slot="append" icon="el-icon-search" @click="searchIdle" class="search-btn"></el-button>
                </el-input>
                </div>
            </div>
            
            <!-- 功能按钮区域 -->
            <div class="action-buttons">
                <el-button class="modern-btn modern-btn-release" icon="el-icon-plus" @click="toRelease">
                    <span>物品发布</span>
                </el-button>
                <el-button class="modern-btn modern-btn-wanted" icon="el-icon-search" @click="toWantedItemList">
                    <span>求购</span>
                </el-button>
                
                <el-badge :value="unreadTotal" :hidden="!unreadTotal" class="notification-badge">
                    <el-button class="modern-btn modern-btn-message" icon="el-icon-chat-dot-round" @click="toChat">
                        <span>私信</span>
                    </el-button>
            </el-badge>
                
                <el-badge :value="unreadMessageTotal" :hidden="!unreadMessageTotal" class="notification-badge">
                    <el-button class="modern-btn modern-btn-message" icon="el-icon-bell" @click="toMessage">
                        <span>留言</span>
                    </el-button>
            </el-badge>
            </div>
            
            <!-- 用户区域 -->
            <div class="user-section">
                <router-link v-if="!isLogin" class="login-link" to="/login">
                    <span class="login-text">登录</span>
                </router-link>
                
                <el-dropdown trigger="click" v-else class="user-dropdown">
                    <div class="user-info">
                        <div class="user-name">{{nicknameValue?nicknameValue:nickname}}</div>
                        <el-avatar class="user-avatar" :src="avatarValue?avatarValue:avatar"></el-avatar>
                    </div>
                    <el-dropdown-menu slot="dropdown" class="user-menu">
                        <el-dropdown-item class="menu-item">
                            <div @click="toMe" class="menu-link">
                                <i class="el-icon-user"></i>
                                <span>个人中心</span>
                            </div>
                        </el-dropdown-item>
                        <el-dropdown-item divided class="menu-item logout-item">
                            <div @click="loginOut" class="menu-link logout-link">
                                <i class="el-icon-switch-button"></i>
                                <span>退出登录</span>
                </div>
                        </el-dropdown-item>
                </el-dropdown-menu>
            </el-dropdown>
            </div>
        </div>
    </div>
</template>
<script>

    export default {
        name: 'Header',
        props: ['searchInput','nicknameValue','avatarValue'],
        data() {
            return {
                searchValue: this.searchInput,
                nickname:'登录',
                avatar:'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
                isLogin:false,
                unreadTotal: 0,
                unreadMessageTotal: 0
            };
        },
        mounted() {
            // console.log("header mounted");
            this.getUnreadTotal();
            this.getUnreadMessageTotal();
            this.$bus.$on('new-message', this.getUnreadTotal);
            this.$bus.$on('revoke-message', this.getUnreadTotal);
            this.$bus.$on('new-leave-message', this.getUnreadMessageTotal);
            
            // 添加滚动监听器以实现导航栏效果
            this.handleScroll();
            window.addEventListener('scroll', this.handleScroll);
        },
        beforeDestroy() {
            // console.log("header beforeDestroy");
            this.$bus.$off('new-message', this.getUnreadTotal);
            this.$bus.$off('revoke-message', this.getUnreadTotal);
            this.$bus.$off('new-leave-message', this.getUnreadMessageTotal);
            // remove windows event listener
            window.removeEventListener('storage', this.handleStorageChange);
        },
        created(){
            // multi-pages sync
            window.addEventListener('storage', this.handleStorageChange);
            console.log("header");
            const userIdFromCookie = this.getCookie('shUserId'); // 在这里定义
            if(!this.$globalData.userInfo.nickname){
                this.$api.getUserInfo().then(res=>{
                    console.log('Header getUserInfo:',res);
                    if(res.status_code===200 && res.data){
                        res.data.signInTime=res.data.signInTime.substring(0,10);
                        this.$store.commit('setUserInfo', res.data); // 更新Vuex store
                        this.nickname=res.data.nickname;
                        this.avatar=res.data.avatar;
                        this.isLogin=true;
                        this.$webSocket.init("ws://localhost:8080/websocket/" + res.data.id);

                        // store user info in localstorage
                        localStorage.setItem('currentUser', JSON.stringify({
                            nickname: res.data.nickname,
                            avatar: res.data.avatar,
                            isLogin: true,
                            userId: res.data.id
                        }));
                    }
                })
            }else {
                this.nickname=this.$globalData.userInfo.nickname;
                this.avatar=this.$globalData.userInfo.avatar;
                this.isLogin=true;
                this.$webSocket.init("ws://localhost:8080/websocket/" + this.$globalData.userInfo.id);

                // store user info in localstorage
                localStorage.setItem('currentUser', JSON.stringify({
                    nickname: this.$globalData.userInfo.nickname,
                    avatar: this.$globalData.userInfo.avatar,
                    isLogin: true,
                    userId: this.$globalData.userInfo.id
                }));
                console.log("localStorage stored");
                console.log("" + this.$globalData.userInfo);
            }
            console.log('AppHeader created() - Final isLogin state:', this.isLogin, 'userIdFromCookie:', userIdFromCookie);
        },
        methods: {
            handleStorageChange(e) {
                if (e.key == 'currentUser') {
                    const userData = e.newValue ? JSON.parse(e.newValue) : null;
                    this.updateUserInfo(userData);
                }
            },
            updateUserInfo(user) {
                console.log('update UserInfo called with:', user);

                // every AppHeader component reload user info
                if(user) {
                    // if changing user successfully
                    this.nickname = user.nickname;
                    this.avatar = user.avatar;
                    this.isLogin = true;
                    // get unread msg
                    this.getUnreadTotal();
                    this.getUnreadMessageTotal();
                } else {
                    // default
                    this.nickname = '登录';
                    this.avatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
                    this.isLogin = false;
                    this.unreadTotal = 0;
                    this.unreadMessageTotal = 0;
                }
            },
            searchIdle() {
                if ('/search' !== this.$route.path) {
                    this.$router.push({path: '/search', query: {searchValue: this.searchValue}});
                } else {
                    this.$router.replace({path: '/search', query: {searchValue: this.searchValue}});
                    this.$router.go(0);
                }

            },
            getUnreadTotal() {
                // 从Vuex获取userId
                const userId = this.$store.state.userInfo.id;
                if (!userId) return;

                this.$api.getChatList("userId=" + userId + "&type=-1").then(res => {
                if (res.status_code === 200) {
                    this.unreadTotal = res.data.reduce((sum, chat) => sum + (chat.unread || 0), 0);
                }});
            },
            getUnreadMessageTotal() {
                // 从Vuex获取userId
                const userId = this.$store.state.userInfo.id;
                if (!userId) return;

                this.$api.getAllMyMessage({ userId: userId }).then(res => {
                    if (res.status_code === 200) {
                        this.unreadMessageTotal = res.data.filter(msg => !msg.has_read).length;
                    }
                });
            },
            toMe() {
                if ('/me' !== this.$route.path) {
                    this.$router.push({path: '/me'});
                }
            },
            toChat(){
                if ('/chat' !== this.$route.path) {
                    this.$router.push({path: '/chat'});
                }
            },
            toMessage(){
                if ('/message' !== this.$route.path) {
                    this.$router.push({path: '/message'});
                }
            },
            toRelease(){
                if ('/release' !== this.$route.path) {
                    this.$router.push({path: '/release'});
                }
            },
            toWantedItemList() {
                if ('/wanteditemlist' !== this.$route.path) {
                    this.$router.push({path: '/wanteditemlist'});
                }
            },
            getCookie(cname){
                var name = cname + "=";
                var ca = document.cookie.split(';');
                for(var i=0; i<ca.length; i++){
                    var c = ca[i].trim();
                    if (c.indexOf(name)===0) return c.substring(name.length,c.length);
                }
                return "";
            },
            loginOut(){
                this.$api.logout().then(res=>{
                    if(res.status_code===200){
                        this.$store.commit('setUserInfo', {}); // 清空Vuex store中的用户信息
                        // 同时清空 cookie
                        document.cookie = "shUserId=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
                        console.log("login out");
                        this.$webSocket.closeWebSocket();
                        if ('/index' === this.$route.path) {
                            this.$router.go(0);
                        }else {
                            this.$router.push({path: '/index'});
                        }
                    }else {
                        this.$message.error('网络或系统异常，退出登录失败！');
                    }
                });
            },
            
            // 处理滚动事件，实现导航栏透明度变化
            handleScroll() {
                const header = this.$el;
                if (header) {
                    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
                    if (scrollTop > 50) {
                        header.classList.add('scrolled');
                    } else {
                        header.classList.remove('scrolled');
                    }
                }
            }
        },
    };
</script>
<style scoped>
/* 现代化设计变量 - 直接定义在这里避免导入问题 */
:root {
  /* Primary Colors - Modern Blue Gradient */
  --primary-color: #667eea;
  --primary-light: #764ba2;
  --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --primary-gradient-hover: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  
  /* Secondary Colors */
  --secondary-color: #f093fb;
  --secondary-gradient: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  
  /* Success & Status Colors */
  --success-color: #4facfe;
  --success-gradient: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  --warning-color: #f093fb;
  --warning-gradient: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  --danger-color: #ff6b6b;
  --danger-gradient: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  
  /* Neutral Colors */
  --text-primary: #2d3748;
  --text-secondary: #718096;
  --text-light: #a0aec0;
  --text-white: #ffffff;
  
  /* Background Colors */
  --bg-primary: #f7fafc;
  --bg-white: #ffffff;
  --bg-card: rgba(255, 255, 255, 0.95);
  --bg-overlay: rgba(0, 0, 0, 0.1);
  
  /* Glass Morphism */
  --glass-bg: rgba(255, 255, 255, 0.25);
  --glass-border: rgba(255, 255, 255, 0.18);
  --glass-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  
  /* Shadows */
  --shadow-sm: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  --shadow-2xl: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  
  /* Border Radius */
  --radius-sm: 0.375rem;
  --radius-md: 0.5rem;
  --radius-lg: 0.75rem;
  --radius-xl: 1rem;
  --radius-2xl: 1.5rem;
  --radius-full: 9999px;
  
  /* Spacing */
  --spacing-xs: 0.25rem;
  --spacing-sm: 0.5rem;
  --spacing-md: 1rem;
  --spacing-lg: 1.5rem;
  --spacing-xl: 2rem;
  --spacing-2xl: 3rem;
  
  /* Typography */
  --font-size-xs: 0.75rem;
  --font-size-sm: 0.875rem;
  --font-size-base: 1rem;
  --font-size-lg: 1.125rem;
  --font-size-xl: 1.25rem;
  --font-size-2xl: 1.5rem;
  --font-size-3xl: 1.875rem;
  --font-size-4xl: 2.25rem;
  
  /* Animation */
  --transition-fast: 0.15s ease-in-out;
  --transition-normal: 0.3s ease-in-out;
  --transition-slow: 0.5s ease-in-out;
}

/* 主导航栏容器 */
    .header {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        width: 100%;
    height: 70px;
    background: var(--glass-bg);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border-bottom: 1px solid var(--glass-border);
    box-shadow: var(--shadow-lg);
        display: flex;
        justify-content: center;
        z-index: 1000;
    transition: all var(--transition-normal);
}

.header::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: var(--primary-gradient);
    opacity: 0.05;
    z-index: -1;
    }

    .header-container {
    width: min(1200px, 95%);
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: space-between;
    padding: 0 var(--spacing-lg);
    gap: var(--spacing-lg);
}

/* Logo和品牌区域 */
.app-name {
    flex-shrink: 0;
}

.logo-container {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
}

.logo-image {
    width: 32px;
    height: 32px;
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-md);
    transition: all var(--transition-normal);
}

.logo-image:hover {
    transform: scale(1.1) rotate(5deg);
    box-shadow: var(--shadow-lg);
}

.brand-link {
    text-decoration: none;
    transition: all var(--transition-normal);
}

.brand-text {
    font-size: var(--font-size-xl);
    font-weight: 700;
    background: var(--primary-gradient);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    letter-spacing: -0.02em;
    transition: all var(--transition-normal);
}

.brand-link:hover .brand-text {
    background: var(--primary-gradient-hover);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    transform: translateY(-1px);
}

/* 搜索区域 */
.search-container {
    flex: 1;
    max-width: 400px;
    min-width: 250px;
}

.search-wrapper {
    position: relative;
}

/* 重写Element UI搜索框样式 */
.search-container >>> .el-input {
    border-radius: var(--radius-full);
}

.search-container >>> .el-input__inner {
    border: 2px solid transparent;
    border-radius: var(--radius-full);
    background: rgba(255, 255, 255, 0.9);
    padding: 12px 20px;
    font-size: var(--font-size-base);
    transition: all var(--transition-normal);
    box-shadow: var(--shadow-sm);
}

.search-container >>> .el-input__inner:focus {
    border-color: var(--primary-color);
    background: var(--bg-white);
    box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.search-container >>> .el-input-group__append {
    border: none;
    background: transparent;
    padding: 0;
}

.search-container >>> .search-btn {
    border: none;
    background: var(--primary-gradient);
    color: var(--text-white);
    border-radius: 0 var(--radius-full) var(--radius-full) 0;
    padding: 12px 16px;
    transition: all var(--transition-normal);
}

.search-container >>> .search-btn:hover {
    background: var(--primary-gradient-hover);
    transform: scale(1.05);
}

/* 功能按钮区域 */
.action-buttons {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    flex-shrink: 0;
}

.modern-btn {
    border: none;
    border-radius: var(--radius-lg);
    padding: 10px 16px;
    font-weight: 600;
    font-size: var(--font-size-sm);
    transition: all var(--transition-normal);
    position: relative;
    overflow: hidden;
    cursor: pointer;
    box-shadow: var(--shadow-sm);
}

.modern-btn::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
    transition: left 0.5s;
}

.modern-btn:hover::before {
    left: 100%;
}

.modern-btn-release {
    background: var(--success-gradient);
    color: var(--text-white);
}

.modern-btn-release:hover {
    background: var(--success-gradient);
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
}

.modern-btn-message {
    background: var(--secondary-gradient);
    color: var(--text-white);
}

.modern-btn-message:hover {
    background: var(--secondary-gradient);
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
}

.modern-btn span {
    position: relative;
    z-index: 1;
}

.notification-badge {
    margin-right: var(--spacing-xs);
}

.notification-badge >>> .el-badge__content {
    background: var(--danger-color);
    border: 2px solid var(--bg-white);
    font-weight: 600;
    font-size: 10px;
    animation: pulse 2s infinite;
}

/* 用户区域 */
.user-section {
    flex-shrink: 0;
}

.login-link {
    text-decoration: none;
    padding: 10px 20px;
    border-radius: var(--radius-lg);
    background: var(--primary-gradient);
    color: var(--text-white);
    font-weight: 600;
    transition: all var(--transition-normal);
    box-shadow: var(--shadow-sm);
    display: inline-block;
}

.login-link:hover {
    background: var(--primary-gradient-hover);
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
        text-decoration: none;
}

.login-text {
    color: var(--text-white);
}

.user-dropdown {
    cursor: pointer;
}

.user-info {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    padding: var(--spacing-sm) var(--spacing-md);
    border-radius: var(--radius-lg);
    background: rgba(255, 255, 255, 0.1);
    transition: all var(--transition-normal);
}

.user-info:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateY(-1px);
}

.user-name {
    font-size: var(--font-size-base);
    font-weight: 600;
    color: var(--text-primary);
    background: var(--primary-gradient);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.user-avatar {
    border: 2px solid var(--primary-color);
    transition: all var(--transition-normal);
}

.user-info:hover .user-avatar {
    transform: scale(1.1);
    box-shadow: var(--shadow-md);
}

/* 下拉菜单样式 */
.user-menu {
    border: none;
    border-radius: var(--radius-lg);
    background: var(--glass-bg);
    backdrop-filter: blur(20px);
    box-shadow: var(--shadow-xl);
    padding: var(--spacing-sm);
    margin-top: var(--spacing-sm);
}

.menu-item {
    border-radius: var(--radius-md);
    margin: 2px 0;
    transition: all var(--transition-normal);
}

.menu-item:hover {
    background: rgba(102, 126, 234, 0.1);
}

.menu-link {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    padding: var(--spacing-sm) var(--spacing-md);
    color: var(--text-primary);
    font-weight: 500;
    cursor: pointer;
    transition: all var(--transition-normal);
}

.logout-item:hover {
    background: rgba(255, 107, 107, 0.1);
}

.logout-link {
    color: var(--danger-color);
}

/* 响应式设计 */
@media (max-width: 1024px) {
    .header-container {
        width: 95%;
        gap: var(--spacing-md);
    }
    
    .search-container {
        max-width: 300px;
        min-width: 200px;
    }
    
    .modern-btn span {
        display: none;
    }
    
    .brand-text {
        font-size: var(--font-size-lg);
    }
}

@media (max-width: 768px) {
    .header {
        height: 60px;
    }
    
    .header-container {
        gap: var(--spacing-sm);
        padding: 0 var(--spacing-md);
    }

    .search-container {
        max-width: 200px;
        min-width: 150px;
    }
    
    .action-buttons {
        gap: var(--spacing-xs);
    }
    
    .modern-btn {
        padding: 8px 12px;
    }
    
    .brand-text {
        font-size: var(--font-size-base);
    }
}

@media (max-width: 480px) {
    .logo-container {
        gap: var(--spacing-xs);
    }
    
    .brand-text {
        display: none;
    }
    
    .search-container {
        min-width: 120px;
    }
    
    .action-buttons .modern-btn:not(:first-child) {
        display: none;
    }
}

/* 动画效果 */
@keyframes pulse {
    0%, 100% { transform: scale(1); }
    50% { transform: scale(1.1); }
}

.fade-in {
    animation: fadeIn 0.6s ease-out;
}

/* 滚动时的效果 */
.header.scrolled {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(25px);
    box-shadow: var(--shadow-xl);
    }
</style>
