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
                    <el-popover
                        v-model="showTagDropdown"
                        placement="bottom-start"
                        trigger="manual"
                        :width="300"
                        :visible-arrow="false"
                        :append-to-body="false"
                        >
                        <div v-if="tagSuggestions.length">
                            <div
                            v-for="(item, idx) in tagSuggestions"
                            :key="item.text"
                            :class="['tag-suggestion-item', {active: idx === highlightedIdx}]"
                            @mousedown.prevent="selectTagSuggestion(item)"
                            style="display:flex;justify-content:space-between;cursor:pointer;padding:4px 8px;"
                            @mouseenter="highlightedIdx = idx">
                                <span>{{ item.text }}</span>
                                <span style="color:#888;font-size:12px;">被引用{{ item.use_count }}次</span>
                            </div>
                        </div>
                        <div v-else style="color:#aaa;padding:4px 8px;">暂无建议</div>
                        <el-input
                        class="release-idle-tag-text"
                        slot="reference"
                        ref="tagInput"
                        v-model="idleItemInfo.idleTag"
                        placeholder="请输入物品标签(#开头)"
                        @input="onTagInputChange"
                        @focus="onTagInputChange"
                        @blur="onTagInputBlur"
                        @keyup.native="onCursorMove"
                        @mouseup.native="onCursorMove"
                        @keydown.down.native="highlightNext"
                        @keydown.up.native="highlightPrev"
                        @keydown.enter.native="selectHighlighted"
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
                                      v-for="(img,index) in imgList" :src="img"
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
import { isArray } from 'jquery';

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
                tagSuggestions: [],
    showTagDropdown: false,
    highlightedIdx: 0,
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
        created() {
            const userId = this.getCookie && this.getCookie('shUserId');
            if (!userId) {
                this.$message.error('请先登录');
                this.$router.push('/login');
                return;
            }
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
            onCursorMove() {
                this.updateTagSuggestions();
            },
            getCurrentTagInfo() {
                const input = this.idleItemInfo.idleTag || '';
                const inputEl = this.$refs.tagInput.$el.querySelector('input');
                if (!inputEl) return null;
                const cursorPos = inputEl.selectionStart;

                // 找到所有#的位置
                let tagPositions = [];
                for (let i = 0; i < input.length; i++) {
                    if (input[i] === '#') tagPositions.push(i);
                }
                if (tagPositions.length === 0) return null;

                // 找到光标所在的标签区间
                let start = -1, end = input.length;
                for (let i = 0; i < tagPositions.length; i++) {
                    if (tagPositions[i] < cursorPos) {
                        start = tagPositions[i];
                    }
                    if (tagPositions[i] > cursorPos) {
                        end = tagPositions[i];
                        break;
                    }
                }
                // 如果光标正好在某个#前，也归到前一个标签
                if (tagPositions.includes(cursorPos)) {
                    const idx = tagPositions.indexOf(cursorPos);
                    if (idx > 0) {
                        start = tagPositions[idx - 1];
                        end = tagPositions[idx];
                    }
                }
                // 特殊处理：如果光标正好在#上，归到前一个标签
                if (input[cursorPos - 1] === '#') {
                    start = cursorPos - 1;
                 // end 需要重新找
                    end = input.indexOf('#', start + 1);
                    if (end === -1) end = input.length;
                }
                if (start === -1) return null;
                const tagText = input.substring(start, end);
                return { tagText, start, end };
            },
            onTagInputBlur() {
                this.showTagDropdown = false;
            },
            async updateTagSuggestions() {
                const info = this.getCurrentTagInfo();
                if (!info || !info.tagText || info.tagText === '#') {
                    this.tagSuggestions = [];
                    this.showTagDropdown = false;
                    return;
                }
                const res = await this.$api.getAkinTag("text=" + info.tagText).catch(() => ({data: []}));
                this.tagSuggestions = Array.isArray(res.data) ? res.data : [];
                this.showTagDropdown = this.tagSuggestions.length > 0;
                this.highlightedIdx = 0;
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
            selectTagSuggestion(item) {
                const input = this.idleItemInfo.idleTag || '';
                const inputEl = this.$refs.tagInput.$el.querySelector('input');
                const info = this.getCurrentTagInfo();
                if (!info) return;
                this.idleItemInfo.idleTag = input.substring(0, info.start) + item.text + input.substring(info.end);
                this.$nextTick(() => {
                    if (inputEl) {
                        inputEl.selectionStart = inputEl.selectionEnd = info.start + item.text.length;
                        inputEl.focus();
                    }
                });
            },
            async onTagInputChange() {
                await this.updateTagSuggestions();
            },
            selectTagSuggestion(item) {
                const input = this.idleItemInfo.idleTag || '';
                const info = this.getCurrentTagInfo();
                if (!info) return;
                this.idleItemInfo.idleTag = input.substring(0, info.start) + item.text + input.substring(info.end);
                this.showTagDropdown = false;
                this.$nextTick(() => {
                const inputEl = this.$refs.tagInput.$el.querySelector('input');
                if (inputEl) {
                    inputEl.selectionStart = inputEl.selectionEnd = info.start + item.text.length;
                    inputEl.focus();
                }
                });
            },
            // 键盘上下选择
            highlightNext() {
                if (!this.showTagDropdown) return;
                this.highlightedIdx = (this.highlightedIdx + 1) % this.tagSuggestions.length;
            },
            highlightPrev() {
                if (!this.showTagDropdown) return;
                this.highlightedIdx = (this.highlightedIdx - 1 + this.tagSuggestions.length) % this.tagSuggestions.length;
            },
            selectHighlighted() {
                if (this.showTagDropdown && this.tagSuggestions[this.highlightedIdx]) {
                this.selectTagSuggestion(this.tagSuggestions[this.highlightedIdx]);
                }
            },
        }
    }
</script>

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
    .tag-suggestion-item.active {
        background: #f5f7fa;
    }
</style>