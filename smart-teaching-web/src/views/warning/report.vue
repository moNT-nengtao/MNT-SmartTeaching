<template>
  <div class="warning-report-page">
    <div class="page-card">
      <div class="page-header">
        <span class="page-title">学业预警报告</span>
        <!-- 移除导出报告按钮 -->
      </div>

      <div v-if="report" class="report-content">
        <!-- 学生基本信息 -->
        <div class="report-section">
          <h3>学生信息</h3>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="姓名">{{ report.studentName }}</el-descriptions-item>
            <el-descriptions-item label="学号">{{ report.studentNo }}</el-descriptions-item>
            <el-descriptions-item label="班级">{{ report.className }}</el-descriptions-item>
            <el-descriptions-item label="学院">{{ report.collegeName }}</el-descriptions-item>
            <el-descriptions-item label="专业">{{ report.majorName }}</el-descriptions-item>
            <el-descriptions-item label="预警等级">
              <el-tag :type="report.level === 'high' ? 'danger' : report.level === 'medium' ? 'warning' : 'info'" effect="dark">
                {{ report.level === 'high' ? '严重' : report.level === 'medium' ? '中等' : '轻微' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 预警详情 -->
        <div class="report-section">
          <h3>预警详情</h3>
          <el-alert :title="report.reason" type="warning" :closable="false" show-icon />
        </div>

        <!-- 数据图表 -->
        <div class="report-section">
          <h3>学业数据</h3>
          <el-row :gutter="20">
            <el-col :span="12">
              <ChartCard title="成绩趋势" :option="scoreOption" :height="280" />
            </el-col>
            <el-col :span="12">
              <ChartCard title="考勤情况" :option="attendanceOption" :height="280" />
            </el-col>
          </el-row>
        </div>

        <!-- 建议 -->
        <div class="report-section">
          <h3>改进建议</h3>
          <div class="suggestion-box">
            <p v-for="(s, i) in report.suggestions" :key="i">{{ i + 1 }}. {{ s }}</p>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无预警报告数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import ChartCard from '@/components/ChartCard.vue'
import { getWarningDetail } from '@/api/warning'

const route = useRoute()
const report = ref(null)

// 成绩趋势图表配置
const scoreOption = computed(() => {
  if (!report.value?.scoreTrend || report.value.scoreTrend.length === 0) return {}
  return {
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: report.value.scoreTrend.map(item => item.courseName)
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100
    },
    series: [{
      data: report.value.scoreTrend.map(item => item.score),
      type: 'bar',
      itemStyle: {
        color: (params) => {
          return params.value < 60 ? '#f56c6c' : '#67c23a'
        }
      }
    }]
  }
})

// 考勤图表配置（考勤五态：考勤成功/迟到/请假/缺勤/旷课）
const attendanceOption = computed(() => {
  if (!report.value?.attendance) return {}
  const data = report.value.attendance
  const pieData = [
    { value: data.attended || 0, name: '考勤成功', itemStyle: { color: '#67c23a' } },
    { value: data.late || 0, name: '迟到', itemStyle: { color: '#e6a23c' } },
    { value: data.leaveCount || 0, name: '请假', itemStyle: { color: '#409eff' } },
    { value: data.absent || 0, name: '缺勤', itemStyle: { color: '#f56c6c' } },
    { value: data.truant || 0, name: '旷课', itemStyle: { color: '#c0392b' } }
  ].filter(item => item.value > 0)
  return {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      label: { show: true, formatter: '{b}\n{d}%' },
      data: pieData
    }]
  }
})

const fetchDetail = async () => {
  if (!route.query.id) return
  try {
    const res = await getWarningDetail(route.query.id)
    report.value = res.data
  } catch (error) {
    console.error('获取预警详情失败', error)
    ElMessage.error('获取预警详情失败')
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.report-section {
  margin-bottom: 24px;
}

.report-section h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 3px solid var(--primary-color);
}

.suggestion-box {
  background: #f0f9eb;
  padding: 16px 20px;
  border-radius: 8px;
  line-height: 2;
  color: var(--text-regular);
}
</style>