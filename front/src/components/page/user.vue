<template>
    <div>
        <app-head></app-head>
        <app-body>
            <div>
                <div class="user-info-container">
                    <div class="user-info-details">
                        <el-image
                            style="width: 120px; height: 120px;border-radius: 10px;"
                            :src="userInfo.avatar"
                            fit="contain">
                        </el-image>
                        <div class="user-info-details-text">
                            <div class="user-info-details-text-nickname">{{userInfo.nickname}}</div>
                            <div class="user-info-details-text-time">{{userInfo.signInTime}} 加入平台</div>
                            <div class="user-info-details-text-edit">
                                <el-button type="primary" plain @click="toChat()">私信</el-button>
                                <div class="user-info-trade-info">
                                <div class="trade-count">交易次数：{{ userInfo.tradeCount || 0 }}</div>
                                <div class="applause-rate">好评率：{{ userInfo.applauseRate != null ? userInfo.applauseRate + '%' : '0%' }}</div>
    </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="idle-container">
                    <div class="idle-container-list">
                        <div v-for="(item,index) in dataList[activeName-1]" class="idle-container-list-item">
                            <div class="idle-container-list-item-detile" @click="toDetails(activeName,item)">
                                <el-image
                                        style="width: 100px; height: 100px;"
                                        :src="item.imgUrl"
                                        fit="cover">
                                    <div slot="error" class="image-slot">
                                        <i class="el-icon-picture-outline">无图</i>
                                    </div>
                                </el-image>
                                <div class="idle-container-list-item-text">
                                    <div class="idle-container-list-title">
                                        {{item.idleName}}
                                    </div>
                                    <div class="idle-container-list-idle-details" v-html="item.idleDetails">
                                    </div>
                                    <div class="idle-container-list-idle-time">{{item.timeStr}}</div>

                                    <div class="idle-item-foot">
                                        <div class="idle-prive">￥{{item.idlePrice}}
                                            {{(activeName==='4'||activeName==='5')?orderStatus[item.orderStatus]:''}}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
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
        name: "me",
        components: {
            AppHead,
            AppBody,
            AppFoot
        },
        data() {
            return {
                activeName: '1',
                dataList: [
                    [],
                    [],
                    [],
                    [],
                    [],
                    [],
                    [],
                    [],
                ],
                userInfo: {
                    accountNumber: "",
                    avatar: "",
                    nickname: "",
                    signInTime: "",
                },
            };
        },
        created() {
            this.$api.getUser(
                "id=" + this.$route.query.id,
            ).then(res => {
                if (res.status_code === 200) {
                    this.userInfo = res.data;
                }
            });
            this.getIdleItemData();
        },
        methods: {
            getIdleItemData() {
                this.$api.getUserIdleItem(
                    "id=" + this.$route.query.id,
                ).then(res => {
                    console.log(res);
                    if (res.status_code === 200) {
                        for (let i = 0; i < res.data.length; i++) {
                            res.data[i].timeStr = res.data[i].releaseTime.substring(0, 10) + " " + res.data[i].releaseTime.substring(11, 19);
                            let pictureList = JSON.parse(res.data[i].pictureList);
                            res.data[i].imgUrl = pictureList.length > 0 ? pictureList[0] : '';
                            if (res.data[i].idleStatus === 1) {
                                this.dataList[0].push(res.data[i]);
                            } else if (res.data[i].idleStatus === 2) {
                                this.dataList[1].push(res.data[i]);
                            }
                        }
                    }
                })
            },
            toDetails(activeName, item) {
                if (activeName === '4'||activeName === '5') {
                    this.$router.push({path: '/order', query: {id: item.id}});
                } else {
                    this.$router.push({path: '/details', query: {id: item.id}});
                }
            },
            toChat(userId){
                this.$router.push({ path: '/chat', query: {id: this.$route.query.id} });
            }
        }
    }
</script>

<style scoped>
    .user-info-container {
        width: 100%;
        height: 200px;
        border-bottom: 15px solid #f6f6f6;
        display: flex;
        align-items: center;
        justify-content: space-between;
    }

    .user-info-details {
        display: flex;
        height: 140px;
        align-items: center;
        margin: 20px 40px;
    }

    .user-info-details-text {
        margin-left: 20px;
    }

    .user-info-details-text-nickname {
        font-size: 26px;
        font-weight: 600;
        margin: 10px 0;
    }

    .user-info-details-text-time {
        font-size: 14px;
        margin-bottom: 10px;
    }

    .user-info-splace {
        margin-right: 90px;
    }

    .idle-container {
        padding: 0 20px;
    }

    .idle-container-list {
        min-height: 55vh;
    }

    .idle-container-list-item {
        border-bottom: 1px solid #eeeeee;
        cursor: pointer;
    }

    .idle-container-list-item:last-child {
        border-bottom: none;
    }

    .idle-container-list-item-detile {
        height: 120px;
        display: flex;
        align-items: center;
    }

    .idle-container-list-item-text {
        margin-left: 10px;
        height: 100px;
        max-width: 800px;
    }

    .idle-container-list-title {
        font-weight: 600;
        font-size: 18px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
    }

    .idle-container-list-idle-details {
        font-size: 14px;
        color: #555555;
        padding-top: 5px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
    }

    .idle-container-list-idle-time {
        font-size: 13px;
        padding-top: 5px;
    }

    .idle-prive {
        font-size: 15px;
        padding-top: 5px;
        color: red;
    }

    .edit-tip {
        font-size: 14px;
        margin: 10px 5px;
    }

    .address-container {
        padding: 10px 20px;
    }

    .address-container-back {
        margin-bottom: 10px;
    }

    .address-container-add-title {
        font-size: 15px;
        color: #409EFF;
        padding: 10px;
    }

    .address-container-add-item {
        margin-bottom: 20px;
    }

    .demonstration {
        color: #666666;
        font-size: 14px;
        padding: 10px;
    }

    .address-container-add {
        padding: 0 200px;
    }

    .address-container-list {
        padding: 30px 100px;
    }

    .idle-item-foot {
        width: 800px;
        display: flex;
        justify-content: space-between;
    }
    .user-info-trade-info {
    display: inline-block;
    vertical-align: top;
    margin-left: 20px;
    text-align: left;
    }
    .trade-count {
        font-size: 14px;
        color: #333;
    }
    .applause-rate {
        font-size: 13px;
        margin-top: 2px;
    }
</style>