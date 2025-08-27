<template>
    <div>
        <app-head></app-head>
        <app-body>
<<<<<<< HEAD
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
=======
            <div class="modern-index-container">
                <!-- 现代化标签导航 -->
                <div class="category-nav">
                    <div class="nav-title">
                        <h2>商品分类</h2>
                        <p>发现你需要的好物</p>
                    </div>
                    <div class="category-tabs">
                        <button 
                            v-for="(category, index) in categories" 
                            :key="category.value"
                            :class="['category-btn', { active: labelName === category.value }]"
                            @click="handleCategoryClick(category.value)">
                            <i :class="category.icon"></i>
                            <span>{{ category.label }}</span>
                        </button>
                    </div>
                </div>

                <!-- 商品网格 -->
                <div class="products-section">
                    <div class="products-header">
                        <h3>{{ getCurrentCategoryName() }}</h3>
                        <div class="products-count">共 {{ totalItem }} 件商品</div>
                    </div>
                    
                    <div class="products-grid">
                        <div 
                            v-for="idle in idleList" 
                            :key="idle.id"
                            class="product-card"
                            @click="toDetails(idle)">
                            <!-- 商品图片 -->
                            <div class="product-image">
                                <el-image
                                    :src="idle.imgUrl"
                                    fit="cover"
                                    class="product-img">
                                    <div slot="error" class="image-placeholder">
                                        <i class="el-icon-picture-outline"></i>
                                        <p>暂无图片</p>
                                    </div>
                                </el-image>
                                <div class="product-overlay">
                                    <button class="quick-view-btn">
                                        <i class="el-icon-view"></i>
                                    </button>
                                </div>
                            </div>

                            <!-- 商品信息 -->
                            <div class="product-info">
                                <h4 class="product-title">{{ idle.idleName }}</h4>
                                
                                <div class="product-details">
                                    <div class="price-section">
                                        <span class="price">¥{{ idle.idlePrice }}</span>
                                    </div>
                                    <div class="location">
                                        <i class="el-icon-location-outline"></i>
                                        {{ idle.idlePlace }}
                                    </div>
                                </div>

                                <!-- 用户信息 -->
                                <div class="seller-info">
                                    <div class="seller-avatar">
                                        <el-avatar 
                                            :src="idle.user.avatar" 
                                            :size="24"
                                            class="avatar">
                                            <i class="el-icon-user-solid"></i>
                                        </el-avatar>
                                    </div>
                                    <span class="seller-name">{{ idle.user.nickname }}</span>
                                    <div class="product-actions">
                                        <button class="action-btn favorite">
                                            <i class="el-icon-star-off"></i>
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 加载状态 -->
                    <div v-if="loading" class="loading-grid">
                        <div v-for="n in 8" :key="n" class="loading-card">
                            <div class="loading-shimmer loading-image"></div>
                            <div class="loading-content">
                                <div class="loading-shimmer loading-title"></div>
                                <div class="loading-shimmer loading-price"></div>
                                <div class="loading-shimmer loading-user"></div>
                            </div>
                        </div>
                    </div>

                    <!-- 分页 -->
                    <div class="modern-pagination">
                        <el-pagination
                            background
                            @current-change="handleCurrentChange"
                            :current-page.sync="currentPage"
                            :page-size="8"
                            layout="prev, pager, next, jumper"
                            :total="totalItem"
                            class="custom-pagination">
                        </el-pagination>
                    </div>
                </div>
>>>>>>> feature/ui-improvements
            </div>
            <app-foot></app-foot>
        </app-body>
    </div>
</template>

<script>
import AppHead from '../common/AppHeader.vue';
import AppBody from '../common/AppPageBody.vue'
import AppFoot from '../common/AppFoot.vue'

