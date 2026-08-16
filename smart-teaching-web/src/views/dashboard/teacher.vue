<template>
  <div class="dashboard-page">
    <el-row :gutter="20" class="mb-20">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <div class="stat-card page-card">
          <el-icon :size="32" :color="card.color"><component :is="card.icon" /></el-icon>
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <ChartCard title="所授课程成绩分布" :option="barOption" :loading="loading" :height="320" />
      </el-col>
      <el-col :span="12">
        <ChartCard title="教学评价评分" :option="radarOption" :loading="loading" :height="320" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import ChartCard from '@/components/ChartCard.vue'
import { getTeacherDashboard } from '@/api/dashboard'

const loading = ref(false)

const statCards = ref([
  { label: '授课课程数', value: '--', color: '#409eff', icon: 'Notebook' },
  { label: '选课人数', value: '--', color: '#67c23a', icon: 'User' },
  { label: '平均分', value: '--', color: '#e6a23c', icon: 'TrendCharts' },
  { label: '评价评分', value: '--', color: '#f56c6c', icon: 'Star' }
])

const barOption = ref({})
const radarOption = ref({})

const fetchData = async () => {
  loading.value = true
  try {
    await getTeacherDashboard()
  } catch (e) {
    // 错误已处理
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.stat-card {
  text-align: center;
  padding: 20px 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  margin: 10px 0 6px;
  color: var(--text-primary);
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
