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
        <el-table-column prop="usualScore" label="平时成绩" width="100" />
        <el-table-column prop="finalScore" label="期末成绩" width="100" />
        <el-table-column label="综合成绩" width="120">
          <template #default="{ row }">
            <el-tag :type="scoreTagType(row.totalScore)">{{ row.totalScore }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="绩点" width="80">
          <template #default="{ row }">{{ scoreToGpa(row.totalScore).toFixed(1) }}</template>
        </el-table-column>
        <el-table-column label="等级" width="80">
          <template #default="{ row }">{{ scoreToLevel(row.totalScore) }}</template>
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

const gpa = computed(() => {
  if (scoreList.value.length === 0) return '0.0'
  const total = scoreList.value.reduce((sum, s) => sum + scoreToGpa(s.totalScore) * (s.credit || 0), 0)
  const credits = scoreList.value.reduce((sum, s) => sum + (s.credit || 0), 0)
  return credits > 0 ? (total / credits).toFixed(2) : '0.0'
})

const avgScore = computed(() => {
  if (scoreList.value.length === 0) return '--'
  const total = scoreList.value.reduce((sum, s) => sum + (s.totalScore || 0), 0)
  return (total / scoreList.value.length).toFixed(1)
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
.stats {
  display: flex;
  gap: 10px;
}
</style>
