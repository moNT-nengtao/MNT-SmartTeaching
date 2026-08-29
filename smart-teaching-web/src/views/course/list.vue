<template>
  <div class="course-page">
    <SearchForm
      :model-value="searchParams"
      :fields="searchFields"
      @update:model-value="Object.assign(searchParams, $event)"
      @search="fetchList"
      @reset="fetchList"
    />

    <div class="page-card">
      <div class="page-header">
        <span class="page-title">课程管理</span>
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增课程</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="code" label="课程编号" width="120" />
        <el-table-column prop="name" label="课程名称" min-width="150" />
        <el-table-column prop="teacherName" label="授课教师" width="140" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="semester" label="学期" width="120" />
        <el-table-column prop="capacity" label="最大人数" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
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
        <el-form-item label="课程编号" prop="code">
          <el-input v-model="form.code" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherId">
          <el-select v-model="form.teacherId" style="width: 100%">
            <el-option v-for="t in teacherList" :key="t.id" :label="t.real_name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="form.credit" :min="0" :max="10" :step="0.5" />
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-select v-model="form.semester" style="width: 100%" placeholder="请选择学期" clearable>
            <el-option
              v-for="item in semesterOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="最大人数" prop="capacity">
          <el-input-number v-model="form.capacity" :min="0" :max="500" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { getCourseList, addCourse, updateCourse, deleteCourse } from '@/api/course'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const teacherList = ref([
  { id: 2, real_name: '张教授' },
  { id: 3, real_name: '李老师' }
])

// ==================== 学期工具函数 ====================
/**
 * 获取当前学期编号
 * 2-7月：春季学期（2），8-12月：秋季学期（1），1月：春季学期（2）
 */
const getCurrentSemesterNumber = (month) => {
  if (month >= 8 && month <= 12) return 1
  return 2
}

/**
 * 获取当前学期字符串
 */
const getCurrentSemester = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const sem = getCurrentSemesterNumber(month)
  return `${year}-${year + 1}-${sem}`
}

/**
 * 生成学期选项列表
 * @param {number} startYear - 起始年份，默认2020
 * @param {number} futureCount - 未来学期数量，默认2
 */
const generateSemesterOptions = (startYear = 2020, futureCount = 2) => {
  const options = []
  const now = new Date()
  const currentYear = now.getFullYear()
  const currentMonth = now.getMonth() + 1
  const currentSem = getCurrentSemesterNumber(currentMonth)

  // 计算结束年份和学期
  let endYear = currentYear
  let endSem = currentSem + futureCount
  while (endSem > 2) {
    endSem -= 2
    endYear += 1
  }

  // 生成所有学期
  for (let year = startYear; year <= endYear; year++) {
    for (let sem = 1; sem <= 2; sem++) {
      // 跳过未来超出范围的学期
      if (year === endYear && sem > endSem) continue
      if (year > endYear) continue

      const label = `${year}-${year + 1}-${sem}`
      options.push({ label, value: label })
    }
  }

  // 按时间倒序（最新的在前）
  return options.reverse()
}

// ==================== 学期选项 ====================
const semesterOptions = generateSemesterOptions(2020, 2)

// ==================== 搜索参数 ====================
const searchParams = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  semester: '',
  status: ''
})

// 搜索表单配置
const searchFields = computed(() => [
  { prop: 'keyword', label: '关键词', type: 'input', placeholder: '课程编号/名称' },
  {
    prop: 'semester',
    label: '学期',
    type: 'select',
    options: [
      { label: '全部', value: '' },
      ...semesterOptions
    ]
  },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: [
      { label: '全部', value: '' },
      { label: '启用', value: 1 },
      { label: '停用', value: 0 }
    ]
  }
])

// ==================== 表单 ====================
const form = reactive({
  id: null,
  code: '',
  name: '',
  teacherId: null,
  credit: 2,
  semester: getCurrentSemester(), // 默认当前学期
  capacity: 60,
  description: ''
})

const rules = {
  code: [{ required: true, message: '请输入课程编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  teacherId: [{ required: true, message: '请选择授课教师', trigger: 'change' }],
  semester: [{ required: true, message: '请选择学期', trigger: 'change' }]
}

// ==================== 方法 ====================
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

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    code: '',
    name: '',
    teacherId: null,
    credit: 2,
    semester: getCurrentSemester(),
    capacity: 60,
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除课程「${row.name}」吗？`, '提示', { type: 'warning' })
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

// ==================== 生命周期 ====================
onMounted(() => {
  fetchList()
})
</script>