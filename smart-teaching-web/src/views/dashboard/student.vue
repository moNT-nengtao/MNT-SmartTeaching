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
        <ChartCard title="各科成绩对比" :option="scoreCompareOption" :loading="loading" :height="280" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import ChartCard from '@/components/ChartCard.vue'
import { getStudentDashboard } from '@/api/dashboard'

const loading = ref(false)

// 顶部统计卡片
const statCards = ref([
  { label: '当前GPA', value: '--', color: '#409eff' },
  { label: '已修学分', value: '--', color: '#67c23a' },
  { label: '本月考勤率', value: '--', color: '#e6a23c' },
  { label: '挂科科目', value: '--', color: '#f56c6c' }
])

// 图表配置
const radarOption = ref({
  tooltip: {},
  radar: { indicator: [] },
  series: [{ type: 'radar', data: [{ value: [], name: '我的分数' }] }]
})

const lineOption = ref({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value', min: 0, max: 4 },
  series: [{ type: 'line', data: [], smooth: true, name: 'GPA' }]
})

const attendanceOption = ref({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value', min: 0, max: 100 },
  series: [{ type: 'line', data: [], smooth: true, areaStyle: {}, name: '考勤率' }]
})

// 成绩对比柱状图（替代原作业提交）
const scoreCompareOption = ref({
  tooltip: { trigger: 'axis' },
  legend: { data: ['平时成绩', '期末成绩', '总评成绩'] },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value', min: 0, max: 100 },
  series: [
    { name: '平时成绩', type: 'bar', data: [], barWidth: '20%' },
    { name: '期末成绩', type: 'bar', data: [], barWidth: '20%' },
    { name: '总评成绩', type: 'bar', data: [], barWidth: '20%' }
  ]
})

// 数据获取
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStudentDashboard()
    const data = res.data

    // 1. 填充顶部统计卡片
    const cards = data.statCards || {}
    statCards.value[0].value = cards.gpa ?? '--'
    statCards.value[1].value = cards.finishedCredit ?? '--'
    statCards.value[2].value = cards.attendanceRate != null ? cards.attendanceRate + '%' : '--'
    statCards.value[3].value = cards.failSubjectCount ?? '--'

    // 2. 各科成绩雷达图
    const radar = data.subjectRadar || {}
    radarOption.value.radar.indicator = radar.indicator || []
    const radarData = radar.data || []
    radarOption.value.series[0].data = radarData.length > 0 ? radarData : [{ value: [], name: '我的分数' }]

    // 3. 绩点趋势折线图
    const trend = data.gpaTrend || {}
    const categories = trend.categories || []
    const seriesList = trend.series || []
    lineOption.value.xAxis.data = categories
    lineOption.value.series[0].data = seriesList.length > 0 ? seriesList[0].data || [] : []
    lineOption.value.series[0].name = seriesList.length > 0 ? seriesList[0].name : 'GPA'

    // 4. 月度考勤统计
    const attendance = data.attendanceMonth || {}
    const attCategories = attendance.categories || []
    const attSeries = attendance.series || []
    attendanceOption.value.xAxis.data = attCategories
    attendanceOption.value.series[0].data = attSeries.length > 0 ? attSeries[0].data || [] : []
    attendanceOption.value.series[0].name = attSeries.length > 0 ? attSeries[0].name : '考勤率'

    // 5. 各科成绩对比柱状图（从雷达图数据衍生）
    // 如果后端没有专门返回成绩对比数据，从雷达图数据转换
    if (radar.indicator && radar.indicator.length > 0 && radarData.length > 0) {
      const indicatorNames = radar.indicator.map(item => item.name || '')
      const scores = radarData[0]?.value || []
      
      // 假设后端返回的雷达图数据中包含成绩详情，这里做展示
      // 如果有专门的成绩对比数据，使用专门的字段
      const compareData = data.scoreCompare || {}
      if (compareData.categories && compareData.categories.length > 0) {
        // 使用专门的成绩对比数据
        scoreCompareOption.value.xAxis.data = compareData.categories || []
        const compareSeries = compareData.series || []
        compareSeries.forEach(item => {
          const existing = scoreCompareOption.value.series.find(s => s.name === item.name)
          if (existing) {
            existing.data = item.data || []
          }
        })
      } else {
        // 降级方案：用雷达图数据展示单科成绩
        scoreCompareOption.value.xAxis.data = indicatorNames
        // 使用总评成绩（如果雷达图有多个系列，取第一个）
        const mainData = radarData[0]?.value || []
        // 没有平时/期末数据时，只展示总评
        scoreCompareOption.value.series[0].data = mainData // 平时成绩用同一个数据
        scoreCompareOption.value.series[1].data = mainData.map(v => Math.round(v * 0.7)) // 模拟期末
        scoreCompareOption.value.series[2].data = mainData
        // 隐藏图例，避免误导
        scoreCompareOption.value.legend.data = ['总评成绩']
        scoreCompareOption.value.series = [
          { name: '总评成绩', type: 'bar', data: mainData, barWidth: '30%', itemStyle: { color: '#409eff' } }
        ]
      }
    }

  } catch (e) {
    console.error('学生仪表盘加载异常', e)
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
.mb-20 {
  margin-bottom: 20px;
}
.mt-20 {
  margin-top: 20px;
}
.page-card {
  background: #fff;
  border-radius: 8px;
}
</style>