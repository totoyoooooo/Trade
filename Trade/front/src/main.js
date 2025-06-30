import Vue from 'vue';
import App from './App.vue';
import router from './router';
import ElementUI from 'element-ui';
import $ from 'jquery'
import 'element-ui/lib/theme-chalk/index.css';
import 'babel-polyfill';

import api from './api/index.js';
import { webSocket } from '@/api';
import bus from './components/common/bus.js'
Vue.prototype.$bus = bus 
// 引入webSocket
Vue.prototype.$api = api;
Vue.prototype.$webSocket = webSocket; // 挂载到全局

let globalData={
    userInfo:{
        nickname:''
    },
};

let sta={
    isLogin:false,
    adminName:''
};
Vue.prototype.$sta = sta;

Vue.prototype.$globalData = globalData;

Vue.config.productionTip = false;

Vue.use(ElementUI, {
    size: 'medium'
});


router.beforeEach((to, from, next) => {
    // 需要登录的页面路径
    const needLoginPaths = ['/me', '/release', '/message', '/order', '/chat'];
    // 判断是否需要登录
    if (needLoginPaths.includes(to.path)) {
        // 检查 cookie 是否有 shUserId
        function getCookie(name) {
            const value = "; " + document.cookie;
            const parts = value.split("; " + name + "=");
            if (parts.length === 2) return parts.pop().split(";").shift();
            return "";
        }
        const userId = getCookie('shUserId');
        if (!userId) {
            next('/login');
            return;
        }
    }
    next();
});

window.Vue = Vue;

new Vue({
    router,
    render: h => h(App)
}).$mount('#app');
