<template>
    <div>
        <app-head></app-head>
        <app-body>
            <div class="release-idle-container">
                <div class="release-idle-container-title">物品发布</div>
                <div class="release-idle-container-form">
                    <el-input placeholder="请输入物品名称" v-model="idleItemInfo.idleName"
                              maxlength="30"
                              show-word-limit>
                    </el-input>
                    <!-- Popover 容器: 结合了输入建议和所有标签列表 -->
                    <el-popover
                        ref="popover"
                        v-model="showTagPopover"
                        placement="bottom-start"
                        trigger="manual"
                        :width="350"
                        :visible-arrow="false"
                        :append-to-body="false"
                        popper-class="tag-popover"
                    >
                        <!-- 视图容器 -->
                        <div class="popover-content" @mousedown.prevent>
                            <!-- 视图一: 输入建议 -->
                            <div v-if="tagViewMode === 'suggestions'">
                                <div v-if="tagSuggestions.length">
                                    <div
                                        v-for="(item, idx) in tagSuggestions"
                                        :key="item.text"
                                        :class="['tag-suggestion-item', {active: idx === highlightedIdx}]"
                                        @mousedown.prevent="selectTagSuggestion(item)"
                                        @mouseenter="highlightedIdx = idx">
                                        <span>{{ item.text }}</span>
                                        <span class="use-count">被引用{{ item.use_count }}次</span>
                                    </div>
                                </div>
                                <div v-else class="no-suggestions">暂无建议</div>
                                <div class="popover-footer">
                                    <el-button type="text" @mousedown.prevent @click="switchToAllTagsView">查看所有标签</el-button>
                                </div>
                            </div>

                            <!-- 视图二: 所有标签列表 -->
                            <div v-if="tagViewMode === 'all'">
                                <div class="popover-header">
                                    <span>所有标签</span>
                                    <el-button type="text" @mousedown.prevent @click="switchToSuggestionsView">返回</el-button>
                                </div>
                                <div v-if="paginatedTags.length" class="all-tags-list">
                                    <el-tag
                                        v-for="tag in paginatedTags"
                                        :key="tag.text"
                                        class="tag-item"
                                        effect="plain"
                                        @mousedown.prevent
                                        @click="selectTagFromAll(tag)"
                                    >
                                        {{ tag.text }}
                                    </el-tag>
                                </div>
                                <div v-else class="no-suggestions">暂无标签</div>
                                <el-pagination
                                    v-if="allTags.length > pageSize"
                                    small
                                    layout="prev, pager, next"
                                    :total="allTags.length"
                                    :page-size="pageSize"
                                    :current-page="currentPage"
                                    @current-change="handlePageChange"
                                    class="tag-pagination"
                                    @mousedown.prevent
                                ></el-pagination>
                            </div>
                        </div>

                        <!-- 触发 popover 的输入框 -->
                        <el-input
                            class="release-idle-tag-text"
                            slot="reference"
                            ref="tagInput"
                            v-model="idleItemInfo.idleTag"
                            placeholder="请输入物品标签(#开头)，或点击选择"
                            @input="onTagInputChange"
                            @focus="onTagInputFocus"
                            @blur="onTagInputBlur"
                            @keydown.down.native.prevent="highlightNext"
                            @keydown.up.native.prevent="highlightPrev"
                            @keydown.enter.native.prevent="selectHighlighted"
                        ></el-input>
                    </el-popover>
                    <el-input
                            class="release-idle-detiles-text"
                            type="textarea"
                            autosize
                            placeholder="请输入物品的详细介绍..."
                            v-model="idleItemInfo.idleDetails"
                            maxlength="1000"
                            show-word-limit>
                    </el-input>
                    <div class="release-idle-place">
                        <div class="release-tip">您的地区</div>
                        <el-cascader
                                :options="options"
                                v-model="selectedOptions"
                                @change="handleChange"
                                :separator="' '"
                                style="width: 90%;"
                        >
                        </el-cascader>
                    </div>
                    <div style="display: flex; justify-content: space-between;">
                        <div>
                            <div class="release-tip">物品类别</div>
                            <el-select  v-model="idleItemInfo.idleLabel" placeholder="请选择类别">
                                <el-option
                                        v-for="item in options2"
                                        :key="item.value"
                                        :label="item.label"
                                        :value="item.value">
                                </el-option>
                            </el-select>
                        </div>
                        <div style="width: 300px;">
                            <el-input-number v-model="idleItemInfo.idlePrice" :precision="2" :step="10" :max="10000000">
                                <div slot="prepend">价格</div>
                            </el-input-number>
                        </div>

                    </div>
                    <div class="release-idle-container-picture">
                        <div class="release-idle-container-picture-title">上传二手物品照片</div>
                        <el-upload
                                action="http://localhost:8080/file/"
                                :on-preview="fileHandlePreview"
                                :on-remove="fileHandleRemove"
                                :on-success="fileHandleSuccess"
                                :show-file-list="showFileList"
                                :limit="10"
                                :on-exceed="handleExceed"
                                accept="image/*"
                                drag
                                multiple>
                            <i class="el-icon-upload"></i>
                            <div class="el-upload__text">将图片拖到此处，或<em>点击上传</em></div>
                        </el-upload>
                        <div class="picture-list">
                            <el-image style="width: 600px;height:400px; margin-bottom: 2px;" fit="contain"
                                      v-for="img in imgList" :key="img" :src="img"
                                      :preview-src-list="imgList"></el-image>
                        </div>
                        <el-dialog :visible.sync="imgDialogVisible">
                            <img width="100%" :src="dialogImageUrl" alt="">
                        </el-dialog>
                    </div>
                    <div style="display: flex;justify-content: center;margin-top: 30px;margin-bottom: 30px;">
                        <el-button type="primary" plain @click="releaseButton">确认发布</el-button>
                    </div>
                </div>
            </div>
            <app-foot></app-foot>
        </app-body>
    </div>
