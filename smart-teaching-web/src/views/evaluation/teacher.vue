<template>
  <div class="evaluation-teacher-page">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-20">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <div class="stat-card page-card text-center">
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 雷达图 -->
      <el-col :span="12">
        <ChartCard title="各维度评分" :option="radarOption" :loading="loading" :height="320" />
      </el-col>

      <!-- 评价列表 -->
      <el-col :span="12">
        <div class="page-card" style="height: 100%">
          <div class="page-header">
            <span class="page-title">课程评价列表</span>
          </div>

          <!-- 课程下拉 -->
          <el-select
              v-model="selectedCourse"
              filterable
              style="width: 100%; margin-bottom: 16px"
              @change="fetchEvaluations"
          >
            <el-option
                v-for="c in courseList"
                :key="c.id"
                :label="c.courseName"
                :value="c.id"
            />
          </el-select>

          <!-- 评价列表 -->
          <div class="evaluation-list">
            <div v-for="item in evaluationList" :key="item.id" class="evaluation-item">
              <div class="eval-header">
                <el-rate v-model="item.score" disabled show-score score-template="{value}" :max="5" />
                <span class="eval-time">{{ item.createTime }}</span>
              </div>
              <div class="eval-content">{{ item.comment }}</div>
              <div class="eval-tags">
                <el-tag v-for="dim in dimensionLabels" :key="dim.key" size="small" type="info">
                  {{ dim.label }}：{{ item[dim.key] }}分
                </el-tag>
              </div>
            </div>
            <el-empty v-if="evaluationList.length === 0" description="暂无评价" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import ChartCard from '@/components/ChartCard.vue'
import { getEvaluationDashboard, getEvaluationList } from '@/api/evaluation'

const loading = ref(false)
const courseList = ref([])
const selectedCourse = ref('')
const evaluationList = ref([])
const radarOption = ref({})

const dimensionLabels = [
  { key: 'teachingAbility', label: '授课能力' },
  { key: 'classAtmosphere', label: '课堂氛围' },
  { key: 'knowledgeClarity', label: '知识讲解' },
  { key: 'homeworkFeedback', label: '作业批改' },
  { key: 'qaService', label: '答疑服务' }
]

const statCards = ref([
  { label: '综合评分', value: '--', color: '#e6a23c' },
  { label: '评价人数', value: '--', color: '#409eff' },
  { label: '好评率', value: '--', color: '#67c23a' },
  { label: '被评课程', value: '--', color: '#909399' }
])

/**
 * 加载仪表盘数据（统计卡片 + 雷达图 + 课程列表）
 */
const loadDashboard = async () => {
  loading.value = true
  try {
    const res = await getEvaluationDashboard()
    const data = res.data

    // 1. 统计卡片
    if (data.statCards) {
      statCards.value[0].value = data.statCards.avgScore ?? '--'
      statCards.value[1].value = data.statCards.evaluationCount ?? '--'
      statCards.value[2].value = data.statCards.goodRate != null ? data.statCards.goodRate + '%' : '--'
      statCards.value[3].value = data.statCards.courseCount ?? '--'
    }

    // 2. 课程下拉列表
    courseList.value = data.courseList || []

    // 3. 默认选中第一门课并加载评价
    if (courseList.value.length > 0) {
      selectedCourse.value = courseList.value[0].id
      await fetchEvaluations()
    }

    // 4. 雷达图
    if (data.radarData) {
      radarOption.value = buildRadarOption(data.radarData)
    }

  } catch (e) {
    console.error('加载仪表盘失败:', e)
  } finally {
    loading.value = false
  }
}

/**
 * 按课程查询评价列表
 */
const fetchEvaluations = async () => {
  if (!selectedCourse.value) return

  loading.value = true
  try {
    const res = await getEvaluationList({ courseId: selectedCourse.value })
    evaluationList.value = res.data || []
  } catch (e) {
    console.error('获取评价列表失败:', e)
  } finally {
    loading.value = false
  }
}

/**
 * 构建雷达图配置
 */
const buildRadarOption = (radarData) => {
  return {
    radar: {
      indicator: [
        { name: '授课能力', max: 5 },
        { name: '课堂氛围', max: 5 },
        { name: '知识讲解', max: 5 },
        { name: '作业批改', max: 5 },
        { name: '答疑服务', max: 5 }
      ],
      center: ['50%', '50%'],
      radius: '70%',
      shape: 'polygon',
      splitNumber: 4,
      axisName: {
        color: '#333',
        fontSize: 13
      }
    },
    series: [{
      type: 'radar',
      data: [{
        value: [
          radarData.teachingAbility || 0,
          radarData.classAtmosphere || 0,
          radarData.knowledgeClarity || 0,
          radarData.homeworkFeedback || 0,
          radarData.qaService || 0
        ],
        name: '评分',
        areaStyle: {
          color: 'rgba(64, 158, 255, 0.2)'
        },
        lineStyle: {
          color: '#409eff',
          width: 2
        },
        itemStyle: {
          color: '#409eff'
        }
      }]
    }]
  }
}

onMounted(() => {
  loadDashboard()
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

.evaluation-list {
  max-height: 400px;
  overflow-y: auto;
}

.evaluation-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
}

.evaluation-item:last-child {
  border-bottom: none;
}

.eval-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.eval-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.eval-content {
  color: var(--text-regular);
  line-height: 1.6;
  margin-bottom: 8px;
}

.eval-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>