<<<<<<< HEAD
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
=======
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
                loading: false,
                categories: [
                    { value: '0', label: '全部', icon: 'el-icon-menu' },
                    { value: '1', label: '数码', icon: 'el-icon-mobile-phone' },
                    { value: '2', label: '家电', icon: 'el-icon-refrigerator' },
                    { value: '3', label: '户外', icon: 'el-icon-bicycle' },
                    { value: '4', label: '图书', icon: 'el-icon-reading' },
                    { value: '5', label: '其他', icon: 'el-icon-more-outline' }
                ]
            };
        },
        created() {
            this.findIdleTiem(1)
        },
        watch:{
            $route(to,from){
                this.labelName=to.query.labelName;
                let val=parseInt(to.query.page)?parseInt(to.query.page):1;
                // let totalPage=parseInt(this.totalItem/8)+1;
                // val=parseInt(val%totalPage);
                // val=val===0?totalPage:val;
                this.currentPage=parseInt(to.query.page)?parseInt(to.query.page):1;
                this.findIdleTiem(val);
            }
        },
        methods: {
            getCurrentCategoryName() {
                const category = this.categories.find(cat => cat.value === this.labelName);
                return category ? category.label : '全部';
            },
            handleCategoryClick(value) {
                this.labelName = value;
                this.$router.replace({query: {page: 1, labelName: value}});
            },
            findIdleTiem(page){
                this.loading = true;
                if(this.labelName > 0){
                    this.$api.findIdleTiemByLable({
                        idleLabel:this.labelName,
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
                        this.totalItem=res.data.count;
                        console.log(this.totalItem);
                    }).catch(e => {
                        console.log(e)
                    }).finally(()=>{
                        this.loading = false;
                    })
                }else{
                    this.$api.findIdleTiem({
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
                        this.totalItem=res.data.count;
                        console.log(this.totalItem);
                    }).catch(e => {
                        console.log(e)
                    }).finally(()=>{
                        this.loading = false;
                    })
                }
            },
            handleClick(tab, event) {
                // console.log(tab,event);
                console.log(this.labelName);
                this.$router.replace({query: {page: 1,labelName:this.labelName}});
            },
            handleCurrentChange(val) {
                console.log(`当前页: ${val}`);
                this.$router.replace({query: {page: val,labelName:this.labelName}});
            },
            toDetails(idle) {
                this.$router.push({path: '/details', query: {id: idle.id}});
>>>>>>> feature/ui-improvements
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
<<<<<<< HEAD
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
=======
.modern-index-container {
    padding: 2rem;
    min-height: calc(100vh - 120px);
    animation: fadeIn 0.6s ease-out;
}

/* Category Navigation */
.category-nav {
    margin-bottom: 3rem;
}

.nav-title {
    text-align: center;
    margin-bottom: 2rem;
}

.nav-title h2 {
    font-size: 2rem;
    font-weight: 700;
    background: var(--primary-gradient);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin: 0 0 0.5rem 0;
}

.nav-title p {
    color: var(--text-secondary);
    font-size: 1rem;
    margin: 0;
}

.category-tabs {
    display: flex;
    justify-content: center;
    gap: 1rem;
    flex-wrap: wrap;
}

.category-btn {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1.5rem;
    border: 2px solid transparent;
    border-radius: var(--radius-full);
    background: rgba(255, 255, 255, 0.8);
    color: var(--text-primary);
    font-weight: 500;
    cursor: pointer;
    transition: all var(--transition-normal);
    box-shadow: var(--shadow-sm);
}

.category-btn:hover {
    background: rgba(255, 255, 255, 0.95);
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
}

.category-btn.active {
    background: var(--primary-gradient);
    color: white;
    border-color: transparent;
    box-shadow: var(--shadow-lg);
}

.category-btn i {
    font-size: 1.1rem;
}

/* Products Section */
.products-section {
    margin-bottom: 2rem;
}

.products-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    padding: 0 0.5rem;
}

.products-header h3 {
    font-size: 1.5rem;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0;
}

.products-count {
    color: var(--text-secondary);
    font-size: 0.9rem;
}

/* Products Grid */
.products-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 1.5rem;
    margin-bottom: 3rem;
}

.product-card {
    background: rgba(255, 255, 255, 0.95);
    border-radius: var(--radius-xl);
    overflow: hidden;
    cursor: pointer;
    transition: all var(--transition-normal);
    box-shadow: var(--shadow-md);
    position: relative;
    animation: slideInUp 0.6s ease-out;
}

.product-card:hover {
    transform: translateY(-8px);
    box-shadow: var(--shadow-2xl);
}

/* Product Image */
.product-image {
    position: relative;
    height: 200px;
    overflow: hidden;
}

.product-img {
    width: 100%;
    height: 100%;
    transition: transform var(--transition-normal);
}

.product-card:hover .product-img {
    transform: scale(1.05);
}

.image-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    background: var(--bg-primary);
    color: var(--text-light);
}

.image-placeholder i {
    font-size: 2rem;
    margin-bottom: 0.5rem;
}

.image-placeholder p {
    margin: 0;
    font-size: 0.9rem;
}

.product-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity var(--transition-normal);
}

.product-card:hover .product-overlay {
    opacity: 1;
}

.quick-view-btn {
    background: rgba(255, 255, 255, 0.9);
    border: none;
    border-radius: var(--radius-full);
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all var(--transition-normal);
    color: var(--text-primary);
}

.quick-view-btn:hover {
    background: white;
    transform: scale(1.1);
}

/* Product Info */
.product-info {
    padding: 1.25rem;
}

