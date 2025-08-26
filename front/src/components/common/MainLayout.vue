<template>
    <div>
        <AppHeader />
        <router-view></router-view>
    </div>
</template>

<script>
import AppHeader from './AppHeader.vue';

export default {
    name: 'MainLayout',
    components: {
        AppHeader
    }
};
</script>

<style scoped>
/* 可以在这里添加针对布局的全局样式，例如内容区域的padding */
/* body { margin-top: 70px; } */ /* 确保内容不会被固定头遮挡 */
</style>
