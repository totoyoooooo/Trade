<template>
    <div>
        <app-head></app-head>
        <app-body>
            <div style="min-height: 85vh;">
                <el-tabs v-model="labelName" type="card" @tab-click="handleClick">
                    <el-tab-pane label="全部" name="0"></el-tab-pane>
                    <el-tab-pane label="数码" name="1"></el-tab-pane>
                    <el-tab-pane label="家电" name="2"></el-tab-pane>
                    <el-tab-pane label="户外" name="3"></el-tab-pane>
                    <el-tab-pane label="图书" name="4"></el-tab-pane>
                    <el-tab-pane label="其他" name="5"></el-tab-pane>
                </el-tabs>
                <div style="margin: 0 20px;">
                    <el-row :gutter="30">
                        <el-col :span="6" v-for="(idle, index) in idleList">
                            <div class="idle-card" @click="toDetails(idle)"
                                @contextmenu.prevent="showContextMenu($event, idle, index)">
                                <el-image style="width: 100%; height: 160px" :src="idle.imgUrl" fit="contain">
                                    <div slot="error" class="image-slot">
                                        <i class="el-icon-picture-outline">无图</i>
                                    </div>
                                </el-image>
                                <div class="idle-title">
                                    {{ idle.idleName }}
                                </div>
                                <el-row style="margin: 5px 10px;">
                                    <el-col :span="12">
                                        <div class="idle-prive">￥{{ idle.idlePrice }}</div>
                                    </el-col>
                                    <el-col :span="12">
                                        <div class="idle-place">{{ idle.idlePlace }}</div>
                                    </el-col>
                                </el-row>
                                <!-- <div class="idle-time">{{idle.timeStr}}</div>-->
                                <div class="user-info">
                                    <el-image style="width: 30px; height: 30px" :src="idle.user.avatar" fit="contain">
                                        <div slot="error" class="image-slot">
                                            <i class="el-icon-picture-outline">无图</i>
                                        </div>
                                    </el-image>
                                    <div class="user-nickname">{{ idle.user.nickname }}</div>
                                </div>
                            </div>
                        </el-col>
                    </el-row>
                    <div v-if="contextMenu.visible" :style="{
                        position: 'fixed',
                        top: contextMenu.y + 'px',
                        left: contextMenu.x + 'px',
                        background: '#fff',
                        border: '1px solid #eee',
                        boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
                        zIndex: 9999,
                        borderRadius: '4px'
                    }" @click.stop>
                        <div class="context-menu-item" style="padding: 8px 20px; cursor: pointer;"
                            @click="blockItem(contextMenu.item)">屏蔽</div>
                        <div class="context-menu-item" style="padding: 8px 20px; cursor: pointer;"
                            @click="lessRecommend(contextMenu.item)">减少推荐</div>
                    </div>
                </div>
                <div class="fenye">
                    <el-pagination background @current-change="handleCurrentChange" :current-page.sync="currentPage"
                        :page-size="8" layout="prev, pager, next, jumper" :total="totalItem">
                    </el-pagination>
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
    name: "index",
    components: {
        AppHead,
        AppBody,
        AppFoot
    },
    data() {
        return {
            labelName: '0',
            idleList: [],
            currentPage: 1,
            totalItem: 1,
            contextMenu: {
                visible: false,
                x: 0,
                y: 0,
                item: null,
                index: null
            }
        };
    },
    created() {
        this.findIdleTiem(1)
    },
    watch: {
        $route(to, from) {
            this.labelName = to.query.labelName;
            let val = parseInt(to.query.page) ? parseInt(to.query.page) : 1;
            // let totalPage=parseInt(this.totalItem/8)+1;
            // val=parseInt(val%totalPage);
            // val=val===0?totalPage:val;
            this.currentPage = parseInt(to.query.page) ? parseInt(to.query.page) : 1;
            this.findIdleTiem(val);
        }
    },
    methods: {
        findIdleTiem(page) {
            const loading = this.$loading({
                lock: true,
                text: '加载数据中',
                spinner: 'el-icon-loading',
                background: 'rgba(0, 0, 0, 0)'
            });
            if (this.labelName > 0) {
                this.$api.findIdleTiemByLable({
                    idleLabel: this.labelName,
                    page: page,
                    nums: 8
                }).then(res => {
                    //console.log(res);
                    let list = res.data.list;
                    for (let i = 0; i < list.length; i++) {
                        list[i].timeStr = list[i].releaseTime.substring(0, 10) + " " + list[i].releaseTime.substring(11, 19);
                        let pictureList = JSON.parse(list[i].pictureList);
                        list[i].imgUrl = pictureList.length > 0 ? pictureList[0] : '';
                        console.log(list[i].imgUrl);
                    }
                    this.idleList = list;
                    this.totalItem = res.data.count;
                    console.log(this.totalItem);
                }).catch(e => {
                    console.log(e)
                }).finally(() => {
                    loading.close();
                })
            } else {
                this.$api.findIdleTiem({
                    userId: this.getCookie('shUserId'),
                    page: page,
                    nums: 8
                }).then(res => {
                    console.log(res);
                    let list = res.data.list;
                    for (let i = 0; i < list.length; i++) {
                        list[i].timeStr = list[i].releaseTime.substring(0, 10) + " " + list[i].releaseTime.substring(11, 19);
                        let pictureList = JSON.parse(list[i].pictureList);
                        list[i].imgUrl = pictureList.length > 0 ? pictureList[0] : '';
                        console.log(list[i].imgUrl);
                    }
                    this.idleList = list;
                    this.totalItem = res.data.count;
                    console.log(this.totalItem);
                }).catch(e => {
                    console.log(e)
                }).finally(() => {
                    loading.close();
                })
            }
        },
        handleClick(tab, event) {
            // console.log(tab,event);
            console.log(this.labelName);
            this.$router.replace({ query: { page: 1, labelName: this.labelName } });
        },
        handleCurrentChange(val) {
            console.log(`当前页: ${val}`);
            this.$router.replace({ query: { page: val, labelName: this.labelName } });
        },
        toDetails(idle) {
            this.$router.push({ path: '/details', query: { id: idle.id } });
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
        showContextMenu(e, idle, index) {
            this.contextMenu.visible = true;
            this.contextMenu.x = e.clientX;
            this.contextMenu.y = e.clientY;
            this.contextMenu.item = idle;
            this.contextMenu.index = index;
            // 监听全局点击关闭菜单
            document.addEventListener('click', this.hideContextMenu);
        },
        hideContextMenu() {
            this.contextMenu.visible = false;
            document.removeEventListener('click', this.hideContextMenu);
        },
        blockItem(item) {
            this.hideContextMenu();
            this.$api.addShield({
                idleId: item.id,
            }).then(res => {
                this.$message({ type: 'warning', message: `已屏蔽：${item.idleName}` });
                this.findIdleTiem(this.currentPage);
            }).catch(e => {

            });
        },
        lessRecommend(item) {
            this.hideContextMenu();
            this.$api.decreaseRecommendation({
                id: item.id
            }).then(res => {
                this.$message({ type: 'info', message: `已减少推荐：${item.idleName}` });
                this.findIdleTiem(this.currentPage);
            }).catch(e => {
                console.log(e);
            });
        },
    }
}
</script>

<style scoped>
.idle-card {
    height: 300px;
    border: #eeeeee solid 1px;
    margin-bottom: 15px;
    cursor: pointer;
}

.fenye {
    display: flex;
    justify-content: center;
    height: 60px;
    align-items: center;
}

.idle-title {
    font-size: 18px;
    font-weight: 600;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    margin: 10px;
}

.idle-prive {
    font-size: 16px;
    color: red;
}

.idle-place {
    font-size: 13px;
    color: #666666;
    float: right;
    padding-right: 20px;

}

.idle-time {
    color: #666666;
    font-size: 12px;
    margin: 0 10px;
}

.user-nickname {
    color: #999999;
    font-size: 12px;
    display: flex;
    align-items: center;
    height: 30px;
    padding-left: 10px;
}

.user-info {
    margin-top: 10px;
    float: right;
    padding: 5px 10px;
    height: 30px;
    display: flex;
}

.context-menu-item:hover {
    background: #e2e2e4;
}
</style>