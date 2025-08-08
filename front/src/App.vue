<template>
    <div id="app">
        <router-view></router-view>
        <AIShoppingAssistant
            @purchase="handleProductPurchase"
            :user-id="currentUserId"
        />
    </div>
</template>
<script>
import AIShoppingAssistant  from "@/components/common/AIShoppingAssistant.vue";
export default {
    components: {
        AIShoppingAssistant
    },
    data() {
        return {
            currentUserId: 1
        }
    },
    methods: {
        handleProductPurchase(product) {
            // 处理商品购买逻辑
            console.log('用户要购买:', product)
            // 可以跳转到商品详情页
            this.$router.push(`/product/${product.id}`)
            // 或者添加到购物车
            this.$store.dispatch('cart/addItem', product)
        }
    }
}
</script>
<style>
    html, body, #app{
        overflow: visible;
        background-color: #f6f6f6;
    }
</style>