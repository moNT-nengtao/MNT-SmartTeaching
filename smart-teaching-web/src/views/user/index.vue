<template>
  <div class="user-page">
    <SearchForm v-model="searchParams" :fields="searchFields" @search="fetchList" @reset="fetchList" />


    <div class="page-card">
      <div class="page-header">
        <span class="page-title">用户管理</span>
        <div>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
          <el-upload :show-file-list="false" :before-upload="handleBatchImport" accept=".xlsx,.xls">
            <el-button :icon="Upload">批量导入</el-button>
          </el-upload>
        </div>
      </div>


      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleTag(row.role)">{{ getRoleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="collegeName" label="学院" width="140" />
        <el-table-column prop="majorName" label="专业" width="140" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>


      <Pagination
        v-model:page="searchParams.page"
        v-model:page-size="searchParams.pageSize"
        :total="total"
        @change="fetchList"
      />
    </div>


    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
      destroy-on-close
    >
      <el-form
        v-if="dialogVisible"
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="80px"
        @submit.prevent
        :key="formKey"
        autocomplete="off"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="账号" prop="username">
              <el-input
                v-model="form.username"
                :disabled="isEdit"
                placeholder="请输入账号"
                maxlength="20"
                clearable
                autocomplete="off"
                :readonly="!dialogVisible"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入姓名" maxlength="20" clearable />
            </el-form-item>
          </el-col>
        </el-row>


        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                show-password
                :placeholder="isEdit ? '不修改请留空' : '请输入密码'"
                maxlength="20"
                clearable
                autocomplete="new-password"
                :readonly="!dialogVisible"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio :label="0">未知</el-radio>
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>


        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-select v-model="form.role" style="width:100%" placeholder="请选择角色">
                <el-option label="管理员" value="admin" />
                <el-option label="教师" value="teacher" />
                <el-option label="学生" value="student" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">正常</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>


        <el-form-item label="头像" prop="avatar">
          <div class="avatar-upload">
            <el-upload
              ref="avatarUploadRef"
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleAvatarChange"
              :before-upload="beforeAvatarUpload"
              accept="image/jpeg,image/png,image/gif"
            >
              <div class="avatar-preview" v-if="form.avatar">
                <img :src="form.avatar" alt="头像" />
                <div class="avatar-mask"><el-icon><Edit /></el-icon></div>
              </div>
              <div class="avatar-placeholder" v-else>
                <el-icon><Plus /></el-icon>
                <span>点击上传头像</span>
              </div>
            </el-upload>
            <el-button v-if="form.avatar" link type="danger" @click="removeAvatar" style="margin-top:8px">删除头像</el-button>
          </div>
        </el-form-item>


        <el-form-item label="学院" prop="collegeId">
          <el-select v-model="form.collegeId" style="width:100%" placeholder="请选择学院" @change="onCollegeChange">
            <el-option v-for="c in collegeList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>


        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="专业" prop="majorId">
              <el-select
                v-model="form.majorId"
                style="width:100%"
                placeholder="请先选择学院"
                @change="onMajorChange"
                :disabled="!form.collegeId"
                clearable
              >
                <el-option v-for="m in majorList" :key="m.id" :label="m.name" :value="m.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班级" prop="classId">
              <el-select
                v-model="form.classId"
                style="width:100%"
                placeholder="请先选择专业"
                :disabled="!form.majorId"
                clearable
              >
                <el-option v-for="c in classList" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>


        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" clearable />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>


      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Plus, Upload, Edit } from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { getUserList, addUser, updateUser, deleteUser, toggleUserStatus, batchImportUser } from '@/api/user'
import { getCollegeList, getMajorList, getClassList } from '@/api/org'


const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const avatarUploadRef = ref(null)
const collegeList = ref([])
const majorList = ref([])
const classList = ref([])
const dialogTitle = ref('')
const formKey = ref(0)


// 头像本地文件缓存，只内存保存，不发请求
const tempAvatarFile = ref(null)


const searchParams = reactive({ page:1, pageSize:10, keyword:'', role:'', status:null })


