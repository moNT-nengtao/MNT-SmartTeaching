<template>
  <div class="schedule-page">
    <SearchForm v-model="searchParams" :fields="searchFields" @search="fetchList" @reset="fetchList" />

    <div class="page-card">
      <div class="page-header">
        <span class="page-title">排课管理</span>
        <div>
          <el-button :icon="Download" @click="handleExport">导出课表</el-button>
          <el-button type="primary" :icon="Plus" @click="handleAdd">手动排课</el-button>
          <el-button type="success" :icon="Grid" @click="handleBatch">批量排课</el-button>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="courseName" label="课程" min-width="140" />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="weekday" label="星期" width="80">
          <template #default="{ row }">{{ weekdayText(row.weekday) }}</template>
        </el-table-column>
        <el-table-column prop="section" label="节次" width="100" />
        <el-table-column prop="classroom" label="教室" width="100" />
        <el-table-column prop="weekRange" label="周次" width="100" />
        <el-table-column label="操作" width="150" fixed="right">
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑排课' : '新增排课'" width="550px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" filterable style="width: 100%">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherId">
          <el-select v-model="form.teacherId" filterable style="width: 100%">
            <el-option v-for="t in teacherList" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select v-model="form.classId" filterable style="width: 100%">
            <el-option v-for="c in classList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="星期" prop="weekday">
          <el-select v-model="form.weekday" style="width: 100%">
            <el-option v-for="i in 7" :key="i" :label="weekdayText(i)" :value="i" />
          </el-select>
        </el-form-item>
        <el-form-item label="节次" prop="section">
          <el-select v-model="form.section" style="width: 100%">
            <el-option v-for="s in sections" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="教室" prop="classroom">
          <el-input v-model="form.classroom" />
        </el-form-item>
        <el-form-item label="周次范围" prop="weekRange">
          <el-input v-model="form.weekRange" placeholder="如：1-16" />
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
import { Plus, Download, Grid } from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { getScheduleList, addSchedule, updateSchedule, deleteSchedule, checkScheduleConflict } from '@/api/course'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const courseList = ref([])
const teacherList = ref([])
const classList = ref([])
const sections = ['第1-2节', '第3-4节', '第5-6节', '第7-8节', '第9-10节', '第11-12节']

const searchParams = reactive({
  page: 1,
  pageSize: 10,
  courseId: '',
  teacherId: ''
})

const searchFields = [
  { prop: 'courseId', label: '课程', type: 'select', options: [] }
]

const form = reactive({
  id: null,
  courseId: null,
  teacherId: null,
  classId: null,
  weekday: 1,
  section: '',
  classroom: '',
  weekRange: '1-16'
})

const rules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  weekday: [{ required: true, message: '请选择星期', trigger: 'change' }],
  section: [{ required: true, message: '请选择节次', trigger: 'change' }]
}

const weekdayText = (d) => ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日'][d] || d

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getScheduleList(searchParams)
    tableData.value = res.data?.list ?? res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, courseId: null, teacherId: null, classId: null, weekday: 1, section: '', classroom: '', weekRange: '1-16' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该排课记录吗？', '提示', { type: 'warning' })
    .then(async () => {
      await deleteSchedule(row.id)
      ElMessage.success('删除成功')
      fetchList()
    })
    .catch(() => {})
}

const handleBatch = () => {
  ElMessage.info('批量排课功能待实现')
}

const handleExport = () => {
  ElMessage.info('导出功能待实现')
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    // 冲突校验
    try {
      await checkScheduleConflict(form)
    } catch (e) {
      ElMessage.warning('存在排课冲突，请检查')
      return
    }
    if (isEdit.value) {
      await updateSchedule(form)
    } else {
      await addSchedule(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  })
}

onMounted(() => {
  fetchList()
})
</script>
