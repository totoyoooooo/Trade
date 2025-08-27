<template>
    <div class="modern-login-container">
        <!-- Background Elements -->
        <div class="bg-shapes">
            <div class="shape shape-1"></div>
            <div class="shape shape-2"></div>
            <div class="shape shape-3"></div>
        </div>

        <!-- Login Card -->
        <div class="login-card">
            <!-- Header -->
            <div class="login-header">
                <div class="logo-section">
                    <div class="logo-icon">
                        <i class="el-icon-shopping-bag-2"></i>
                    </div>
                    <h1 class="brand-title" @click="toIndex">
                        <span class="brand-main">校园</span>
                        <span class="brand-accent">交易</span>
                    </h1>
                </div>
                <p class="login-subtitle">欢迎回来，开始你的交易之旅</p>
            </div>

            <!-- Login Form -->
            <div class="login-form-container">
                <el-form ref="form" :model="userForm" class="modern-form">
                    <!-- Account Input -->
                    <div class="input-group">
                        <div class="input-label">
                            <i class="el-icon-user"></i>
                            <span>账号</span>
                        </div>
                        <el-input 
                            v-model="userForm.accountNumber" 
                            placeholder="请输入你的账号"
                            class="modern-input"
                            size="large">
                        </el-input>
                    </div>

                    <!-- Password Input -->
                    <div class="input-group">
                        <div class="input-label">
                            <i class="el-icon-lock"></i>
                            <span>密码</span>
                        </div>
                        <el-input 
                            v-model="userForm.userPassword"
                            type="password"
                            placeholder="请输入你的密码"
                            class="modern-input"
                            size="large"
                            show-password
                            @keyup.enter.native="login">
                        </el-input>
                    </div>

                    <!-- Login Buttons -->
                    <div class="button-group">
                        <button class="primary-btn" @click="login" :disabled="!userForm.accountNumber || !userForm.userPassword">
                            <span>登录</span>
                            <i class="el-icon-right"></i>
                        </button>
                        
                        <button class="secondary-btn" @click="$router.push('/sign-in')">
                            <span>注册新账号</span>
                        </button>
                    </div>

                    <!-- Additional Links -->
                    <div class="additional-links">
                        <router-link to="/login-admin" class="admin-link">
                            <i class="el-icon-user-solid"></i>
                            <span>管理员入口</span>
                        </router-link>
                    </div>
                </el-form>
            </div>

            <!-- Features -->
            <div class="login-features">
                <div class="feature-item">
                    <i class="el-icon-shield"></i>
                    <span>安全保障</span>
                </div>
                <div class="feature-item">
                    <i class="el-icon-finished"></i>
                    <span>快速交易</span>
                </div>
                <div class="feature-item">
                    <i class="el-icon-star-on"></i>
                    <span>优质服务</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script>

import { webSocket } from '@/api';

    //保证所有消息只会同时显示一条
    const show = {
        lackParam: false,
        accountNotExist: false,
        passwordError: false,
        error: false
    }

    export default {
        name: "login",
        data() {
            return {
                userForm: {
                    accountNumber: '',
                    userPassword: ''
                }
            };
        },

        methods: {
            login() {
                if(this.userForm.accountNumber === '' || this.userForm.userPassword === ''){
                        //账号或者密码为空，提示用户
                        if(!show.lackParam){
                            show.lackParam = true;
                            this.$message.error({
                                message: '账号或密码不能为空',
                                onClose: () => {
                                    show.lackParam = false;
                                }  
                            });
                        }
                        return;
                }
                this.$api.userLogin({
                    accountNumber: this.userForm.accountNumber,
                    userPassword: this.userForm.userPassword
                }).then(res => {
                    if (res.status_code === 200) {
                        res.data.signInTime=res.data.signInTime.substring(0,10);
                        this.$webSocket.init("ws://localhost:8080/websocket/" + res.data.id);
                        this.$store.commit('setUserInfo', res.data); // 将用户信息保存到Vuex store
                        this.$router.replace({path: '/index'});
                    } else if(res.status_code === 400) {
                        //密码错误，提示用户
                        if(!show.passwordError){
                            show.passwordError = true;
                            this.$message.error({
                                message: '密码错误,请重新输入',
                                onClose: () => {
                                    show.passwordError = false;
                                }  
                            });
                        }
                    } else if(res.status_code === 404){
                        //账号不存在，提示用户
                        if(!show.accountNotExist){
                            show.accountNotExist = true;
                            this.$message.error({
                                message: "账号不存在,请注册",
                                onClose: () => {
                                    show.accountNotExist = false;
                                }  
                            });
                        }
                    }
                }).catch(e => {
                    //网络问题或者后端问题，提示用户
                    if(!show.error){
                        show.error = true;
                        this.$message.error({
                            message: "登录失败，网络异常！",
                            onClose: () => {
                                show.error = false;
                            }  
                        });
                    }
                });
            },
            toIndex() {
                this.$router.replace({path: '/index'});
            }
        }
    }
</script>

<style scoped>
.modern-login-container {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    padding: 2rem;
    overflow: hidden;
}

/* Background Shapes */
.bg-shapes {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
    z-index: 0;
}

.shape {
    position: absolute;
    border-radius: 50%;
    opacity: 0.6;
    animation: float 6s ease-in-out infinite;
}

.shape-1 {
    width: 200px;
    height: 200px;
    background: var(--primary-gradient);
    top: 10%;
    left: 10%;
    animation-delay: 0s;
}

.shape-2 {
    width: 150px;
    height: 150px;
    background: var(--secondary-gradient);
    top: 20%;
    right: 15%;
    animation-delay: 2s;
}

