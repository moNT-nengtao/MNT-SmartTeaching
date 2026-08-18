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
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="collegeName" label="学院" width="140" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleAssignRole(row)">分配角色</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="handleDialogClose">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%" placeholder="请选择角色">
            <el-option label="管理员" value="admin" />
            <el-option label="教师" value="teacher" />
            <el-option label="学生" value="student" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院" prop="collegeId">
          <el-select v-model="form.collegeId" style="width: 100%" placeholder="请选择学院" @change="handleCollegeChange">
            <el-option v-for="c in collegeList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select v-model="form.classId" style="width: 100%" placeholder="请选择班级">
            <el-option v-for="c in classList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { getUserList, addUser, updateUser, deleteUser, toggleUserStatus } from '@/api/user'
import { getCollegeList, getClassList } from '@/api/org'

// ============ 数据状态 ============
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const collegeList = ref([])
const classList = ref([])

// ============ 查询参数 ============
const searchParams = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  role: '',
  status: null
})

// ============ 搜索表单配置 ============
const searchFields = [
  { prop: 'keyword', label: '关键词', type: 'input', placeholder: '账号/姓名' },
  {
    prop: 'role',
    label: '角色',
    type: 'select',
    options: [
      { label: '全部', value: '' },
      { label: '管理员', value: 'admin' },
      { label: '教师', value: 'teacher' },
      { label: '学生', value: 'student' }
    ]
  },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: [
      { label: '全部', value: null },
      { label: '正常', value: 1 },
      { label: '禁用', value: 0 }
    ]
  }
]

// ============ 表单数据 ============
const form = reactive({
  id: null,
  username: '',
  realName: '',
  role: 'student',
  collegeId: null,
  classId: null,
  phone: '',
  email: ''
})

// ============ 表单校验规则 ============
const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// ============ 计算属性 ============
const dialogTitle = ref('')

// ============ 工具函数 ============
const roleText = (role) => {
  const map = { admin: '管理员', teacher: '教师', student: '学生' }
  return map[role] || role
}

const roleTagType = (role) => {
  const map = { admin: 'danger', teacher: 'warning', student: 'primary' }
  return map[role] || 'info'
}

// ============ API 请求 ============
const fetchList = async () => {
  loading.value = true
  try {
    const res = await getUserList(searchParams)
    // 兼容不同的返回格式
    const data = res.data || res
    tableData.value = data.list ?? data.records ?? []
    total.value = data.total ?? 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const fetchColleges = async () => {
  try {
    const res = await getCollegeList()
    collegeList.value = res.data || []
  } catch (error) {
    console.error('获取学院列表失败:', error)
    ElMessage.error('获取学院列表失败')
  }
}

const fetchClassByCollege = async (collegeId) => {
  if (!collegeId) {
    classList.value = []
    return
  }
  try {
    const res = await getClassList(collegeId)
    classList.value = res.data || []
  } catch (error) {
    console.error('获取班级列表失败:', error)
    ElMessage.error('获取班级列表失败')
  }
}

// ============ 事件处理 ============
const handleCollegeChange = (val) => {
  form.classId = null
  fetchClassByCollege(val)
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  Object.assign(form, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    role: row.role,
    collegeId: row.collegeId,
    classId: row.classId,
    phone: row.phone || '',
    email: row.email || ''
  })
  // 加载对应的班级列表
  if (row.collegeId) {
    fetchClassByCollege(row.collegeId)
  }
  dialogVisible.value = true
}

const handleDialogClose = () => {
  // 关闭弹窗时重置表单
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    username: '',
    realName: '',
    role: 'student',
    collegeId: null,
    classId: null,
    phone: '',
    email: ''
  })
  classList.value = []
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      // 构建提交数据
      const submitData = {
        username: form.username,
        realName: form.realName,
        role: form.role,
        collegeId: form.collegeId,
        classId: form.classId,
        phone: form.phone,
        email: form.email
      }
      
      if (isEdit.value) {
        submitData.id = form.id
        await updateUser(submitData)
        ElMessage.success('用户信息更新成功')
      } else {
        await addUser(submitData)
        ElMessage.success('用户创建成功')
      }
      
      dialogVisible.value = false
      fetchList()
    } catch (error) {
      console.error('保存用户失败:', error)
      ElMessage.error(error.message || '保存失败，请重试')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleAssignRole = (row) => {
  // TODO: 实现角色分配功能
  ElMessage.info('角色分配功能待实现')
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户「${row.realName}」吗？`,
      '提示',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    await toggleUserStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定删除用户「${row.realName}」吗？删除后不可恢复！`,
    '警告',
    {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    }
  ).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }).catch(() => {})
}

const handleBatchImport = (file) => {
  // TODO: 实现批量导入功能
  ElMessage.info('批量导入功能待实现')
  return false // 阻止自动上传
}

// ============ 监听器 ============
// 监听学院变化，联动加载班级
watch(() => form.collegeId, (newVal) => {
  if (newVal) {
    fetchClassByCollege(newVal)
  } else {
    classList.value = []
    form.classId = null
  }
})

// 监听搜索参数变化，自动刷新列表（可选）
watch(
  () => [searchParams.page, searchParams.pageSize],
  () => {
    fetchList()
  },
  { deep: true }
)

// ============ 生命周期 ============
onMounted(() => {
  fetchList()
  fetchColleges()
})
</script>

<style scoped>
.user-page {
  padding: 20px;
}

.page-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.page-header .el-button + .el-upload {
  margin-left: 10px;
}

/* 表格样式优化 */
:deep(.el-table) {
  margin-bottom: 20px;
}

:deep(.el-table .cell) {
  text-align: center;
}

:deep(.el-table .el-table-column--selection .cell) {
  text-align: center;
}

/* 弹窗样式 */
:deep(.el-dialog) {
  border-radius: 8px;
}

:deep(.el-dialog .el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 20px;
}

:deep(.el-dialog .el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog .el-dialog__footer) {
  border-top: 1px solid #ebeef5;
  padding: 10px 20px;
}

/* 搜索表单样式 */
:deep(.search-form) {
  margin-bottom: 20px;
}
</style>