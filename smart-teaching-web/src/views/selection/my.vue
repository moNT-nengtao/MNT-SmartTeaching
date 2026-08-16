<template>
  <div class="my-selection-page">
    <div class="page-card">
      <div class="page-header">
        <span class="page-title">我的已选课程</span>
        <el-tag type="success">共 {{ myCourses.length }} 门课</el-tag>
      </div>
      <el-table :data="myCourses" v-loading="loading" border stripe>
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="scheduleTime" label="上课时间" width="160" />
        <el-table-column prop="classroom" label="教室" width="100" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleDrop(row)">退课</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="myCourses.length === 0 && !loading" description="还没有选择任何课程" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyCourses, dropCourse } from '@/api/selection'

const loading = ref(false)
const myCourses = ref([])

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getMyCourses()
    myCourses.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleDrop = (row) => {
  ElMessageBox.confirm(`确定退选「${row.courseName}」吗？`, '退课确认', { type: 'warning' })
    .then(async () => {
      await dropCourse(row.id)
      ElMessage.success('退课成功')
      fetchList()
    })
    .catch(() => {})
}

onMounted(() => {
  fetchList()
})
</script>