.shape-3 {
    width: 120px;
    height: 120px;
    background: var(--success-gradient);
    bottom: 20%;
    left: 20%;
    animation-delay: 4s;
}

@keyframes float {
    0%, 100% { transform: translateY(0) rotate(0deg); }
    33% { transform: translateY(-20px) rotate(5deg); }
    66% { transform: translateY(10px) rotate(-3deg); }
}

/* Login Card */
.login-card {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: var(--radius-2xl);
    box-shadow: var(--shadow-2xl);
    padding: 3rem;
    width: 100%;
    max-width: 480px;
    position: relative;
    z-index: 1;
    animation: slideInUp 0.8s ease-out;
}

.login-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: var(--primary-gradient);
    border-radius: var(--radius-2xl) var(--radius-2xl) 0 0;
}

/* Header */
.login-header {
    text-align: center;
    margin-bottom: 2.5rem;
}

.logo-section {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 1rem;
    margin-bottom: 1rem;
}

.logo-icon {
    width: 60px;
    height: 60px;
    background: var(--primary-gradient);
    border-radius: var(--radius-xl);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 1.8rem;
    box-shadow: var(--shadow-lg);
    animation: pulse 2s infinite;
}

.brand-title {
    margin: 0;
    cursor: pointer;
    transition: all var(--transition-normal);
}

.brand-title:hover {
    transform: scale(1.02);
}

.brand-main {
    font-size: 2rem;
    font-weight: 700;
    color: var(--text-primary);
}

.brand-accent {
    font-size: 2rem;
    font-weight: 700;
    background: var(--primary-gradient);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.login-subtitle {
    color: var(--text-secondary);
    font-size: 1rem;
    margin: 0;
}

/* Form */
.login-form-container {
    margin-bottom: 2rem;
}

.modern-form {
    width: 100%;
}

.input-group {
    margin-bottom: 1.5rem;
}

.input-label {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.5rem;
    color: var(--text-primary);
    font-weight: 500;
    font-size: 0.9rem;
}

.input-label i {
    color: var(--primary-color);
    font-size: 1rem;
}

.modern-input >>> .el-input__inner {
    border: 2px solid rgba(102, 126, 234, 0.1);
    border-radius: var(--radius-lg);
    padding: 1rem 1.25rem;
    font-size: 1rem;
    transition: all var(--transition-normal);
    box-shadow: var(--shadow-sm);
    background: rgba(255, 255, 255, 0.9);
}

.modern-input >>> .el-input__inner:focus {
    border-color: var(--primary-color);
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.modern-input >>> .el-input__inner::placeholder {
    color: var(--text-light);
}

/* Buttons */
.button-group {
    margin-bottom: 2rem;
}

.primary-btn {
    width: 100%;
    background: var(--primary-gradient);
    border: none;
    border-radius: var(--radius-lg);
    padding: 1rem 1.5rem;
    color: white;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all var(--transition-normal);
    box-shadow: var(--shadow-md);
    margin-bottom: 1rem;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    position: relative;
    overflow: hidden;
}

.primary-btn::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
    transition: left 0.5s;
}

.primary-btn:hover::before {
    left: 100%;
}

.primary-btn:hover {
    background: var(--primary-gradient-hover);
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
}

.primary-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
}

.secondary-btn {
    width: 100%;
    background: transparent;
    border: 2px solid rgba(102, 126, 234, 0.2);
    border-radius: var(--radius-lg);
    padding: 0.875rem 1.5rem;
    color: var(--primary-color);
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    transition: all var(--transition-normal);
}

.secondary-btn:hover {
    background: rgba(102, 126, 234, 0.05);
    border-color: var(--primary-color);
    transform: translateY(-1px);
}

/* Additional Links */
.additional-links {
    text-align: center;
    padding-top: 1.5rem;
    border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.admin-link {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--text-secondary);
    text-decoration: none;
    font-size: 0.9rem;
    transition: all var(--transition-normal);
    padding: 0.5rem 1rem;
    border-radius: var(--radius-lg);
}

.admin-link:hover {
    color: var(--primary-color);
    background: rgba(102, 126, 234, 0.05);
}

/* Features */
.login-features {
    display: flex;
    justify-content: space-around;
    margin-top: 2rem;
    padding-top: 1.5rem;
    border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.feature-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
    color: var(--text-secondary);
    font-size: 0.85rem;
}

.feature-item i {
    font-size: 1.25rem;
    color: var(--primary-color);
}

/* Responsive Design */
@media (max-width: 768px) {
    .modern-login-container {
        padding: 1rem;
    }
    
    .login-card {
        padding: 2rem;
        max-width: 400px;
    }
    
    .brand-main, .brand-accent {
        font-size: 1.75rem;
    }
    
    .logo-icon {
        width: 50px;
        height: 50px;
        font-size: 1.5rem;
    }
}

@media (max-width: 480px) {
    .login-card {
        padding: 1.5rem;
        margin: 0.5rem;
    }
    
    .logo-section {
        flex-direction: column;
        gap: 0.75rem;
    }
    
    .brand-main, .brand-accent {
        font-size: 1.5rem;
    }
    
    .login-features {
        flex-direction: column;
        gap: 1rem;
    }
    
    .feature-item {
        flex-direction: row;
        justify-content: center;
    }
}

/* Loading state */
.primary-btn.loading {
    position: relative;
    color: transparent;
}

.primary-btn.loading::after {
    content: '';
    position: absolute;
    width: 20px;
    height: 20px;
    border: 2px solid transparent;
    border-top: 2px solid white;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}
</style>
