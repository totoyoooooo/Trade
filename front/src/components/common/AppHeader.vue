<template>
    <div class="header">
        <div class="header-container">
            <div class="app-name">
                <img src="image/bg2.jpg" alt="" style="width:20px; position: relative; top:5px; margin-right: 5px">
                <router-link to="/"><b style="color: dodgerblue" >校园二手物品交易平台</b></router-link>

            </div>
            <div class="search-container">
                <el-input placeholder="搜物品..." v-model="searchValue" @keyup.enter.native="searchIdle">
                    <el-button slot="append" icon="el-icon-search" @click="searchIdle"></el-button>
                </el-input>
            </div>
            <el-button type="primary" icon="el-icon-plus"  @click="toRelease">物品发布</el-button>
            <el-badge :value="unreadTotal" :hidden="!unreadTotal">
                <el-button type="primary" icon="el-icon-chat-dot-round" @click="toChat">私信</el-button>
            </el-badge>
            <el-badge :value="unreadMessageTotal" :hidden="!unreadMessageTotal">
                <el-button type="primary" icon="el-icon-chat-dot-round" @click="toMessage">留言消息</el-button>
            </el-badge>
            <router-link v-if="!isLogin" class="user-name-text" to="/login">登录</router-link>
            <el-dropdown trigger="click" v-else>
                <div style="cursor:pointer;display: flex;align-items: center;">
                    <div style="font-size: 16px;color: #409EFF;padding-right: 5px;">{{nicknameValue?nicknameValue:nickname}}</div>
                    <el-avatar :src="avatarValue?avatarValue:avatar"></el-avatar>
                </div>
                <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item><div @click="toMe">个人中心</div></el-dropdown-item>
                    <el-dropdown-item divided style="color: red;"><div @click="loginOut">退出登录</div></el-dropdown-item>
                </el-dropdown-menu>
            </el-dropdown>
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
            if(!this.$globalData.userInfo.nickname){
                this.$api.getUserInfo().then(res=>{
                    console.log('Header getUserInfo:',res);
                    if(res.status_code===200){
                        this.nickname=res.data.nickname;
                        this.avatar=res.data.avatar;
                        res.data.signInTime=res.data.signInTime.substring(0,10);
                        this.$globalData.userInfo=res.data;
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
                this.$api.getChatList("userId=" + this.getCookie('shUserId') + "&type=-1").then(res => {
                if (res.status_code === 200) {
                    this.unreadTotal = res.data.reduce((sum, chat) => sum + (chat.unread || 0), 0);
                }});
            },
            getUnreadMessageTotal() {
                this.$api.getAllMyMessage().then(res => {
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
                        this.$globalData.userInfo={};
                        localStorage.removeItem('currentUser'); // remove localStorage
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

            }
        },
    };
</script>
<style scoped>
    .header {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        width: 100%;
        height: 58px;
        background: #ffffff;
        display: flex;
        justify-content: center;
        border-bottom: #eeeeee solid 2px;
        z-index: 1000;
    }

    .header-container {
        width: 1000px;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: space-between;
    }

    .app-name a {
        color: #409EFF;
        font-size: 24px;
        text-decoration: none;
    }

    .search-container {
        width: 300px;
    }
    .user-name-text{
        font-size: 16px;
        font-weight: 600;
        color: #409EFF;
        cursor: pointer;
        text-decoration: none;
    }
    .el-badge {
        margin-right: 8px;
    }
</style>
