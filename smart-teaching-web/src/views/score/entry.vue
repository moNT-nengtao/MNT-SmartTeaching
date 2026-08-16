<template>
  <div class="score-entry-page">
    <div class="page-card mb-20">
      <el-form :model="filterForm" inline>
        <el-form-item label="选择课程">
          <el-select v-model="filterForm.courseId" filterable style="width: 280px" @change="fetchScoreList">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-upload :show-file-list="false" :before-upload="handleBatchImport" accept=".xlsx,.xls">
            <el-button :icon="Upload">批量导入成绩</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
    </div>

    <div class="page-card" v-if="filterForm.courseId">
      <div class="page-header">
        <span class="page-title">成绩录入</span>
        <el-button type="primary" @click="handleSaveAll">保存全部</el-button>
      </div>
      <el-table :data="scoreList" v-loading="loading" border stripe>
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column label="平时成绩" width="140">
          <template #default="{ row }">
            <el-input-number v-model="row.usualScore" :min="0" :max="100" size="small" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="期末成绩" width="140">
          <template #default="{ row }">
            <el-input-number v-model="row.finalScore" :min="0" :max="100" size="small" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="综合成绩" width="120">
          <template #default="{ row }">
            <el-tag :type="scoreTagType(calcTotal(row))">{{ calcTotal(row) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-empty v-else description="请先选择课程" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { getCourseScoreList, enterScore, batchImportScore } from '@/api/score'

const loading = ref(false)
const courseList = ref([])
const scoreList = ref([])

const filterForm = reactive({ courseId: null })

const calcTotal = (row) => {
  const usual = row.usualScore || 0
  const final = row.finalScore || 0
  return Math.round(usual * 0.3 + final * 0.7)
}

const scoreTagType = (score) => {
  if (score >= 90) return 'success'
  if (score >= 60) return ''
  return 'danger'
}

const fetchScoreList = async () => {
  if (!filterForm.courseId) return
  loading.value = true
  try {
    const res = await getCourseScoreList(filterForm.courseId)
    scoreList.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleSaveAll = async () => {
  const data = scoreList.value.map((row) => ({
    studentId: row.studentId,
    usualScore: row.usualScore,
    finalScore: row.finalScore,
    totalScore: calcTotal(row)
  }))
  await enterScore({ courseId: filterForm.courseId, scores: data })
  ElMessage.success('成绩保存成功')
}

const handleBatchImport = (file) => {
  ElMessage.info('批量导入功能待实现')
  return false
}

onMounted(() => {
  // TODO: 获取教师授课课程列表
})
</script>
