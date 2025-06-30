<template>
    <div class="login-container" >
        <el-card class="box-card">
            <div class="login-body">
                <div class="login-title" @click="toIndex" >校园交易平台</div>
                <el-form ref="form" :model="userForm">
                    <el-input placeholder="请输入账号" v-model="userForm.accountNumber" class="login-input">
                        <template slot="prepend">
                            <div class="el-icon-user-solid"></div>
                        </template>
                    </el-input>
                    <el-input placeholder="请输入密码" v-model="userForm.userPassword" class="login-input"
                              @keyup.enter.native="login"  show-password>
                        <template slot="prepend">
                            <div class="el-icon-lock"></div>
                        </template>
                    </el-input>
                    <div class="login-submit" >
                        <el-button type="primary" @click="login">登录</el-button>
                        <el-button type="warning" autocomplete="off" @click="$router.push('/sign-in')" style="margin-left: 20px">注册</el-button>
                    </div>
                    <div class="other-submit">

                        <router-link to="/login-admin" class="sign-in-text">管理员登录</router-link>
                    </div>
                </el-form>
            </div>
        </el-card>
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
                        this.$globalData.userInfo = res.data;
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
    .login-container {
        display: flex;
        justify-content: center;
        align-items: center;
        height: 100vh;
        width: 100%;

    }

    .login-body {
        padding: 30px;
        width: 300px;
        height: 100%;
    }

    .login-title {
        padding-bottom: 30px;
        text-align: center;
        font-weight: 600;
        font-size: 20px;
        color: #409EFF;
        cursor: pointer;
    }

    .login-input {
        margin-bottom: 20px;
    }

    .login-submit {
        margin-top: 20px;
        display: flex;
        justify-content: center;
    }

    .sign-in-container {
        padding: 0 10px;
    }

    .sign-in-text {
        color: #409EFF;
        font-size: 16px;
        text-decoration: none;
        line-height:28px;
    }
    .other-submit{
        display:flex;
        justify-content: space-between;
        margin-top: 30px;
        margin-left: 200px;
    }
</style>
