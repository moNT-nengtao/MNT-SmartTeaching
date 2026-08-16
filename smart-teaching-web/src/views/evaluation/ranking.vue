<template>
  <div class="ranking-page">
    <SearchForm v-model="searchParams" :fields="searchFields" @search="fetchList" @reset="fetchList" />

    <div class="page-card">
      <div class="page-header">
        <span class="page-title">教师评分榜单</span>
      </div>

      <!-- 前三名展示 -->
      <div v-if="rankingList.length >= 3" class="top-three mb-20">
        <div class="top-item second">
          <div class="top-rank">2</div>
          <el-avatar :size="60">{{ rankingList[1]?.teacherName?.charAt(0) }}</el-avatar>
          <div class="top-name">{{ rankingList[1]?.teacherName }}</div>
          <div class="top-score">{{ rankingList[1]?.avgScore }} 分</div>
        </div>
        <div class="top-item first">
          <div class="top-rank"><el-icon :size="28"><Trophy /></el-icon></div>
          <el-avatar :size="72">{{ rankingList[0]?.teacherName?.charAt(0) }}</el-avatar>
          <div class="top-name">{{ rankingList[0]?.teacherName }}</div>
          <div class="top-score">{{ rankingList[0]?.avgScore }} 分</div>
        </div>
        <div class="top-item third">
          <div class="top-rank">3</div>
          <el-avatar :size="60">{{ rankingList[2]?.teacherName?.charAt(0) }}</el-avatar>
          <div class="top-name">{{ rankingList[2]?.teacherName }}</div>
          <div class="top-score">{{ rankingList[2]?.avgScore }} 分</div>
        </div>
      </div>

      <el-table :data="rankingList.slice(3)" v-loading="loading" border stripe>
        <el-table-column label="排名" width="80" align="center">
          <template #default="{ $index }">
            <el-tag>{{ $index + 4 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="教师姓名" width="120" />
        <el-table-column prop="collegeName" label="所属学院" width="160" />
        <el-table-column prop="courseCount" label="授课数" width="100" align="center" />
        <el-table-column prop="evaluationCount" label="评价数" width="100" align="center" />
        <el-table-column label="综合评分" width="200">
          <template #default="{ row }">
            <el-progress :percentage="row.avgScore * 20" :color="scoreColor(row.avgScore)" :stroke-width="14" />
            <span style="margin-left: 8px; font-weight: 600">{{ row.avgScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label="各维度" min-width="300">
          <template #default="{ row }">
            <el-tag v-for="dim in dimensions" :key="dim.key" size="small" class="mr-5">
              {{ dim.label }}: {{ row[dim.key] }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Trophy } from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import { getTeacherRanking } from '@/api/evaluation'

const loading = ref(false)
const rankingList = ref([])

const dimensions = [
  { key: 'teachingAbility', label: '授课' },
  { key: 'classAtmosphere', label: '氛围' },
  { key: 'knowledgeClarity', label: '讲解' },
  { key: 'homeworkFeedback', label: '批改' },
  { key: 'qaService', label: '答疑' }
]

const searchParams = reactive({ collegeId: '', subject: '' })
const searchFields = [
  { prop: 'collegeId', label: '学院', type: 'select', options: [] }
]

const scoreColor = (score) => {
  if (score >= 4.5) return '#67c23a'
  if (score >= 4.0) return '#409eff'
  if (score >= 3.5) return '#e6a23c'
  return '#f56c6c'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getTeacherRanking(searchParams)
    rankingList.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.ranking-page {
  padding: 20px;
}

.page-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.top-three {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 40px;
  padding: 30px 0;
}

.top-item {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.top-item.first {
  order: 2;
}

.top-item.second {
  order: 1;
}

.top-item.third {
  order: 3;
}

.top-rank {
  font-size: 24px;
  font-weight: 700;
  color: #909399;
}

.top-item.first .top-rank {
  color: #e6a23c;
}

.top-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.top-score {
  font-size: 20px;
  font-weight: 700;
  color: #409eff;
}

.mb-20 {
  margin-bottom: 20px;
}

.mr-5 {
  margin-right: 5px;
}

:deep(.el-avatar) {
  background: #f0f2f5;
  color: #409eff;
  font-weight: 600;
}

:deep(.el-table) {
  margin-top: 10px;
}
</style>