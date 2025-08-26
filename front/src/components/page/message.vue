<template>
    <div>
        <app-head></app-head>
        <app-body>
            <div class="message-container">
                <div class="message-container-title">我的消息</div>
                <div v-for="(mes,index) in meslist"
                    class="message-container-list"
                    @click="toDetails(mes.idle && mes.idle.id)">
                    <div class="message-container-list-left">
                        <el-image
                                style="width: 55px; height: 55px;border-radius: 5px;"
                                :src="mes.fromU.avatar"
                                fit="cover"></el-image>
                        <div class="message-container-list-text">
                            <div class="message-nickname">{{mes.fromU.nickname}}</div>
                            <div class="message-content">{{mes.content}}</div>
                            <div class="message-time">{{mes.createTime}}</div>
                        </div>
                    </div>
                    <div class="message-container-list-right">
                        <el-image
                                style="width:130px; height: 90px;"
                                :src="mes.idle.imgUrl"
                                fit="contain"></el-image>
                    </div>
                </div>
            </div>
            <app-foot></app-foot>
        </app-body>
    </div>
</template>

<script>
    import AppHead from '../common/AppHeader.vue';
    import AppBody from '../common/AppPageBody.vue'
    import AppFoot from '../common/AppFoot.vue'

    export default {
        name: "message",
        components: {
            AppHead,
            AppBody,
            AppFoot
        },
        data(){
            return{
                meslist:[]
            };
        },
        created(){
            console.log('message.vue created() hook triggered.');
            const userId = this.getCookie("shUserId");
            console.log('Current userId from cookie:', userId);
            if (userId) {
                this.$api.clearMessageUnread({ id: Number(userId) })
                    .then(res=>{
                        console.log('clearMessageUnread response:', res);
                        if (res.status_code === 200) {
                            this.$bus.$emit('new-leave-message');
                            this.fetchMyMessages(Number(userId)); // Pass userId to fetch messages
                        } else {
                            this.$message.error('清除未读消息失败');
                        }
                    }).catch(err => {
                        console.error("清除未读消息请求失败", err);
                        this.$message.error('清除未读消息请求失败');
                    });
            } else {
                this.$message.error('未登录或登录信息已过期，请重新登录');
                this.$router.push('/login');
            }
        },
        methods:{
            fetchMyMessages(userId) {
                console.log('Fetching my messages with userId:', userId);
                this.$api.getAllMyMessage({ userId: userId }).then(res=>{
                    console.log(res);
                    if(res.status_code===200){
                        for(let i=0;i<res.data.length;i++){
                            if(res.data[i].idle && res.data[i].idle.pictureList){
                                let imgList = JSON.parse(res.data[i].idle.pictureList);
                                 res.data[i].idle.imgUrl = imgList ? imgList[0] : '';
                            } else {
                                res.data[i].idle = res.data[i].idle || {};
                                res.data[i].idle.imgUrl = '';
                            }
                            let content = res.data[i].content || '';
                            let contentList = content.split('<br>');
                            let contenHtml = contentList[0];
                            for(let j=1;j<contentList.length;j++){
                                contenHtml += '  ' + contentList[j];
                            }
                            res.data[i].content = contenHtml;
                        }
                        this.meslist=res.data;
                    } else {
                        this.$message.error('获取我的消息失败');
                    }
                }).catch(err => {
                    console.error("获取我的消息请求失败", err);
                    this.$message.error('获取我的消息请求失败');
                });
            },
            getCookie(cname){
                console.log('Attempting to get cookie:', cname);
                var name = cname + "=";
                var ca = document.cookie.split(';');
                for(var i=0; i<ca.length; i++){
                    var c = ca[i].trim();
                    if (c.indexOf(name)===0) {
                        const cookieValue = c.substring(name.length,c.length);
                        console.log('Cookie found:', cname, 'value:', cookieValue);
                        return cookieValue;
                    }
                }
                console.log('Cookie not found:', cname);
                return "";
            },
            toDetails(id){
                if (!id) {
                    this.$message && this.$message.error('该留言没有关联的商品，无法跳转');
                    return;
                }
                this.$router.push({path: '/details', query: {id}});
            }
        }
    }
</script>

<style scoped>
    .message-container{
        min-height: 85vh;
        padding: 0 20px;
    }
    .message-container-title{
        font-size: 16px;
        padding: 20px 0;
        font-weight: 600;
    }
    .message-container-list{
        cursor:pointer;
        height: 110px;
        border-top: 1px solid #eeeeee;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .message-container-list-left{
        width: 800px;
        display: flex;
    }
    .message-container-list-right{
        width: 130px;
    }
    .message-container-list-text{
        margin-left: 10px;
    }
    .message-nickname{
        font-weight: 600;
        font-size: 18px;
        padding-bottom: 5px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
    }
    .message-content{
        font-size: 16px;
        padding-bottom: 15px;
        color: #555555;
        width: 710px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
    }
    .message-time{
        font-size: 13px;
        color: #555555;
    }
</style>