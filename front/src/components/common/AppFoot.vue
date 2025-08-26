<template>
    <div class="modern-footer">
        <div class="footer-content">
            <!-- Logo Section -->
            <div class="footer-brand">
                <div class="footer-logo">
                    <div class="logo-icon">
                        <i class="el-icon-shopping-bag-2"></i>
                    </div>
                    <div class="brand-text">
                        <span class="brand-name">校园交易</span>
                        <span class="brand-tagline">让闲置物品焕发新生</span>
                    </div>
                </div>
            </div>

            <!-- Links Section -->
            <div class="footer-links">
                <div class="link-group">
                    <h4>平台服务</h4>
                    <ul>
                        <li><a href="#" @click.prevent>交易流程</a></li>
                        <li><a href="#" @click.prevent>安全保障</a></li>
                        <li><a href="#" @click.prevent>服务条款</a></li>
                    </ul>
                </div>
                <div class="link-group">
                    <h4>帮助中心</h4>
                    <ul>
                        <li><a href="#" @click.prevent>常见问题</a></li>
                        <li><a href="#" @click.prevent>联系客服</a></li>
                        <li><a href="#" @click.prevent>意见反馈</a></li>
                    </ul>
                </div>
                <div class="link-group">
                    <h4>关于我们</h4>
                    <ul>
                        <li><a href="#" @click.prevent>公司介绍</a></li>
                        <li><a href="#" @click.prevent>加入我们</a></li>
                        <li><a href="#" @click.prevent>合作伙伴</a></li>
                    </ul>
                </div>
            </div>

            <!-- Contact Section -->
            <div class="footer-contact">
                <h4>联系方式</h4>
                <div class="contact-item">
                    <i class="el-icon-phone"></i>
                    <span>客服热线: 114-514-1919</span>
                </div>
                <div class="contact-item">
                    <i class="el-icon-message"></i>
                    <span>邮箱: 2022@hunnu.edu.cn</span>
                </div>
                <div class="social-links">
                    <a href="#" class="social-link">
                        <i class="el-icon-chat-dot-round"></i>
                    </a>
                    <a href="#" class="social-link">
                        <i class="el-icon-share"></i>
                    </a>
                    <a href="#" class="social-link">
                        <i class="el-icon-star-on"></i>
                    </a>
                </div>
            </div>
        </div>

        <!-- Bottom Bar -->
        <div class="footer-bottom">
            <div class="bottom-content">
                <div class="copyright">
                    <span>&copy; 2024 校园二手交易平台. 保留所有权利.</span>
                </div>
                <div class="bottom-links">
                    <a href="#" @click.prevent>隐私政策</a>
                    <span class="divider">|</span>
                    <a href="#" @click.prevent>用户协议</a>
                    <span class="divider">|</span>
                    <a href="#" @click.prevent>网站地图</a>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        name: "Foot"
    }
</script>

<style scoped>
.modern-footer {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border-top: 1px solid rgba(0, 0, 0, 0.05);
    margin-top: 3rem;
}

.footer-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 3rem 2rem 2rem;
    display: grid;
    grid-template-columns: 1fr 2fr 1fr;
    gap: 3rem;
}

/* Brand Section */
.footer-brand {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.footer-logo {
    display: flex;
    align-items: center;
    gap: 1rem;
}

.logo-icon {
    width: 48px;
    height: 48px;
    background: var(--primary-gradient);
    border-radius: var(--radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 1.5rem;
    box-shadow: var(--shadow-md);
}

.brand-text {
    display: flex;
    flex-direction: column;
}

.brand-name {
    font-size: 1.25rem;
    font-weight: 700;
    color: var(--text-primary);
}

.brand-tagline {
    font-size: 0.9rem;
    color: var(--text-secondary);
    margin-top: 0.25rem;
}

/* Links Section */
.footer-links {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 2rem;
}

.link-group h4 {
    font-size: 1rem;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 1rem 0;
    position: relative;
}

.link-group h4::after {
    content: '';
    position: absolute;
    bottom: -0.5rem;
    left: 0;
    width: 30px;
    height: 2px;
    background: var(--primary-gradient);
    border-radius: 1px;
}

.link-group ul {
    list-style: none;
    padding: 0;
    margin: 0;
}

.link-group li {
    margin-bottom: 0.75rem;
}

.link-group a {
    color: var(--text-secondary);
    text-decoration: none;
    font-size: 0.9rem;
    transition: all var(--transition-normal);
    position: relative;
}

.link-group a:hover {
    color: var(--primary-color);
    transform: translateX(5px);
}

.link-group a::before {
    content: '';
    position: absolute;
    left: -10px;
    top: 50%;
    transform: translateY(-50%);
    width: 0;
    height: 2px;
    background: var(--primary-color);
    transition: width var(--transition-normal);
}

.link-group a:hover::before {
    width: 6px;
}

/* Contact Section */
.footer-contact h4 {
    font-size: 1rem;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 1rem 0;
    position: relative;
}

.footer-contact h4::after {
    content: '';
    position: absolute;
    bottom: -0.5rem;
    left: 0;
    width: 30px;
    height: 2px;
    background: var(--primary-gradient);
    border-radius: 1px;
}

.contact-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 1rem;
    color: var(--text-secondary);
    font-size: 0.9rem;
}

.contact-item i {
    color: var(--primary-color);
    font-size: 1rem;
}

.social-links {
    display: flex;
    gap: 0.75rem;
    margin-top: 1.5rem;
}

.social-link {
    width: 36px;
    height: 36px;
    background: rgba(102, 126, 234, 0.1);
    border-radius: var(--radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--primary-color);
    text-decoration: none;
    transition: all var(--transition-normal);
}

.social-link:hover {
    background: var(--primary-color);
    color: white;
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
}

/* Bottom Bar */
.footer-bottom {
    background: rgba(0, 0, 0, 0.02);
    border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.bottom-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 1.5rem 2rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.copyright {
    color: var(--text-secondary);
    font-size: 0.85rem;
}

.bottom-links {
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.bottom-links a {
    color: var(--text-secondary);
    text-decoration: none;
    font-size: 0.85rem;
    transition: color var(--transition-normal);
}

.bottom-links a:hover {
    color: var(--primary-color);
}

.divider {
    color: var(--text-light);
}

/* Responsive Design */
@media (max-width: 768px) {
    .footer-content {
        grid-template-columns: 1fr;
        gap: 2rem;
        padding: 2rem 1rem 1.5rem;
    }
    
    .footer-links {
        grid-template-columns: 1fr;
        gap: 1.5rem;
    }
    
    .bottom-content {
        flex-direction: column;
        gap: 1rem;
        text-align: center;
        padding: 1.5rem 1rem;
    }
    
    .bottom-links {
        flex-wrap: wrap;
        justify-content: center;
    }
}

@media (max-width: 480px) {
    .footer-logo {
        flex-direction: column;
        text-align: center;
        gap: 0.75rem;
    }
    
    .social-links {
        justify-content: center;
    }
    
    .logo-icon {
        width: 40px;
        height: 40px;
        font-size: 1.25rem;
    }
    
    .brand-name {
        font-size: 1.1rem;
    }
}

/* Animation for footer elements */
.modern-footer {
    animation: slideInUp 0.6s ease-out;
}

@keyframes slideInUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>
