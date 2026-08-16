<template>
  <div class="dashboard-page">
    <el-row :gutter="20" class="mb-20">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <div class="stat-card page-card">
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <ChartCard title="各科成绩雷达图" :option="radarOption" :loading="loading" :height="320" />
      </el-col>
      <el-col :span="12">
        <ChartCard title="绩点趋势" :option="lineOption" :loading="loading" :height="320" />
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <ChartCard title="月度考勤统计" :option="attendanceOption" :loading="loading" :height="280" />
      </el-col>
      <el-col :span="12">
        <ChartCard title="作业提交情况" :option="homeworkOption" :loading="loading" :height="280" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import ChartCard from '@/components/ChartCard.vue'
import { getStudentDashboard } from '@/api/dashboard'

const loading = ref(false)

const statCards = ref([
  { label: '当前GPA', value: '--', color: '#409eff' },
  { label: '已修学分', value: '--', color: '#67c23a' },
  { label: '本月考勤率', value: '--', color: '#e6a23c' },
  { label: '挂科科目', value: '--', color: '#f56c6c' }
])

const radarOption = ref({})
const lineOption = ref({})
const attendanceOption = ref({})
const homeworkOption = ref({})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStudentDashboard()
    // TODO: 填充雷达图（各科优劣）和绩点趋势
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
  margin-bottom: 8px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
