<template>
  <div class="dashboard-page">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-20">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <div class="stat-card page-card">
          <el-icon :size="32" :color="card.color">
            <component :is="card.icon" />
          </el-icon>
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <!-- 左：成绩分布柱状图 -->
      <el-col :span="12">
        <ChartCard 
          title="所授课程成绩分布" 
          :option="barOption" 
          :loading="loading" 
          :height="320" 
        />
      </el-col>
      <!-- 右：近7日考勤签到率趋势折线图 -->
      <el-col :span="12">
        <ChartCard 
          title="近7日考勤签到率趋势" 
          :option="lineOption" 
          :loading="loading" 
          :height="320" 
        />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import ChartCard from '@/components/ChartCard.vue'
import { getTeacherDashboard } from '@/api/dashboard'

const loading = ref(false)

// 统计卡片数据
const statCards = ref([
  { label: '授课课程数', value: '--', color: '#409eff', icon: 'Notebook' },
  { label: '选课人数', value: '--', color: '#67c23a', icon: 'User' },
  { label: '平均分', value: '--', color: '#e6a23c', icon: 'TrendCharts' },
  { label: '评价评分', value: '--', color: '#f56c6c', icon: 'Star' }
])

// 图表配置
const barOption = ref({})
const lineOption = ref({})

/**
 * 获取数据
 */
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTeacherDashboard()
    const data = res.data

    console.log('=== 后端返回数据 ===')
    console.log('scoreDistribution:', data.scoreDistribution)
    console.log('attendanceTrend:', data.attendanceTrend)

    // 1. 统计卡片赋值
    statCards.value[0].value = data.statCards?.courseCount ?? 0
    statCards.value[1].value = data.statCards?.studentTotal ?? 0
    statCards.value[2].value = data.statCards?.avgScore ?? 0
    statCards.value[3].value = data.statCards?.avgEvaluate ?? 0

    // 2. ★ 成绩分布柱状图
    if (data.scoreDistribution && data.scoreDistribution.categories) {
      const seriesData = data.scoreDistribution.series || []
      
      barOption.value = {
        tooltip: { 
          trigger: 'axis',
          formatter: function(params) {
            if (!params || params.length === 0) return ''
            let html = `<strong>${params[0].axisValue}</strong><br/>`
            params.forEach(p => {
              html += `${p.marker} ${p.seriesName}：<strong>${p.value}</strong> 人<br/>`
            })
            return html
          }
        },
        legend: { 
          data: seriesData.map(s => s.name),
          bottom: 0
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          top: '5%',
          containLabel: true
        },
        xAxis: { 
          data: data.scoreDistribution.categories,
          axisLabel: { fontSize: 12 }
        },
        yAxis: { 
          name: '人数',
          minInterval: 1
        },
        series: seriesData.map(item => ({
          ...item,
          type: 'bar',
          barWidth: '45%',
          itemStyle: {
            borderRadius: [4, 4, 0, 0],
            color: function(params) {
              const colors = ['#f56c6c', '#e6a23c', '#f0c78a', '#67c23a', '#409eff']
              return colors[params.dataIndex] || '#409eff'
            }
          },
          label: {
            show: true,
            position: 'top',
            formatter: '{c}人'
          }
        }))
      }
    }

    // 3. ★ 近7日考勤签到率趋势折线图
    if (data.attendanceTrend && data.attendanceTrend.categories) {
      const seriesData = data.attendanceTrend.series || []
      
      lineOption.value = {
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            if (!params || params.length === 0) return ''
            let html = `<strong>${params[0].axisValue}</strong><br/>`
            params.forEach(p => {
              html += `${p.marker} ${p.seriesName}：<strong>${p.value}%</strong><br/>`
            })
            return html
          }
        },
        legend: {
          data: seriesData.map(s => s.name),
          bottom: 0
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          top: '5%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: data.attendanceTrend.categories,
          axisLabel: { fontSize: 12 }
        },
        yAxis: {
          name: '签到率 (%)',
          min: 0,
          max: 100,
          axisLabel: {
            formatter: '{value}%'
          }
        },
        series: seriesData.map(item => ({
          ...item,
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: { width: 3 },
          areaStyle: { opacity: 0.25 },
          markLine: {
            silent: true,
            data: [{
              yAxis: 80,
              label: {
                formatter: '⚠ 警戒线 80%',
                color: '#f56c6c',
                fontSize: 11
              },
              lineStyle: {
                color: '#f56c6c',
                type: 'dashed',
                width: 2
              }
            }]
          },
          label: {
            show: true,
            formatter: '{c}%',
            fontSize: 11
          }
        }))
      }
    } else {
      // 无数据时显示提示
      lineOption.value = {
        title: {
          text: '暂无考勤数据',
          left: 'center',
          top: 'center',
          textStyle: {
            color: '#999',
            fontSize: 14,
            fontWeight: 'normal'
          }
        }
      }
    }

  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.dashboard-page {
  padding: 20px;
}

.mb-20 {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 24px 10px;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.10);
}

.stat-value {
  font-size: 30px;
  font-weight: 700;
  margin: 10px 0 6px;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}
</style>