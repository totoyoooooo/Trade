<template>
    <div>
        <div class="container">
            <el-card class="box-card modern-card">
                <div slot="header" class="clearfix page-title-wrapper">
                    <span class="page-title">求购详情</span>
                    <el-button type="primary" @click="goBack" class="modern-btn modern-btn-primary return-btn">返回列表</el-button>
                </div>
                <div class="wanted-item-detail">
                    <el-descriptions :column="1" border class="modern-descriptions">
                        <el-descriptions-item label="求购物品名称">{{ wantedItem.wantedName }}</el-descriptions-item>
                        <el-descriptions-item label="最高预算">{{ wantedItem.maxPrice }}</el-descriptions-item>
                        <el-descriptions-item label="期望交易地点">{{ wantedItem.wantedPlace }}</el-descriptions-item>
                        <el-descriptions-item label="分类标签">
                            <el-tag size="small">{{ getLabelName(wantedItem.wantedLabel) }}</el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="发布时间">{{ wantedItem.releaseTime }}</el-descriptions-item>
                        <el-descriptions-item label="求购状态">
                            <el-tag :type="wantedItem.wantedStatus === 1 ? 'danger' : (wantedItem.wantedStatus === 2 ? 'success' : '')">
                                {{ wantedItem.wantedStatus === 1 ? '求购中' : (wantedItem.wantedStatus === 2 ? '已找到' : '') }}
                            </el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="发布人">{{ wantedItem.user ? wantedItem.user.nickname : '未知' }}</el-descriptions-item>
                        <el-descriptions-item label="求购详情">{{ wantedItem.wantedDetails }}</el-descriptions-item>
                    </el-descriptions>
                </div>
            </el-card>
        </div>
    </div>
</template>

<script>
import api from '../../api/index';

export default {
    name: 'WantedItemDetailPage',
    data() {
        return {
            wantedItem: {},
            wantedItemId: null
        };
    },
    created() {
        if (this.$route.query.id) {
            this.wantedItemId = this.$route.query.id;
            this.getWantedItemDetail();
        }
    },
    methods: {
        getWantedItemDetail() {
            api.getWantedItemById(this.wantedItemId).then(res => {
                if (res.status_code === 200 && res.data) { // 修正状态码判断
                    this.wantedItem = res.data;
                } else {
                    this.$message.error('获取求购详情失败');
                }
            }).catch(err => {
                this.$message.error('请求失败');
            });
        },
        getLabelName(label) {
            switch (label) {
                case 1:
                    return '学习用品';
                case 2:
                    return '电子产品';
                case 3:
                    return '生活用品';
                case 4:
                    return '服饰';
                case 5:
                    return '其他';
                default:
                    return '未知';
            }
        },
        goBack() {
            this.$router.go(-1);
        }
    }
};
</script>

<style scoped>
.container {
    padding: 30px;
}

.modern-card {
    border-radius: 12px;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
    padding: 20px;
}

.page-title-wrapper {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.page-title {
    font-size: 28px;
    font-weight: bold;
    color: #333;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    padding-left: 10px;
}

.return-btn {
    border: none;
    border-radius: 8px;
    padding: 10px 20px;
    font-weight: bold;
    transition: all 0.3s ease;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    color: #fff;
}

.return-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.15);
    opacity: 0.9;
}

.modern-descriptions >>> .el-descriptions-item__label {
    background: linear-gradient(135deg, #e0eafc 0%, #cfdef3 100%);
    font-weight: bold;
    color: #555;
    padding: 12px 15px;
}

.modern-descriptions >>> .el-descriptions-item__content {
    padding: 12px 15px;
    color: #333;
}

/* 标签样式 (与列表页保持一致) */
.el-tag {
    border-radius: 6px;
    font-weight: bold;
    text-transform: uppercase;
    padding: 5px 10px;
}

.el-tag--success {
    background-color: #e6ffed; /* Light green */
    color: #38a169; /* Dark green */
    border-color: #9ae6b4;
}

.el-tag--info {
    background-color: #ebf8ff; /* Light blue */
    color: #3182ce; /* Dark blue */
    border-color: #90cdf4;
}

.el-tag--danger {
    background-color: #fff5f5; /* Light red */
    color: #e53e3e; /* Dark red */
    border-color: #feb2b2;
}

.wanted-item-detail {
    padding: 0;
}

.box-card {
    margin-bottom: 20px;
}
.clearfix:before,
.clearfix:after {
    display: table;
    content: "";
}
.clearfix:after {
    clear: both
}
</style>
