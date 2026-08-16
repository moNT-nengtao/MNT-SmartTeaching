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
        <el-table-column prop="name" label="姓名" width="100" />
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
        <el-table-column label="操作" width="240" fixed="right">
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="管理员" value="admin" />
            <el-option label="教师" value="teacher" />
            <el-option label="学生" value="student" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院" prop="collegeId">
          <el-select v-model="form.collegeId" style="width: 100%">
            <el-option v-for="c in collegeList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select v-model="form.classId" style="width: 100%">
            <el-option v-for="c in classList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { getUserList, addUser, updateUser, deleteUser, toggleUserStatus } from '@/api/user'
import { getCollegeList, getClassList } from '@/api/org'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const collegeList = ref([])
const classList = ref([])

const searchParams = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  role: ''
})

const searchFields = [
  { prop: 'keyword', label: '关键词', type: 'input', placeholder: '账号/姓名' },
  {
    prop: 'role',
    label: '角色',
    type: 'select',
    options: [
      { label: '管理员', value: 'admin' },
      { label: '教师', value: 'teacher' },
      { label: '学生', value: 'student' }
    ]
  }
]

const form = reactive({
  id: null,
  username: '',
  name: '',
  role: 'student',
  collegeId: null,
  classId: null,
  phone: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const dialogTitle = ref('')

const roleText = (role) => ({ admin: '管理员', teacher: '教师', student: '学生' }[role] || role)
const roleTagType = (role) => ({ admin: 'danger', teacher: 'warning', student: 'primary' }[role] || 'info')

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getUserList(searchParams)
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const fetchColleges = async () => {
  const res = await getCollegeList()
  collegeList.value = res.data || []
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  Object.assign(form, { id: null, username: '', name: '', role: 'student', collegeId: null, classId: null, phone: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleAssignRole = (row) => {
  ElMessage.info('角色分配功能待实现')
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  await toggleUserStatus(row.id, newStatus)
  ElMessage.success('操作成功')
  fetchList()
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除用户「${row.name}」吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchList()
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (isEdit.value) {
      await updateUser(form)
    } else {
      await addUser(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  })
}

const handleBatchImport = (file) => {
  ElMessage.info('批量导入功能待实现')
  return false
}

onMounted(() => {
  fetchList()
  fetchColleges()
})
</script>
