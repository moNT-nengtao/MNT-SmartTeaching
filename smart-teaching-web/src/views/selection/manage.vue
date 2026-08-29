<template>
  <div class="selection-manage-page">
    <div class="page-card mb-20">
      <div class="page-header">
        <span class="page-title">选课开放设置</span>
      </div>
      <el-form :model="configForm" label-width="120px" inline>
        <el-form-item label="选课开始时间">
          <el-date-picker v-model="configForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="选课结束时间">
          <el-date-picker v-model="configForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="选课范围">
          <el-select v-model="configForm.scope" style="width: 200px" @change="onScopeChange">
            <el-option label="全校开放" value="all" />
            <el-option label="指定年级" value="grade" />
            <el-option label="指定专业" value="major" />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="configForm.scope === 'grade' || configForm.scope === 'major'"
          :label="configForm.scope === 'grade' ? '选择年级' : '选择专业'"
        >
          <el-select
            v-model="configForm.scopeValue"
            multiple
            filterable
            :placeholder="configForm.scope === 'grade' ? '请选择年级' : '请选择专业'"
            style="width: 250px"
          >
            <el-option
              v-if="configForm.scope === 'grade'"
              v-for="year in gradeOptions"
              :key="year"
              :label="year + '级'"
              :value="year"
            />
            <el-option
              v-if="configForm.scope === 'major'"
              v-for="major in majorOptions"
              :key="major.id"
              :label="major.name"
              :value="major.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSaveConfig">保存设置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <SearchForm v-model="searchParams" :fields="searchFields" @search="fetchList" @reset="fetchList" />

    <div class="page-card">
      <div class="page-header">
        <span class="page-title">课程选课名单</span>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="code" label="课程编号" width="120" />
        <el-table-column prop="courseName" label="课程" min-width="140" />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="maxStudents" label="名额上限" width="100" />
        <el-table-column prop="selectedCount" label="已选人数" width="100" />
        <el-table-column label="选课率" width="120">
          <template #default="{ row }">
            <el-progress :percentage="Math.round((row.selectedCount / row.maxStudents) * 100)" :status="row.selectedCount >= row.maxStudents ? 'exception' : ''" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewStudents(row)">查看名单</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination v-model:page="searchParams.pageNum" v-model:page-size="searchParams.pageSize" :total="total" @change="fetchList" />
    </div>

    <!-- 选课学生名单弹窗（带分页） -->
    <el-dialog v-model="studentDialogVisible" :title="`选课学生名单 - ${currentCourseName}`" width="760px">
      <el-table :data="studentList" v-loading="studentLoading" border stripe>
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="majorName" label="所属专业" min-width="160" />
        <el-table-column prop="selectTime" label="选课时间" min-width="180" />
      </el-table>
      <div class="mt-16" style="margin-top: 16px; display: flex; justify-content: flex-end;">
        <Pagination
          v-model:page="studentPage.pageNum"
          v-model:page-size="studentPage.pageSize"
          :total="studentPage.total"
          @change="fetchStudents"
        />
      </div>
      <template #footer>
        <el-button @click="()=>{studentDialogVisible = false; currentCourseId.value=null}">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { getSelectionConfig, setSelectionTime, getSelectionCourseList, getCourseStudents } from '@/api/selection'
import { getMajorList } from '@/api/org'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

// 学生弹窗相关
const studentDialogVisible = ref(false)
const studentLoading = ref(false)
const studentList = ref([])
const currentCourseId = ref(null)
const currentCourseName = ref('')
const studentPage = reactive({ pageNum: 1, pageSize: 10, total: 0 })

// ⭐ 年级选项（近10年）
const currentYear = new Date().getFullYear()
const gradeOptions = []
for (let i = currentYear - 5; i <= currentYear + 1; i++) {
  gradeOptions.push(i)
}

// ⭐ 专业选项
const majorOptions = ref([])

const configForm = reactive({
  startTime: '',
  endTime: '',
  scope: 'all',
  scopeValue: []
})

const searchParams = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const searchFields = [{ prop: 'keyword', label: '课程名/课程编号', type: 'input' }]

// ⭐ 获取专业列表
const fetchMajors = async () => {
  try {
    const res = await getMajorList(null)
    majorOptions.value = res.data ?? []
  } catch (e) {
    console.error('获取专业列表失败', e)
    majorOptions.value = []
  }
}

// ⭐ 选课范围切换时清空已选值
const onScopeChange = () => {
  configForm.scopeValue = []
}

// ⭐ 获取选课配置
const fetchConfig = async () => {
  try {
    const res = await getSelectionConfig()
    const data = res.data || {}
    configForm.startTime = data.startTime || ''
    configForm.endTime = data.endTime || ''
    configForm.scope = data.scope || 'all'

    if (data.scopeValue) {
      if (typeof data.scopeValue === 'string') {
        try {
          configForm.scopeValue = JSON.parse(data.scopeValue)
        } catch {
          configForm.scopeValue = []
        }
      } else if (Array.isArray(data.scopeValue)) {
        configForm.scopeValue = data.scopeValue
      } else {
        configForm.scopeValue = []
      }
    } else {
      configForm.scopeValue = []
    }
  } catch (e) {
    console.error('获取选课配置失败', e)
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getSelectionCourseList(searchParams)
    tableData.value = res.data?.list ?? res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } finally {
    loading.value = false
  }
}

// ⭐ 保存选课配置
const handleSaveConfig = async () => {
  if (configForm.scope !== 'all' && (!configForm.scopeValue || configForm.scopeValue.length === 0)) {
    ElMessage.warning('请选择具体的年级或专业')
    return
  }

  const submitData = {
    startTime: configForm.startTime,
    endTime: configForm.endTime,
    scope: configForm.scope,
    scopeValue: JSON.stringify(configForm.scopeValue)
  }

  await setSelectionTime(submitData)
  ElMessage.success('选课设置已保存')
}

// 查看选课名单（打开弹窗 + 重置分页 + 拉第一页）
const viewStudents = (row) => {
  currentCourseId.value = row.courseId
  currentCourseName.value = row.courseName
  studentDialogVisible.value = true
  studentPage.pageNum = 1
  studentPage.pageSize = 10
  studentPage.total = 0
  studentList.value = []
  fetchStudents()
}

// 拉取学生名单（分页）
const fetchStudents = async () => {
  if (!currentCourseId.value) {
    return
  }
  studentLoading.value = true
  try {
    const res = await getCourseStudents(currentCourseId.value, {
      pageNum: studentPage.pageNum,
      pageSize: studentPage.pageSize
    })
    studentList.value = res.data?.list ?? res.data?.records ?? []
    studentPage.total = res.data?.total ?? 0
  } catch (err) {
    ElMessage.error('获取选课名单失败')
    console.error(err)
  } finally {
    studentLoading.value = false
  }
}


onMounted(() => {
  Promise.all([fetchMajors(), fetchConfig(), fetchList()])
})
</script>