.product-title {
    font-size: 1.1rem;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 1rem 0;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.4;
}

.product-details {
    margin-bottom: 1rem;
}

.price-section {
    margin-bottom: 0.5rem;
}

.price {
    font-size: 1.25rem;
    font-weight: 700;
    background: var(--secondary-gradient);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.location {
    display: flex;
    align-items: center;
    gap: 0.25rem;
    color: var(--text-secondary);
    font-size: 0.9rem;
}

.location i {
    font-size: 0.85rem;
}

/* Seller Info */
.seller-info {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding-top: 1rem;
    border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.seller-avatar {
    flex-shrink: 0;
}

.avatar {
    border: 2px solid rgba(255, 255, 255, 0.8);
    box-shadow: var(--shadow-sm);
}

.seller-name {
    flex: 1;
    font-size: 0.9rem;
    color: var(--text-secondary);
    font-weight: 500;
}

.product-actions {
    display: flex;
    gap: 0.5rem;
}

.action-btn {
    width: 32px;
    height: 32px;
    border: none;
    border-radius: var(--radius-lg);
    background: rgba(255, 255, 255, 0.8);
    color: var(--text-secondary);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all var(--transition-normal);
}

.action-btn:hover {
    background: var(--primary-color);
    color: white;
    transform: scale(1.1);
}

/* Loading States */
.loading-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 1.5rem;
    margin-bottom: 3rem;
}

.loading-card {
    background: rgba(255, 255, 255, 0.95);
    border-radius: var(--radius-xl);
    overflow: hidden;
    box-shadow: var(--shadow-md);
}

.loading-image {
    height: 200px;
}

.loading-content {
    padding: 1.25rem;
}

.loading-title {
    height: 1.5rem;
    margin-bottom: 1rem;
    border-radius: var(--radius-sm);
}

.loading-price {
    height: 1.25rem;
    margin-bottom: 0.5rem;
    border-radius: var(--radius-sm);
    width: 60%;
}

.loading-user {
    height: 1rem;
    border-radius: var(--radius-sm);
    width: 40%;
}

/* Pagination */
.modern-pagination {
    display: flex;
    justify-content: center;
    padding: 2rem 0;
}

.custom-pagination >>> .el-pagination {
    background: transparent;
}

.custom-pagination >>> .el-pager li {
    background: rgba(255, 255, 255, 0.8);
    border-radius: var(--radius-lg);
    margin: 0 0.25rem;
    min-width: 40px;
    height: 40px;
    transition: all var(--transition-normal);
}

.custom-pagination >>> .el-pager li:hover {
    background: var(--primary-color);
    color: white;
    transform: translateY(-2px);
}

.custom-pagination >>> .el-pager li.active {
    background: var(--primary-gradient);
    color: white;
}

.custom-pagination >>> .btn-prev,
.custom-pagination >>> .btn-next {
    background: rgba(255, 255, 255, 0.8);
    border-radius: var(--radius-lg);
    margin: 0 0.25rem;
    transition: all var(--transition-normal);
}

.custom-pagination >>> .btn-prev:hover,
.custom-pagination >>> .btn-next:hover {
    background: var(--primary-color);
    color: white;
    transform: translateY(-2px);
}

/* Responsive Design */
@media (max-width: 768px) {
    .modern-index-container {
        padding: 1rem;
    }
    
    .products-grid {
        grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
        gap: 1rem;
    }
    
    .category-tabs {
        gap: 0.5rem;
    }
    
    .category-btn {
        padding: 0.6rem 1rem;
        font-size: 0.9rem;
    }
    
    .nav-title h2 {
        font-size: 1.75rem;
    }
    
    .products-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.5rem;
    }
}

@media (max-width: 480px) {
    .products-grid {
        grid-template-columns: 1fr;
    }
    
    .category-tabs {
        justify-content: flex-start;
        overflow-x: auto;
        padding-bottom: 0.5rem;
    }
    
    .category-btn {
        flex-shrink: 0;
    }
}

/* Animation delays for staggered effect */
.product-card:nth-child(1) { animation-delay: 0.1s; }
.product-card:nth-child(2) { animation-delay: 0.2s; }
.product-card:nth-child(3) { animation-delay: 0.3s; }
.product-card:nth-child(4) { animation-delay: 0.4s; }
.product-card:nth-child(5) { animation-delay: 0.5s; }
.product-card:nth-child(6) { animation-delay: 0.6s; }
.product-card:nth-child(7) { animation-delay: 0.7s; }
.product-card:nth-child(8) { animation-delay: 0.8s; }
>>>>>>> feature/ui-improvements
</style>