const searchFields = [
  { prop:'keyword', label:'关键词', type:'input', placeholder:'账号/姓名' },
  { prop:'role', label:'角色', type:'select', options:[
    {label:'全部',value:''},{label:'管理员',value:'admin'},{label:'教师',value:'teacher'},{label:'学生',value:'student'}
  ]},
  { prop:'status', label:'状态', type:'select', options:[
    {label:'全部',value:null},{label:'正常',value:1},{label:'禁用',value:0}
  ]}
]


const DEFAULT_FORM = {
  id:null, username:'', password:'', realName:'', gender:0, role:'student', status:1,
  avatar:'', collegeId:null, majorId:null, classId:null, phone:'', email:''
}


const form = reactive({...DEFAULT_FORM})


const rules = {
  username: [
    {required:true,message:'请输入账号',trigger:'blur'},
    {min:3,max:20,message:'账号长度3‑20字符',trigger:'blur'},
    {pattern:/^[a-zA-Z0-9_]+$/,message:'账号仅字母数字下划线',trigger:'blur'}
  ],
  password: [{validator:(r,v,cb)=>{
    if(!isEdit.value&&!v) return cb(new Error('请输入密码'))
    if(v&&(v.length<6||v.length>20)) return cb(new Error('密码6‑20位'))
    cb()
  },trigger:'blur'}],
  realName: [{required:true,message:'请输入姓名',trigger:'blur'},{min:2,max:20,message:'姓名2‑20字符',trigger:'blur'}],
  gender: [{required:true,message:'请选择性别',trigger:'change'}],
  role: [{required:true,message:'请选择角色',trigger:'change'}],
  status: [{required:true,message:'请选择状态',trigger:'change'}],
  phone: [{pattern:/^1[3-9]\d{9}$/,message:'手机号格式错误',trigger:'blur'}],
  email: [{type:'email',message:'邮箱格式错误',trigger:'blur'}],
  collegeId: [{required:true,message:'请选择学院',trigger:'change'}]
}


const ROLE_MAP = { admin:'管理员', teacher:'教师', student:'学生' }
const ROLE_TAG_MAP = { admin:'danger', teacher:'warning', student:'primary' }
const getRoleText = r=>ROLE_MAP[r]||r
const getRoleTag = r=>ROLE_TAG_MAP[r]||'info'


async function fetchList(){
  loading.value=true
  try{
    const res = await getUserList(searchParams)
    const d = res.data||res
    tableData.value = d.list??d.records??[]
    total.value = d.total??0
  }catch(e){ ElMessage.error('获取用户列表失败') }finally{ loading.value=false }
}
async function fetchColleges(){
  try{ const res=await getCollegeList(); collegeList.value=res.data||[] }catch(e){ ElMessage.error('获取学院失败') }
}
async function fetchMajors(cid){
  if(!cid) return majorList.value=[]
  try{ const res=await getMajorList(cid); majorList.value=res.data||[] }catch(e){ ElMessage.error('获取专业失败') }
}
async function fetchClasses(mid){
  if(!mid) return classList.value=[]
  try{ const res=await getClassList(mid); classList.value=res.data||[] }catch(e){ ElMessage.error('获取班级失败') }
}


/**
 * 选中图片，本地blob预览，不转base64，不上传服务器
 */
function handleAvatarChange(file){
  tempAvatarFile.value = file.raw
  form.avatar = URL.createObjectURL(file.raw)
}


function beforeAvatarUpload(file){
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('仅支持图片')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片不能超过2M')
    return false
  }
  return false // 强制关闭el‑upload自动上传
}


const removeAvatar = ()=>{
  form.avatar = ''
  tempAvatarFile.value = null
}


function onCollegeChange(val){
  form.majorId=null; form.classId=null; classList.value=[]
  fetchMajors(val)
}
function onMajorChange(val){
  form.classId=null
  fetchClasses(val)
}


function resetForm(){
  Object.assign(form, {...DEFAULT_FORM})
  majorList.value=[]; classList.value=[]
  formKey.value++
  formRef.value?.clearValidate()
  tempAvatarFile.value = null
}


function handleAdd(){
  isEdit.value=false
  dialogTitle.value='新增用户'
  resetForm()
  nextTick(()=>dialogVisible.value=true)
}


