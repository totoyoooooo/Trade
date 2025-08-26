import Vue from 'vue';
import Router from 'vue-router';
import MainLayout from '../components/common/MainLayout.vue'; // 导入 MainLayout

const originalReplace = Router.prototype.replace;
Router.prototype.replace = function replace(location) {
    return originalReplace.call(this, location).catch(err => err);
};
const originalPush = Router.prototype.push
Router.prototype.push = function push(location) {
    return originalPush.call(this, location).catch(err => err)
};

Vue.use(Router);

export default new Router({
    routes: [
        {
            path: '/login',
            component: () => import('../components/page/login.vue'),
            meta: { title: '登录 | 二手物品交易平台' }
        },
        {
            path: '/sign-in',
            component: () => import('../components/page/sign-in.vue'),
            meta: { title: '注册 | 二手物品交易平台' }
        },
        {
            path: '/login-admin',
            component: () => import('../components/page/login-admin.vue'),
            meta: { title: '后台登陆' }
        },
        {
            path: '/platform-admin',
            component: () => import('../components/page/platform-admin.vue'),
            meta: { title: '后台管理' }
        },
        {
            path: '/',
            component: MainLayout, // 使用 MainLayout 作为主布局组件
            children: [
                {
                    path: '', // 默认子路由
                    redirect: '/index'
                },
                {
                    path: '/index',
                    component: () => import('../components/page/index.vue'),
                    meta: { title: '二手物品交易平台' }
                },
                {
                    path: '/search',
                    component: () => import('../components/page/search.vue'),
                    meta: { title: '闲置二手物品 | 二手物品交易平台' }
                },
                {
                    path: '/me',
                    component: () => import('../components/page/me.vue'),
                    meta: { title: '个人中心 | 二手物品交易平台' }
                },
                {
                    path: '/message',
                    component: () => import('../components/page/message.vue'),
                    meta: { title: '消息 | 二手物品交易平台' }
                },
                {
                    path: '/release',
                    component: () => import('../components/page/release.vue'),
                    meta: { title: '发布二手物品 | 二手物品交易平台' }
                },
                {
                    path: '/details',
                    component: () => import('../components/page/idle-details.vue'),
                    meta: { title: '二手物品详情 | 二手物品交易平台' }
                },
                {
                    path: '/order',
                    component: () => import('../components/page/order.vue'),
                    meta: { title: '订单详情 | 二手物品交易平台' }
                },
                {
                    path: '/chat',
                    component: () => import('../components/page/chat.vue'),
                    meta: { title: '聊天 | 二手物品交易平台' }
                },
                {
                    path: '/user',
                    component: () => import('../components/page/user.vue'),
                    meta: { title: '用户界面 | 二手物品交易平台' }
                },
                {
                    path: '/wanteditemlist',
                    component: () => import('../components/page/WantedItemListPage.vue'),
                    meta: { title: '求购列表 | 二手物品交易平台' }
                },
                {
                    path: '/addwanteditem',
                    component: () => import('../components/page/AddWantedItemPage.vue'),
                    meta: { title: '发布/编辑求购 | 二手物品交易平台' }
                },
                {
                    path: '/wanteditemdetail',
                    component: () => import('../components/page/WantedItemDetailPage.vue'),
                    meta: { title: '求购详情 | 二手物品交易平台' }
                }
            ]
        },
        {
            path: '*',
            redirect: '/'
        }
    ]
});
