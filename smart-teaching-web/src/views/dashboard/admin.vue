<template>
  <div class="dashboard-page">
    <!-- 数据卡片 -->
    <el-row :gutter="20" class="mb-20">
      <el-col :span="4" v-for="card in statCards" :key="card.label">
        <div class="stat-card page-card">
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <ChartCard title="各学院学生人数分布" :option="barOption" :loading="loading" :height="320" />
      </el-col>
      <el-col :span="12">
        <ChartCard title="师生比例" :option="pieOption" :loading="loading" :height="320" />
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <ChartCard title="近7日系统活跃度" :option="lineOption" :loading="loading" :height="300" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import ChartCard from '@/components/ChartCard.vue'
import { getAdminDashboard } from '@/api/dashboard'

const loading = ref(false)

const statCards = ref([
  { label: '班级总数', value: '--', color: '#409eff' },
  { label: '教师人数', value: '--', color: '#67c23a' },
  { label: '学生人数', value: '--', color: '#e6a23c' },
  { label: '课程总数', value: '--', color: '#f56c6c' },
  { label: '选课率', value: '--', color: '#909399' },
  { label: '考勤合格率', value: '--', color: '#409eff' }
])

const barOption = ref({})
const pieOption = ref({})
const lineOption = ref({})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAdminDashboard()
    const data = res.data
    // TODO: 根据后端返回数据填充图表
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
