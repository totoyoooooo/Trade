<template>
    <div class="modern-main-container">
        <div class="main-content">
            <slot></slot>
        </div>
    </div>
</template>

<script>
    export default {
        name: "PageBody",
    };
</script>

<style scoped>
.modern-main-container {
    margin-top: 80px;
    display: flex;
    justify-content: center;
    min-height: calc(100vh - 80px);
    padding: 1rem;
    animation: fadeIn 0.6s ease-out;
}

.main-content {
    width: 100%;
    max-width: 1200px;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: var(--radius-2xl);
    box-shadow: var(--shadow-xl);
    min-height: calc(100vh - 120px);
    overflow: hidden;
    transition: all var(--transition-normal);
    position: relative;
}

.main-content::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, 
        transparent 0%, 
        rgba(255, 255, 255, 0.4) 50%, 
        transparent 100%);
}

/* Responsive adjustments */
@media (max-width: 768px) {
    .modern-main-container {
        margin-top: 70px;
        padding: 0.5rem;
    }
    
    .main-content {
        border-radius: var(--radius-xl);
        min-height: calc(100vh - 90px);
    }
}

@media (max-width: 480px) {
    .modern-main-container {
        margin-top: 65px;
        padding: 0.25rem;
    }
    
    .main-content {
        border-radius: var(--radius-lg);
    }
}

@keyframes fadeIn {
    from { 
        opacity: 0; 
        transform: translateY(20px);
    }
    to { 
        opacity: 1; 
        transform: translateY(0);
    }
}
</style>