<template>
  <div class="score-stats-page">
    <SearchForm v-model="searchParams" :fields="searchFields" @search="fetchData" @reset="fetchData" />

    <el-row :gutter="20" class="mb-20">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <div class="stat-card page-card text-center">
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <ChartCard title="成绩分布" :option="pieOption" :loading="loading" :height="320" />
      </el-col>
      <el-col :span="12">
        <ChartCard title="各学院平均分对比" :option="barOption" :loading="loading" :height="320" />
      </el-col>
    </el-row>

    <div class="page-card mt-20">
      <div class="page-header">
        <span class="page-title">异常成绩筛查</span>
        <el-button type="primary" :icon="Download" @click="handleExport">导出成绩</el-button>
      </div>
      <el-table :data="abnormalList" v-loading="loading" border stripe>
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column prop="courseName" label="课程" min-width="140" />
        <el-table-column prop="totalScore" label="成绩" width="100">
          <template #default="{ row }">
            <el-tag type="danger">{{ row.totalScore }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="异常原因" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Download } from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import ChartCard from '@/components/ChartCard.vue'
import { getScoreStats, getAbnormalScores, exportScore } from '@/api/score'

const loading = ref(false)
const abnormalList = ref([])
const pieOption = ref({})
const barOption = ref({})

const statCards = ref([
  { label: '总人数', value: '--', color: '#409eff' },
  { label: '平均分', value: '--', color: '#67c23a' },
  { label: '及格率', value: '--', color: '#e6a23c' },
  { label: '挂科人数', value: '--', color: '#f56c6c' }
])

const searchParams = reactive({ semester: '', collegeId: '' })
const searchFields = [
  {
    prop: 'semester',
    label: '学期',
    type: 'select',
    options: [{ label: '全部', value: '' }]
  }
]

const fetchData = async () => {
  loading.value = true
  try {
    const [statsRes, abnormalRes] = await Promise.all([
      getScoreStats(searchParams),
      getAbnormalScores(searchParams)
    ])
    abnormalList.value = abnormalRes.data || []
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  exportScore(searchParams)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.stat-value {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
