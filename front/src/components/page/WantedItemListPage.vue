<template>
    <div class="wanted-item-list">
        <div class="container">
            <h2 class="page-title">求购列表</h2>
            <div class="handle-box">
                <el-input v-model="query.wantedName" placeholder="求购物品名称" class="handle-input mr10 modern-search-input"></el-input>
                <el-button type="primary" icon="el-icon-search" @click="handleSearch" class="modern-btn modern-btn-primary">搜索</el-button>
                <el-button
                    type="primary"
                    icon="el-icon-plus"
                    class="handle-del mr10 modern-btn modern-btn-success"
                    @click="handleAddWantedItem"
                >发布求购
                </el-button>
            </div>
            <el-table
                :data="tableData"
                border
                class="table modern-table"
                ref="multipleTable"
                header-cell-class-name="table-header"
            >
                <el-table-column prop="id" label="ID" width="55" align="center"></el-table-column>
                <el-table-column prop="wantedName" label="求购物品名称"></el-table-column>
                <el-table-column prop="maxPrice" label="最高预算"></el-table-column>
                <el-table-column prop="wantedPlace" label="期望交易地点"></el-table-column>
                <el-table-column prop="postTime" label="发布时间"></el-table-column>
                <el-table-column label="发布人">
                    <template slot-scope="scope">
                        <el-link type="primary" @click="goToUserDetail(scope.row.userId)">{{ scope.row.user ? scope.row.user.nickname : '未知' }}</el-link>
                    </template>
                </el-table-column>
                <el-table-column label="状态" align="center">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.wantedStatus === 1 ? 'danger' : (scope.row.wantedStatus === 2 ? 'success' : '')">
                            {{ scope.row.wantedStatus === 1 ? '求购中' : (scope.row.wantedStatus === 2 ? '已找到' : '') }}
                        </el-tag>
                    </template>
                </el-table-column>

                <el-table-column label="操作" width="180" align="center">
                    <template slot-scope="scope">
                        <el-button
                            type="text"
                            icon="el-icon-lx-text"
                            @click="handleView(scope.$index, scope.row)"
                        >查看详情
                        </el-button>
                        <el-button
                            v-if="scope.row.userId === userInfo.id"
                            type="text"
                            icon="el-icon-edit"
                            @click="handleEdit(scope.$index, scope.row)"
                        >编辑
                        </el-button>
                        <el-button
                            v-if="scope.row.userId === userInfo.id"
                            type="text"
                            icon="el-icon-delete"
                            class="red"
                            @click="handleDelete(scope.$index, scope.row)"
                        >删除
                        </el-button>
                        <el-button
                            v-if="scope.row.userId !== userInfo.id"
                            type="text"
                            icon="el-icon-chat-dot-round"
                            @click="handleContact(scope.$index, scope.row)"
                        >联系
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="pagination">
                <el-pagination
                    background
                    layout="total, prev, pager, next"
                    :current-page="query.pageIndex"
                    :page-size="query.pageSize"
                    :total="pageTotal"
                    @current-change="handlePageChange"
                ></el-pagination>
            </div>
        </div>
    </div>
</template>

<script>
import api from '../../api/index';
import { mapState } from 'vuex';

