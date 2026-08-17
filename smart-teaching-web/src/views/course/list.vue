<template>
  <div class="course-page">
    <SearchForm v-model="searchParams" :fields="searchFields" @search="fetchList" @reset="fetchList" />

    <div class="page-card">
      <div class="page-header">
        <span class="page-title">课程管理</span>
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增课程</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="courseCode" label="课程编号" width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="collegeName" label="所属学院" width="140" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="hours" label="学时" width="80" />
        <el-table-column prop="maxStudents" label="最大人数" width="100" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑课程' : '新增课程'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="课程编号" prop="courseCode">
          <el-input v-model="form.courseCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" />
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId">
          <el-select v-model="form.collegeId" style="width: 100%">
            <el-option v-for="c in collegeList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="form.credit" :min="0" :max="10" :step="0.5" />
        </el-form-item>
        <el-form-item label="学时" prop="hours">
          <el-input-number v-model="form.hours" :min="0" :max="200" />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxStudents">
          <el-input-number v-model="form.maxStudents" :min="0" :max="500" />
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
import { Plus } from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { getCourseList, addCourse, updateCourse, deleteCourse } from '@/api/course'
import { getCollegeList } from '@/api/org'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const collegeList = ref([])

const searchParams = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  collegeId: ''
})

const searchFields = [
  { prop: 'keyword', label: '关键词', type: 'input', placeholder: '课程编号/名称' }
]

const form = reactive({
  id: null,
  courseCode: '',
  courseName: '',
  collegeId: null,
  credit: 2,
  hours: 32,
  maxStudents: 60
})

const rules = {
  courseCode: [{ required: true, message: '请输入课程编号', trigger: 'blur' }],
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  collegeId: [{ required: true, message: '请选择学院', trigger: 'change' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getCourseList(searchParams)
    tableData.value = res.data?.list ?? res.data?.records ?? []
    total.value = res.data?.total ?? 0
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
  Object.assign(form, { id: null, courseCode: '', courseName: '', collegeId: null, credit: 2, hours: 32, maxStudents: 60 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除课程「${row.courseName}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteCourse(row.id)
      ElMessage.success('删除成功')
      fetchList()
    })
    .catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (isEdit.value) {
      await updateCourse(form)
    } else {
      await addCourse(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  })
}

onMounted(() => {
  fetchList()
  fetchColleges()
})
</script>
