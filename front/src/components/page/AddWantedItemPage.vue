<template>
    <div>
        <div class="container">
            <div class="form-box modern-card">
                <h2 class="page-title">{{ form.id ? '编辑求购信息' : '发布求购信息' }}</h2>
                <el-form ref="form" :model="form" label-width="120px" :rules="rules" class="modern-form">
                    <el-form-item label="求购物品名称" prop="wantedName">
                        <el-input v-model="form.wantedName" class="modern-input"></el-input>
                    </el-form-item>
                    <el-form-item label="求购详情" prop="wantedDetails">
                        <el-input type="textarea" rows="5" v-model="form.wantedDetails" class="modern-textarea"></el-input>
                    </el-form-item>
                    <el-form-item label="最高预算" prop="maxPrice">
                        <el-input-number v-model="form.maxPrice" :precision="2" :step="0.1" :min="0" class="modern-input-number"></el-input-number>
                    </el-form-item>
                    <el-form-item label="期望交易地点" prop="wantedPlace">
                        <el-input v-model="form.wantedPlace" class="modern-input"></el-input>
                    </el-form-item>
                    <el-form-item label="分类标签" prop="wantedLabel">
                        <el-select v-model="form.wantedLabel" placeholder="请选择" class="modern-select">
                            <el-option label="学习用品" :value="1"></el-option>
                            <el-option label="电子产品" :value="2"></el-option>
                            <el-option label="生活用品" :value="3"></el-option>
                            <el-option label="服饰" :value="4"></el-option>
                            <el-option label="其他" :value="5"></el-option>
                        </el-select>
                    </el-form-item>

                    <el-form-item label="求购状态" prop="wantedStatus" v-if="form.id"> <!-- 只有编辑时显示 -->
                        <el-select v-model="form.wantedStatus" placeholder="请选择状态" class="modern-select">
                            <el-option label="求购中" :value="1"></el-option>
                            <el-option label="已找到" :value="2"></el-option>
                        </el-select>
                    </el-form-item>

                    <el-form-item>
                        <el-button type="primary" @click="onSubmit" class="modern-btn modern-btn-primary">提交</el-button>
                        <el-button @click="onCancel" class="modern-btn modern-btn-cancel">取消</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script>
import api from '../../api/index';
import { mapState } from 'vuex';

export default {
    name: 'AddWantedItemPage',
    data() {
        return {
            form: {
                id: null,
                wantedName: '',
                wantedDetails: '',
                maxPrice: 0.00,
                wantedPlace: '',
                wantedLabel: null,
                userId: null,
                wantedStatus: 1 // 新增 wantedStatus 字段，默认值为1（发布中）
            },
            rules: {
                wantedName: [{ required: true, message: '请输入求购物品名称', trigger: 'blur' }],
                wantedDetails: [{ required: true, message: '请输入求购详情', trigger: 'blur' }],
                maxPrice: [{ required: true, message: '请输入最高预算', trigger: 'blur' }],
                wantedPlace: [{ required: true, message: '请输入期望交易地点', trigger: 'blur' }],
                wantedLabel: [{ required: true, message: '请选择分类标签', trigger: 'change' }]
            }
        };
    },
    computed: {
        ...mapState(['userInfo'])
    },
    created() {
        if (this.$route.query.id) {
            this.form.id = this.$route.query.id;
            this.getWantedItemDetail();
        }
    },
    methods: {
        getWantedItemDetail() {
            api.getWantedItemById(this.form.id).then(res => {
                // 修正：后端返回的 status_code 是 200
                if (res.status_code === 200 && res.data) {
                    this.form = res.data;
                } else {
                    this.$message.error('获取求购详情失败');
                }
            }).catch(err => {
                this.$message.error('请求失败');
            });
        },
        onSubmit() {
            this.$refs.form.validate(valid => {
                if (valid) {
                    if (!this.userInfo || !this.userInfo.id) {
                        this.$message.error('请先登录再发布求购');
                        return;
                    }
                    this.form.userId = this.userInfo.id;
                    if (this.form.id) { // 编辑
                        // 确保 wantedStatus 在更新时被传递
                        api.updateWantedItem(this.form).then(res => {
                            if (res.status_code === 200) {
                                this.$message.success('更新成功');
                                this.$router.push('/wanteditemlist');
                            } else {
                                this.$message.error('更新失败');
                            }
                        }).catch(err => {
                            this.$message.error('请求失败');
                        });
                    } else { // 新增
                        api.addWantedItem(this.form).then(res => {
                            if (res.status_code === 200) {
                                this.$message.success('发布成功');
                                this.$router.push('/wanteditemlist');
                            } else {
                                this.$message.error('发布失败');
                            }
                        }).catch(err => {
                            this.$message.error('请求失败');
                        });
                    }
                } else {
                    this.$message.error('请填写完整信息');
                    return false;
                }
            });
        },
        onCancel() {
            this.$router.push('/wanteditemlist');
        }
    }
};
</script>

<style scoped>
.container {
    padding: 30px;
}

.form-box {
    width: 700px;
    max-width: 90%;
    margin: 40px auto;
    padding: 40px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
    text-align: center; /* 确保标题居中 */
}

.page-title {
    font-size: 28px;
    font-weight: bold;
    color: #333;
    margin-bottom: 30px;
    text-align: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.modern-form .el-form-item {
    margin-bottom: 25px;
}

.modern-input >>> .el-input__inner,
.modern-textarea >>> .el-textarea__inner,
.modern-select >>> .el-input__inner {
    border-radius: 8px;
    border: 1px solid #dcdfe6;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
}

.modern-input >>> .el-input__inner:focus,
.modern-textarea >>> .el-textarea__inner:focus,
.modern-select >>> .el-input__inner:focus {
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.modern-input-number >>> .el-input__inner {
    border-radius: 8px;
    border: 1px solid #dcdfe6;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.modern-btn {
    border: none;
    border-radius: 8px;
    padding: 12px 25px;
    font-weight: bold;
    transition: all 0.3s ease;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    margin-right: 15px;
}

.modern-btn-primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
}

.modern-btn-primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.15);
    opacity: 0.9;
}

.modern-btn-cancel {
    background: #f0f2f5;
    color: #606266;
}

.modern-btn-cancel:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.05);
    opacity: 0.9;
}
</style>