export default {
    name: 'WantedItemListPage',
    data() {
        return {
            query: {
                wantedName: '',
                pageIndex: 1,
                pageSize: 10
            },
            tableData: [],
            pageTotal: 0
        };
    },
    computed: {
        ...mapState(['userInfo'])
    },
    created() {
        this.getWantedItemList();
    },
    methods: {
        goToUserDetail(userId) {
            if (userId) {
                this.$router.push({ path: '/user', query: { id: userId } });
            }
        },
        // 获取求购列表数据
        getWantedItemList() {
            api.getWantedItemList({ page: this.query.pageIndex, limit: this.query.pageSize, wantedName: this.query.wantedName }).then(res => {
                if (res.status_code === 200) {
                    this.tableData = res.data.data;
                    this.pageTotal = res.data.count || 0;
                } else {
                    this.$message.error('获取求购列表失败');
                }
            }).catch(err => {
                this.$message.error('请求失败');
            });
        },
        // 触发搜索按钮
        handleSearch() {
            this.$set(this.query, 'pageIndex', 1);
            this.getWantedItemList();
        },
        // 删除操作
        handleDelete(index, row) {
            // 二次确认删除
            this.$confirm('确定要删除吗？', '提示', {
                type: 'warning'
            }).then(() => {
                api.deleteWantedItem(row.id).then(res => {
                    if (res.status_code === 200) {
                        this.$message.success('删除成功');
                        this.getWantedItemList();
                    } else {
                        this.$message.error('删除失败');
                    }
                }).catch(err => {
                    this.$message.error('请求失败');
                });
            }).catch(() => {});
        },
        // 编辑操作
        handleEdit(index, row) {
            this.$router.push({
                path: '/addwanteditem',
                query: {
                    id: row.id
                }
            });
        },
        // 查看详情
        handleView(index, row) {
            this.$router.push({
                path: '/wanteditemdetail',
                query: {
                    id: row.id
                }
            });
        },
        // 发布求购
        handleAddWantedItem() {
            this.$router.push('/addwanteditem');
        },
        // 分页导航
        handlePageChange(val) {
            this.$set(this.query, 'pageIndex', val);
            this.getWantedItemList();
        },
        handleContact(index, row) {
            if (this.userInfo && this.userInfo.id) {
                this.$router.push({ path: '/chat', query: { otherId: row.userId } });
            } else {
                this.$message.error('请先登录再联系他人');
            }
        }
    }
};
</script>

<style scoped>
/* 引入全局的现代化设计变量 */
/* @import '../../assets/styles/modern-design.css'; */ /* 假设你有一个全局样式文件，这里暂时不导入，直接定义或使用ElementUI默认 */

.container {
    padding: 90px 30px 30px 30px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
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

.handle-box {
    margin-bottom: 20px;
    display: flex;
    gap: 10px;
    align-items: center;
}

.handle-input {
    width: 250px;
    border-radius: 8px; /* 圆角 */
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05); /* 轻微阴影 */
}

.handle-input >>> .el-input__inner {
    border-radius: 8px;
    border: 1px solid #dcdfe6;
}

.modern-btn {
    border: none;
    border-radius: 8px;
    padding: 10px 20px;
    font-weight: bold;
    transition: all 0.3s ease;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.modern-btn-primary {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    color: #fff;
}

.modern-btn-primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.15);
    opacity: 0.9;
}

.modern-btn-success {
    background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%); /* 浅蓝色渐变 */
    color: #fff;
}

.modern-btn-success:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.15);
    opacity: 0.9;
}

.table {
    width: 100%;
    font-size: 14px;
}

.modern-table {
    border-radius: 12px;
    overflow: hidden; /* 确保圆角生效 */
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.modern-table >>> .el-table__header-wrapper th {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); /* 标题渐变 */
    color: #fff;
    border-color: #667eea;
}

.modern-table >>> .el-table__row {
    transition: all 0.3s ease;
}

.modern-table >>> .el-table__row:hover {
    background-color: rgba(102, 126, 234, 0.05); /* 悬停效果 */
}

.red {
    color: #ff6b6b; /* 更现代的红色 */
}

.mr10 {
    margin-right: 10px;
}

.pagination {
    margin-top: 20px;
    text-align: right;
}

.pagination >>> .btn-prev, .pagination >>> .btn-next, .pagination >>> .el-pager li {
    background-color: #f0f2f5 !important;
    border-radius: 8px !important;
    margin: 0 5px;
    transition: all 0.3s ease;
}

.pagination >>> .el-pager li.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
    color: #fff !important;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.pagination >>> .el-pager li:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
}

/* Tag 样式 */
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
</style>
