<template>
  <div class="my-selection-page">
    <div class="page-card">
      <div class="page-header">
        <span class="page-title">我的已选课程</span>
        <el-tag type="success">共 {{ total }} 门课</el-tag>
      </div>
      <el-table :data="myCourses" v-loading="loading" border stripe>
        <el-table-column prop="code" label="课程编号" width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="week" label="周次" width="120" />
        <el-table-column prop="scheduleTime" label="上课时间/教室" min-width="200" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" :disabled="actionLoading" @click="handleDrop(row)">退课</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="myCourses.length === 0 && !loading" description="还没有选择任何课程" />
      <Pagination
        v-model:page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        @change="fetchList"
      />
    </div>
  </div>
</template>


<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'
import { getMyCourses, dropCourse } from '@/api/selection'


const loading = ref(false)
const actionLoading = ref(false)
const myCourses = ref([])
const total = ref(0)


const query = reactive({
  pageNum: 1,
  pageSize: 10
})


const fetchList = async () => {
  loading.value = true
  try {
    const res = await getMyCourses(query)
    myCourses.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } finally {
    loading.value = false
  }
}


const handleDrop = (row) => {
  ElMessageBox.confirm(`确定退选「${row.courseName}」吗？`, '退课确认', { type: 'warning' })
    .then(async () => {
      actionLoading.value = true
      try {
        await dropCourse(row.courseId)
        ElMessage.success('退课成功')
        await fetchList()
      } finally {
        actionLoading.value = false
      }
    })
    .catch(() => {})
}


onMounted(() => {
  fetchList()
})
</script>


<style scoped>
.my-selection-page {
  padding: 16px;
}
.page-card {
  background: #fff;
  padding: 20px;
  border-radius: 6px;
}
.page-header {
  display: flex;
  align-items: center;
  gap:12px;
  margin-bottom:16px;
}
.page-title {
  font-size:18px;
  font-weight:600;
}
</style>
