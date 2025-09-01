import { data } from 'jquery';
import request from '../utils/request';

const api = {
    userLogin(query) {
        return request({
            url: '/user/login',
            method: 'get',
            params: query
        });
    },
    logout(query) {
        return request({
            url: '/user/logout',
            method: 'get',
            params: query
        });
    },
    signIn(data) {
        return request({
            url: '/user/sign-in',
            method: 'post',
            data: data
        });
    },
    getUserInfo() {
        return request({
            url: '/user/info',
            method: 'get',
        });
    },
    getUser(query) {
        return request({
            url: '/user/getUser',
            method: 'post',
            data: query
        });
    },
    updateUserPublicInfo(data) {
        return request({
            url: '/user/info',
            method: 'post',
            data: data
        });
    },
    updatePassword(query) {
        return request({
            url: '/user/password',
            method: 'get',
            params: query
        });
    },
    updateTrade(query) {
        return request({
            url: '/user/order',
            method: 'post',
            data: query
        });
    },
    addAddress(data) {
        return request({
            url: '/address/add',
            method: 'post',
            data: data
        });
    },
    getAddress(query) {
        return request({
            url: '/address/info',
            method: 'get',
            params: query
        });
    },
    updateAddress(data) {
        return request({
            url: '/address/update',
            method: 'post',
            data: data
        });
    },
    deleteAddress(data) {
        return request({
            url: '/address/delete',
            method: 'post',
            data: data
        });
    },
    addIdleItem(data) {
        return request({
            url: '/idle/add',
            method: 'post',
            data: data
        });
    },
    getIdleItem(query) {
        return request({
            url: '/idle/info',
            method: 'get',
            params: query
        });
    },
    getAllIdleItem(query) {
        return request({
            url: '/idle/all',
            method: 'get',
            params: query
        });
    },
    getUserIdleItem(query) {
        return request({
            url: '/idle/getUserIdle',
            method: 'post',
            data: query
        });
    },
    findIdleTiem(query) {
        return request({
            url: '/idle/find',
            method: 'get',
            params: query
        });
    },
    findIdleTiemByLable(query) {
        return request({
            url: '/idle/lable',
            method: 'get',
            params: query
        });
    },
    updateIdleItem(data) {
        return request({
            url: '/idle/update',
            method: 'post',
            data: data
        });
    },
    addOrder(data) {
        return request({
            url: '/order/add',
            method: 'post',
            data: data
        });
    },
    getOrder(query) {
        return request({
            url: '/order/info',
            method: 'get',
            params: query
        });
    },
    updateOrder(data) {
        return request({
            url: '/order/update',
            method: 'post',
            data: data
        });
    },
    getMyOrder(query) {
        return request({
            url: '/order/my',
            method: 'get',
            params: query
        });
    },
    getMySoldIdle(query) {
        return request({
            url: '/order/my-sold',
            method: 'get',
            params: query
        });
    },
    addOrderAddress(data) {
        return request({
            url: '/order-address/add',
            method: 'post',
            data: data
        });
    },
    updateOrderAddress(data) {
        return request({
            url: '/order-address/update',
            method: 'post',
            data: data
        });
    },
    getOrderAddress(query) {
        return request({
            url: '/order-address/info',
            method: 'get',
            params: query
        });
    },
    addFavorite(data) {
        return request({
            url: '/favorite/add',
            method: 'post',
            data: data
        });
    },
    getMyFavorite(query) {
        return request({
            url: '/favorite/my',
            method: 'get',
            params: query
        });
    },
    deleteFavorite(query) {
        return request({
            url: '/favorite/delete',
            method: 'get',
            params: query
        });
    },
    checkFavorite(query) {
        return request({
            url: '/favorite/check',
            method: 'get',
            params: query
        });
    },
    sendMessage(data) {
        return request({
            url: '/message/send',
            method: 'post',
            data: data
        });
    },
    getMessage(query) {
        return request({
            url: '/message/info',
            method: 'get',
            params: query
        });
    },
    getAllIdleMessage(query) {
        return request({
            url: '/message/idle',
            method: 'get',
            params: query
        });
    },
    getAllMyMessage(query) {
        return request({
            url: '/message/my',
            method: 'get',
            params: query
        });
    },
    deleteMessage(query) {
        return request({
            url: '/message/delete',
            method: 'get',
            params: query
        });
    },
    // Wanted Item APIs
    addWantedItem(data) {
        return request({
            url: '/wantedItem',
            method: 'post',
            data: data
        });
    },
    getWantedItemList(query) {
        return request({
            url: '/wantedItem',
            method: 'get',
            params: query
        });
    },
    getWantedItemById(id) {
        return request({
            url: `/wantedItem/${id}`,
            method: 'get'
        });
    },
    updateWantedItem(data) {
        return request({
            url: '/wantedItem',
            method: 'put',
            data: data
        });
    },
    deleteWantedItem(id) {
        return request({
            url: `/wantedItem/${id}`,
            method: 'delete'
        });
    },
    getGoods(query) {
        return request({
            url: '/admin/idleList',
            method: 'get',
            params: query
        });
    },
    updateGoods(query) {
        return request({
            url: '/admin/updateIdleStatus',
            method: 'get',
            params: query
        });
    },

    getOrderList(query) {
        return request({
            url: '/admin/orderList',
            method: 'get',
            params: query
        });
    },
    deleteOrder(query) {
        return request({
            url: '/admin/deleteOrder',
            method: 'get',
            params: query
        });
    },
    getUserData(query) {
        return request({
            url: '/admin/userList',
            method: 'get',
            params: query
        });
    },
    getUserManage(query) {
        return request({
            url: '/admin/list',
            method: 'get',
            params: query
        });
    },
    updateUserStatus(query){
        return request({
            url: '/admin/updateUserStatus',
            method: 'get',
            params: query
        });
    },
    regAdministrator(data){
        return request({
            url: '/admin/add',
            method: 'post',
            data: data
        });
    },
    adminLogin(query) {
        return request({
            url: '/admin/login',
            method: 'get',
            params: query
        });
    },
    loginOut(query) {
        return request({
            url: '/admin/loginOut',
            method: 'get',
            params: query
        });
    },
    addAndGetChat(query) {
        return request({
            url: '/chat/addAndGetChat',
            method: 'post',
            data: query
        });
    },
    getChatList(query) {
        return request({
            url: '/chat/getChatList',
            method: 'post',
            params: query
        });
    },
    openChat(query) {
        return request({
            url: '/chat/openChat',
            method: 'post',
            data: query
        });
    },
    sendChatMessage(query) {
        return request({
            url: '/chat/sendMessage',
            method: 'post',
            data: query
        });
    },
    clearUnread(query) {
        return request({
            url: '/chat/clearUnread',
            method: 'post',
            params: query
        });
    },
    revokeMessage(query) {
        return request({
            url: '/chat/revokeMessage',
            method: 'post',
            data: query
        });
    },
    clearMessageUnread(query) {
        return request({
            url: '/message/clearUnread',
            method: 'post',
            params: query
        });
    },
    createTag(query) {
        return request({
            url: '/tag/createTag',
            method: 'post',
            data: query
        });
    },
    getAkinTag(query) {
        return request({
            url: '/tag/getAkinTag',
            method: 'get',
            params: query
        });
    },
    getAllTag(query) {
        return request({
            url: '/tag/getAllTag',
            method: 'get',
            params: query
        });
    },
    addShield(data) {
        return request({
            url: '/shield/add',
            method: 'post',
            data: data
        });
    },
    getMyShield(query) {
        return request({
            url: '/shield/my',
            method: 'get',
            params: query
        });
    },
    deleteShield(query) {
        return request({
            url: '/shield/delete',
            method: 'get',
            params: query
        });
    },
    checkShield(query) {
        return request({
            url: '/shield/check',
            method: 'get',
            params: query
        });
    },
    decreaseRecommendation(query) {
        return request({
            url: '/shield/decrease',
            method: 'get',
            params: query
        });
    },
    callShoppingAIAgent(data) {
        return request({
            url: '/ai-agent/chat',
            method: 'post',
            data,
            timeout: 60000
        })
    }
};

const webSocket = {
    ws: null,
    init(url){
        this.ws = new WebSocket(url);
        this.ws.onopen = () => {
            console.log('WebSocket connection opened');
        };
        this.ws.onmessage = (event) => {
            if (window.Vue && window.Vue.prototype && window.Vue.prototype.$bus) {
                if(JSON.parse(event.data).type == "chat"){
                    window.Vue.prototype.$bus.$emit('new-message', JSON.parse(event.data).content);
                }else if(JSON.parse(event.data).type == "message"){
                    window.Vue.prototype.$bus.$emit('new-leave-message', JSON.parse(event.data).content);
                }else if(JSON.parse(event.data).type == "revoke"){
                    window.Vue.prototype.$bus.$emit('revoke-message', JSON.parse(event.data).content);
                }else if(JSON.parse(event.data).type == "online"){
                    window.Vue.prototype.$bus.$emit('online');
                }
            }
        };
    },
    sendMessage(data) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(data));
        } else {
            console.error('WebSocket is not open');
        }
    },
    closeWebSocket(){
         if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.close();
        } else {
            console.error('WebSocket is not open');
        }
    },
};

export default api;
export { webSocket };