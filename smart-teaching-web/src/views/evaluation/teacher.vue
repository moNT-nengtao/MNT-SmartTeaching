<template>
  <div class="evaluation-teacher-page">
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
        <ChartCard title="各维度评分" :option="radarOption" :loading="loading" :height="320" />
      </el-col>
      <el-col :span="12">
        <div class="page-card" style="height: 100%">
          <div class="page-header">
            <span class="page-title">课程评价列表</span>
          </div>
          <el-select v-model="selectedCourse" filterable style="width: 100%; margin-bottom: 16px" @change="fetchEvaluations">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
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
import { getEvaluationList, getEvaluationStats } from '@/api/evaluation'

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

const fetchEvaluations = async () => {
  if (!selectedCourse.value) return
  loading.value = true
  try {
    const [listRes, statsRes] = await Promise.all([
      getEvaluationList({ courseId: selectedCourse.value }),
      getEvaluationStats(selectedCourse.value)
    ])
    evaluationList.value = listRes.data || []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // TODO: 获取教师授课课程列表
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
