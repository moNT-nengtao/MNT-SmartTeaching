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
          <el-select v-model="configForm.scope" style="width: 200px">
            <el-option label="全校开放" value="all" />
            <el-option label="指定年级" value="grade" />
            <el-option label="指定专业" value="major" />
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
      <Pagination v-model:page="searchParams.page" v-model:page-size="searchParams.pageSize" :total="total" @change="fetchList" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { getSelectionConfig, setSelectionTime, getSelectionCourseList } from '@/api/selection'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const configForm = reactive({
  startTime: '',
  endTime: '',
  scope: 'all'
})

const searchParams = reactive({ page: 1, pageSize: 10, keyword: '' })
const searchFields = [{ prop: 'keyword', label: '课程名', type: 'input' }]

const fetchConfig = async () => {
  try {
    const res = await getSelectionConfig()
    Object.assign(configForm, res.data || {})
  } catch (e) {}
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getSelectionCourseList(searchParams)
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleSaveConfig = async () => {
  await setSelectionTime(configForm)
  ElMessage.success('选课设置已保存')
}

const viewStudents = (row) => {
  ElMessage.info(`查看「${row.courseName}」选课名单功能待实现`)
}

onMounted(() => {
  fetchConfig()
  fetchList()
})
</script>