</template>

<script>
    import AppHead from '../common/AppHeader.vue';
    import AppBody from '../common/AppPageBody.vue'
    import AppFoot from '../common/AppFoot.vue'
    import options from '../common/country-data.js'

    export default {
        name: "release",
        components: {
            AppHead,
            AppBody,
            AppFoot
        },
        data() {
            return {
                imgDialogVisible:false,
                dialogImageUrl:'',
                showFileList:true,
                options:options,
                selectedOptions:[],
                options2: [{
                    value: 1,
                    label: '数码'
                }, {
                    value: 2,
                    label: '家电'
                }, {
                    value: 3,
                    label: '户外'
                }, {
                    value: 4,
                    label: '图书'
                }, {
                    value: 5,
                    label: '其他'
                }],
                imgList:[],
                // --- 标签功能相关数据 ---
                tagSuggestions: [],
                showTagPopover: false,
                highlightedIdx: 0,
                tagViewMode: 'suggestions', // 'suggestions' or 'all'
                allTags: [],
                currentPage: 1,
                pageSize: 18, // 每页显示18个标签

                idleItemInfo:{
                    idleName:'',
                    idleTag:'',
                    idleDetails:'',
                    pictureList:'',
                    idlePrice:0,
                    idlePlace:'',
                    idleLabel:''
                }
            };
        },
        computed: {
            // 计算当前页应显示的标签
            paginatedTags() {
                const start = (this.currentPage - 1) * this.pageSize;
                const end = start + this.pageSize;
                return this.allTags.slice(start, end);
            }
        },
        mounted() {
            // 添加全局点击监听器来处理点击外部关闭弹窗
            document.addEventListener('click', this.handleGlobalClick);
        },
        beforeDestroy() {
            document.removeEventListener('click', this.handleGlobalClick);
        },
        created() {
            const userId = this.getCookie && this.getCookie('shUserId');
            if (!userId) {
                this.$message.error('请先登录');
                this.$router.push('/login');
                return;
            }
            // 初始化时获取所有标签
            this.fetchAllTags();
        },  
        methods: {
            handleChange(value) {
                console.log(value);
                this.idleItemInfo.idlePlace=value[1];
            },
            fileHandleRemove(file, fileList) {
                console.log(file, fileList);
                for(let i=0;i<this.imgList.length;i++){
                    if(this.imgList[i]===file.response.data){
                        this.imgList.splice(i,1);
                    }
                }
            },
            fileHandlePreview(file) {
                console.log(file);
                this.dialogImageUrl=file.response.data;
                this.imgDialogVisible=true;
            },
            fileHandleSuccess(response, file, fileList){
                console.log("file:",response,file,fileList);
                this.imgList.push(response.data);
            },
            releaseButton(){
                if(this.idleItemInfo.idleTag){
                    let tagStr = this.idleItemInfo.idleTag;
                    let tags = tagStr.match(/#[^\s#]+/g);
                    if (tags) {
                        tags = Array.from(new Set(tags));
                        this.idleItemInfo.idleTag = tags.join('');
                    } else {
                        this.idleItemInfo.idleTag = '';
                    }
                }
                this.idleItemInfo.pictureList=JSON.stringify(this.imgList);
                console.log(this.idleItemInfo);
                if(this.idleItemInfo.idleName&&
                    this.idleItemInfo.idleDetails&&
                    this.idleItemInfo.idlePlace&&
                    this.idleItemInfo.idleLabel&&
                    this.idleItemInfo.idlePrice&&
                    this.idleItemInfo.idleTag){
                    this.$api.createTag("text=" + this.idleItemInfo.idleTag).then(res=>{
                        if (res.status_code === 200) {
                    
                        }
                    }).catch(e=>{
                        
                    });
                    this.$api.addIdleItem(this.idleItemInfo).then(res=>{
                        if (res.status_code === 200) {
                            this.$message({
                                message: '发布成功！',
                                type: 'success'
                            });
                            console.log(res.data);
                            this.$router.replace({path: '/details', query: {id: res.data.id}});
                        } else {
                            this.$message.error('发布失败！'+res.msg);
                        }
                    }).catch(e=>{
                        this.$message.error('请填写完整信息');
                    })
                }else {
                    this.$message.error('请填写完整信息！');
                }

            },
            handleExceed(files, fileList) {
                this.$message.warning(`限制10张图片，本次选择了 ${files.length} 张图，共选择了 ${files.length + fileList.length} 张图`);
            },
            getCookie(cname){
                var name = cname + "=";
                var ca = document.cookie.split(';');
                for(var i=0; i<ca.length; i++){
                    var c = ca[i].trim();
                    if (c.indexOf(name)===0) return c.substring(name.length,c.length);
                }
                return "";
            },
            // --- 标签相关方法 ---
            // 获取所有标签
            fetchAllTags() {
                this.$api.getAllTag({}).then(res => {
                    if (res.status_code === 200 && Array.isArray(res.data)) {
                        this.allTags = res.data;
                    }
                }).catch(e => {
                    console.error("获取所有标签失败:", e);
                });
            },
            // 全局点击处理器，用于关闭弹窗
            handleGlobalClick(event) {
                if (!this.showTagPopover) return;

                const popoverEl = this.$refs.popover ? this.$refs.popover.$el : null;
                const inputEl = this.$refs.tagInput ? this.$refs.tagInput.$el : null;

                // 如果点击的是输入框或弹窗内部，不关闭弹窗
                if ((inputEl && inputEl.contains(event.target)) ||
                    (popoverEl && popoverEl.contains(event.target))) {
                    return;
                }

                this.showTagPopover = false;
            },
            // 切换到所有标签视图
            switchToAllTagsView() {
                this.tagViewMode = 'all';
                this.showTagPopover = true; // 确保popover是打开的
            },
            // 切换到建议视图
            switchToSuggestionsView() {
                this.tagViewMode = 'suggestions';
                this.updateTagSuggestions(); // 重新获取建议
            },
            // 当输入框获得焦点时
            onTagInputFocus() {
                this.showTagPopover = true;
                // 如果当前没有输入有效的tag搜索，或者光标不在tag内，打开“所有标签”视图
                const info = this.getCurrentTagInfo();
                if (!info || !info.tagText || info.tagText.trim() === '#') {
                    this.tagViewMode = 'all';
                } else {
                    this.tagViewMode = 'suggestions';
                    this.updateTagSuggestions();
                }
            },
            // 失焦处理
            onTagInputBlur() {
                // 使用延时
                setTimeout(() => {
                    // 检查是否有活跃的焦点元素在弹窗内
                    const popoverEl = this.$refs.popover ? this.$refs.popover.$el : null;
                    const activeElement = document.activeElement;

                    if (!popoverEl || !popoverEl.contains(activeElement)) {
                        this.showTagPopover = false;
                    }
                }, 150);
            },
            // 当输入框内容变化时
            async onTagInputChange() {
                // 每次输入都回到建议视图
                this.tagViewMode = 'suggestions';
                if (!this.showTagPopover) {
                    this.showTagPopover = true;
                }
                await this.updateTagSuggestions();
            },
            getCurrentTagInfo() {
                const input = this.idleItemInfo.idleTag || '';
                const inputEl = this.$refs.tagInput.$el.querySelector('input');
                if (!inputEl) return null;
                const cursorPos = inputEl.selectionStart;

                let start = -1;
                let end = -1;

                // 找到光标前的最后一个'#'
                for (let i = cursorPos - 1; i >= 0; i--) {
                    if (input[i] === '#') {
                        start = i;
                        break;
                    }
                }

                // 如果没有找到'#', 或者光标不在一个tag内
                if (start === -1 || cursorPos <= start) {
                    return null;
                }

                // 找到从start开始的下一个空格或者'#'
                for (let i = start + 1; i < input.length; i++) {
                    if (input[i] === ' ' || input[i] === '#') {
                        end = i;
                        break;
                    }
                }

                if (end === -1) {
                    end = input.length;
                }
                
                const tagText = input.substring(start, end);
                return { tagText, start, end };
            },
            // 更新建议列表
            async updateTagSuggestions() {
                const info = this.getCurrentTagInfo();
                if (!info || !info.tagText || info.tagText === '#') {
                    this.tagSuggestions = [];
                    return;
                }
                const res = await this.$api.getAkinTag("text=" + info.tagText).catch(() => ({data: []}));
                this.tagSuggestions = Array.isArray(res.data) ? res.data : [];
                this.highlightedIdx = 0;
            },
            // 从所有标签列表中选择一个标签
            selectTagFromAll(tag) {
                let currentTags = this.idleItemInfo.idleTag.trim();
                const newTag = tag.text;
                // 检查是否已存在
                const tagsArray = currentTags.match(/#[^\s#]+/g) || [];
                if (tagsArray.includes(newTag)) {
                    return; // 如果已存在，则不添加
                }
                // 添加新标签，并确保前面有空格
                if (currentTags && !currentTags.endsWith(' ')) {
                    this.idleItemInfo.idleTag += ' ';
                }
                this.idleItemInfo.idleTag += newTag;
                // 选择后不需要关闭popover，可以连续选择
            },
            // fetch-suggestions
            queryTagSuggestions(queryString, cb) {
                this.$nextTick(() => {
                    const info = this.getCurrentTagInfo();
                    if (!info || !info.tagText || info.tagText === '#') {
                        cb([]);
                        return;
                    }
                    this.$api.getAkinTag("text=" + info.tagText).then(res => {
                        if (res.status_code === 200 && Array.isArray(res.data)) {
                            cb(res.data); // 直接返回对象数组
                        } else {
                            cb([]);
                        }
                    }).catch(() => {
                        cb([]);
                    });
                });
            },
            // 从建议列表中选择
            selectTagSuggestion(item) {
                const input = this.idleItemInfo.idleTag || '';
                const info = this.getCurrentTagInfo();
                if (!info) return;

                this.idleItemInfo.idleTag = input.substring(0, info.start) + item.text + input.substring(info.end);
                this.showTagPopover = false;

                this.$nextTick(() => {
                    const inputEl = this.$refs.tagInput.$el.querySelector('input');
                    if (inputEl) {
                        const pos = info.start + item.text.length;
                        inputEl.selectionStart = inputEl.selectionEnd = pos;
                        inputEl.focus();
                    }
                });
            },
            // 键盘高亮下一个
            highlightNext() {
                if (!this.showTagPopover || this.tagViewMode !== 'suggestions' || !this.tagSuggestions.length) return;
                this.highlightedIdx = (this.highlightedIdx + 1) % this.tagSuggestions.length;
            },
            // 键盘高亮上一个
            highlightPrev() {
                if (!this.showTagPopover || this.tagViewMode !== 'suggestions' || !this.tagSuggestions.length) return;
                this.highlightedIdx = (this.highlightedIdx - 1 + this.tagSuggestions.length) % this.tagSuggestions.length;
            },
            // 键盘回车选择
            selectHighlighted() {
                if (this.showTagPopover && this.tagViewMode === 'suggestions' && this.tagSuggestions[this.highlightedIdx]) {
                    this.selectTagSuggestion(this.tagSuggestions[this.highlightedIdx]);
                }
            },
            // 处理分页变化
            handlePageChange(page) {
                this.currentPage = page;
            },
        }
    }
</script>

<style>
/* 将 popover 的内边距设为0，以便内容可以撑满 */
.tag-popover {
    padding: 0 !important;
}
</style>

<style scoped>
    .release-idle-container {
        min-height: 85vh;
    }

    .release-idle-container-title {
        font-size: 18px;
        padding: 30px 0;
        font-weight: 600;
        width: 100%;
        text-align: center;
    }

    .release-idle-container-form {
        padding: 0 180px;
    }

    .release-idle-tag-text {
        margin-top: 20px;
    }

    .release-idle-detiles-text {
        margin: 20px 0;
    }
    .release-idle-place{
        margin-bottom: 15px;
    }
    .release-tip{
        color: #555555;
        float: left;
        padding-right: 5px;
        height: 36px;
        line-height: 36px;
        font-size: 14px;
    }
    .release-idle-container-picture{
        width: 500px;
        height: 400px;
        margin: 20px 0;

    }
    .release-idle-container-picture-title{
        margin: 10px 0;
        color: #555555;
        font-size: 14px;
    }
    .picture-list {
        margin: 20px 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        height: 100px;
    }
    /* --- 标签弹出框样式 --- */
    .popover-content {
        line-height: 1.5;
    }
    .tag-suggestion-item {
        display: flex;
        justify-content: space-between;
        cursor: pointer;
        padding: 6px 12px;
    }
    .tag-suggestion-item:hover, .tag-suggestion-item.active {
        background: #f5f7fa;
    }
    .use-count {
        color:#888;
        font-size:12px;
    }
    .no-suggestions {
        color:#aaa;
        padding:8px 12px;
    }
    .popover-footer {
        border-top: 1px solid #EBEEF5;
        text-align: center;
        padding: 4px 0;
    }
    .popover-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 12px;
        border-bottom: 1px solid #EBEEF5;
        font-weight: bold;
        color: #303133;
    }
    .all-tags-list {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        padding: 12px;
        max-height: 200px;
        overflow-y: auto;
    }
    .tag-item {
        cursor: pointer;
    }
    .tag-pagination {
        display: flex;
        justify-content: center;
        padding-bottom: 8px;
    }
</style>