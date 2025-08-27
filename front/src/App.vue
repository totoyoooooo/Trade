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
@import url('./assets/styles/modern-design.css');
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

html, body, #app {
    overflow: visible;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    min-height: 100vh;
    font-family: 'Inter', sans-serif;
    margin: 0;
    padding: 0;
}

#app {
    position: relative;
}

#app::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
        radial-gradient(circle at 20% 50%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.3) 0%, transparent 50%),
        radial-gradient(circle at 40% 80%, rgba(120, 219, 255, 0.3) 0%, transparent 50%);
    pointer-events: none;
    z-index: -1;
}

/* Global smooth scrolling */
* {
    scroll-behavior: smooth;
}

/* Improved focus styles */
*:focus {
    outline: 2px solid var(--primary-color);
    outline-offset: 2px;
}

/* Modern selection styles */
::selection {
    background: var(--primary-color);
    color: white;
}

::-moz-selection {
    background: var(--primary-color);
    color: white;
}
</style>