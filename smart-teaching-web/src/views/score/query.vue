<template>
  <div class="score-query-page">
    <SearchForm v-model="searchParams" :fields="searchFields" @search="fetchList" @reset="fetchList" />

    <div class="page-card">
      <div class="page-header">
        <span class="page-title">我的成绩</span>
        <div class="stats">
          <el-tag type="primary">GPA：{{ gpa }}</el-tag>
          <el-tag type="success">平均分：{{ avgScore }}</el-tag>
        </div>
      </div>
      <el-table :data="scoreList" v-loading="loading" border stripe>
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="usualScore" label="平时成绩" width="100">
          <template #default="{ row }">
            {{ row.usualScore !== null && row.usualScore !== undefined ? row.usualScore : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="finalScore" label="期末成绩" width="100">
          <template #default="{ row }">
            {{ row.finalScore !== null && row.finalScore !== undefined ? row.finalScore : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="综合成绩" width="120">
          <template #default="{ row }">
            <el-tag v-if="hasValidScore(row)" :type="scoreTagType(row.totalScore)">
              {{ row.totalScore }}
            </el-tag>
            <span v-else class="empty-score">-</span>
          </template>
        </el-table-column>
        <el-table-column label="绩点" width="80">
          <template #default="{ row }">
            <span v-if="hasValidScore(row)">{{ scoreToGpa(row.totalScore).toFixed(1) }}</span>
            <span v-else class="empty-score">-</span>
          </template>
        </el-table-column>
        <el-table-column label="等级" width="80">
          <template #default="{ row }">
            <span v-if="hasValidScore(row)">{{ scoreToLevel(row.totalScore) }}</span>
            <span v-else class="empty-score">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="semester" label="学期" width="120" />
      </el-table>
      <el-empty v-if="scoreList.length === 0 && !loading" description="暂无成绩数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import SearchForm from '@/components/SearchForm.vue'
import { getMyScores } from '@/api/score'
import { scoreToGpa, scoreToLevel } from '@/utils/format'

const loading = ref(false)
const scoreList = ref([])

const searchParams = reactive({ semester: '' })
const searchFields = [
  {
    prop: 'semester',
    label: '学期',
    type: 'select',
    options: [
      { label: '全部学期', value: '' },
      { label: '2025-2026-1', value: '2025-2026-1' },
      { label: '2025-2026-2', value: '2025-2026-2' }
    ]
  }
]

/**
 * 判断成绩是否有效（不为null、undefined，且不为空字符串）
 */
const hasValidScore = (row) => {
  const score = row.totalScore
  return score !== null && score !== undefined && score !== '' && !isNaN(score)
}

/**
 * 判断单个成绩是否有效
 */
const hasValidSingleScore = (score) => {
  return score !== null && score !== undefined && score !== '' && !isNaN(score)
}

const gpa = computed(() => {
  // 过滤出有有效成绩的记录
  const validScores = scoreList.value.filter(row => hasValidScore(row))
  if (validScores.length === 0) return '-'
  
  const total = validScores.reduce((sum, s) => {
    const gpaValue = scoreToGpa(s.totalScore)
    const credit = s.credit || 0
    return sum + gpaValue * credit
  }, 0)
  
  const credits = validScores.reduce((sum, s) => sum + (s.credit || 0), 0)
  return credits > 0 ? (total / credits).toFixed(2) : '-'
})

const avgScore = computed(() => {
  // 过滤出有有效成绩的记录
  const validScores = scoreList.value.filter(row => hasValidScore(row))
  if (validScores.length === 0) return '-'
  
  const total = validScores.reduce((sum, s) => sum + (s.totalScore || 0), 0)
  return (total / validScores.length).toFixed(1)
})

const scoreTagType = (score) => {
  if (score >= 90) return 'success'
  if (score >= 60) return ''
  return 'danger'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getMyScores(searchParams)
    scoreList.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.page-card {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
}

.stats {
  display: flex;
  gap: 10px;
}

.empty-score {
  color: #c0c4cc;
  font-size: 14px;
}
</style>