function handleEdit(row){
  isEdit.value=true
  dialogTitle.value='编辑用户'
  resetForm()
  Object.assign(form, {...row, password:'', avatar:row.avatar||''})
  if(row.collegeId){
    fetchMajors(row.collegeId)
    row.majorId&&fetchClasses(row.majorId)
  }
  nextTick(()=>dialogVisible.value=true)
}


function handleDialogClose(){
  // 释放blob预览内存，防止内存泄漏
  if(form.avatar && form.avatar.startsWith('blob:')){
    URL.revokeObjectURL(form.avatar)
  }
  dialogVisible.value=false
  resetForm()
  formRef.value?.clearValidate()
}


/**
 * 表单校验通过，组装FormData，业务字段+头像文件一次性提交
 */
async function handleSubmit(){
  if(!formRef.value) return
  await formRef.value.validate(async valid=>{
    if(!valid) return
    submitLoading.value=true
    try{
      const submitData = { ...form }
      if(!submitData.password) delete submitData.password

      // 构造FormData
      const fd = new FormData()
      Object.keys(submitData).forEach(key=>{
        if(submitData[key]!==null && submitData[key]!==undefined){
          fd.append(key, submitData[key])
        }
      })

      // 如果用户选择了新头像，追加文件字段 avatarFile
      if(tempAvatarFile.value){
        fd.append("avatarFile", tempAvatarFile.value)
      }

      isEdit.value ? await updateUser(fd) : await addUser(fd)

      ElMessage.success(isEdit.value?'更新成功':'创建成功')
      dialogVisible.value=false
      fetchList()
    }catch(e){
      ElMessage.error(e.message||'保存失败')
    }finally{
      submitLoading.value=false
    }
  })
}


async function handleToggleStatus(row){
  const newSt = row.status===1?0:1
  const op = newSt===0?'禁用':'启用'
  try{
    await ElMessageBox.confirm(`确定${op}用户「${row.realName}」？`,'提示',{type:'warning'})
    await toggleUserStatus(row.id, newSt)
    ElMessage.success(`${op}成功`)
    fetchList()
  }catch(e){ if(e!=='cancel') ElMessage.error('操作失败') }
}


async function handleDelete(row){
  try{
    await ElMessageBox.confirm(`确定删除用户「${row.realName}」，删除不可恢复！`,'警告',{type:'warning'})
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchList()
  }catch(e){}
}


/**
 * 批量导入用户
 */
async function handleBatchImport(file) {
  // 1. 验证文件类型
  const isValidType = file.name.endsWith('.xlsx') || file.name.endsWith('.xls')
  
  if (!isValidType) {
    ElMessage.error('请上传 Excel 文件（.xlsx 或 .xls）')
    return false
  }
  
  // 2. 验证文件大小（限制 8MB）
  const maxSize = 8 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 8MB')
    return false
  }

  // 3. 确认导入
  try {
    await ElMessageBox.confirm(
      `确定要导入文件「${file.name}」吗？\n文件大小: ${(file.size / 1024 / 1024).toFixed(2)} MB`,
      '批量导入用户',
      {
        confirmButtonText: '确定导入',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
  } catch {
    return false // 用户取消
  }

  // 4. 执行导入
  const loading = ElLoading.service({
    fullscreen: true,
    text: '正在导入用户数据，请稍候...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const res = await batchImportUser(formData)
    ElMessage.success(res.message || '批量导入成功')
    await fetchList() // 刷新列表
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || '导入失败，请检查文件格式'
    ElMessage.error(errorMsg)
  } finally {
    loading.close()
  }

  return false // 阻止 el-upload 自动上传
}


onMounted(()=>{ fetchList(); fetchColleges() })
</script>

<style scoped>
.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.avatar-preview,
.avatar-placeholder {
  width: 100px;
  height: 100px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
}

.avatar-preview {
  position: relative;
}

.avatar-preview img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: rgba(0, 0, 0, 0.45);
  opacity: 0;
  transition: opacity 0.2s;
}

.avatar-preview:hover .avatar-mask {
  opacity: 1;
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  cursor: pointer;
}

.avatar-placeholder .el-icon {
  margin-bottom: 6px;
  font-size: 24px;
}

.avatar-placeholder span {
  font-size: 12px;
}
</style>