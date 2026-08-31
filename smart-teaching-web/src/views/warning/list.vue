<template>
  <div class="warning-page">
    <SearchForm v-model="searchParams" :fields="searchFields" @search="fetchList" @reset="fetchList" />

    <el-row :gutter="20" class="mb-20" v-if="!isStudent">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <div class="stat-card page-card text-center">
          <el-icon :size="28" :color="card.color"><component :is="card.icon" /></el-icon>
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="page-card">
      <div class="page-header">
        <span class="page-title">{{ isStudent ? '我的学业预警' : '学业预警列表' }}</span>
        <el-button v-if="!isStudent" type="primary" :icon="Download" @click="handleExport">导出预警报告</el-button>
      </div>
      <el-table :data="warningList" v-loading="loading" border stripe @row-click="goToDetail">
        <el-table-column prop="title" label="预警标题" min-width="160" show-overflow-tooltip />
        <el-table-column v-if="!isStudent" prop="studentName" label="学生姓名" width="100" />
        <el-table-column v-if="!isStudent" prop="studentNo" label="学号" width="120" />
        <el-table-column v-if="!isStudent" prop="className" label="班级" width="120" />
        <el-table-column label="预警等级" width="120" align="center">
          <template #default="{ row }">
            <span class="level-badge" :class="levelClass(row.level)">
              {{ levelText(row.level) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="预警类型" width="120" align="center">
          <template #default="{ row }">
            <span class="type-badge" :class="typeClass(row.warningType)">
              {{ typeText(row.warningType) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" :label="isStudent ? '预警原因（含涉及科目）' : '预警原因'" :min-width="isStudent ? 280 : 200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="生成时间" width="170" align="center" />
        <el-table-column v-if="!isStudent" label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="goToDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination v-model:page="searchParams.pageNum" v-model:page-size="searchParams.pageSize" :total="total" @change="fetchList" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { getWarningList, getWarningStats, exportWarningReport } from '@/api/warning'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const isStudent = computed(() => userStore.role === 'student')

const loading = ref(false)
const warningList = ref([])
const total = ref(0)

const statCards = ref([
  { label: '旷课预警', value: '--', color: '#f56c6c', icon: 'Warning' },
  { label: '挂科预警', value: '--', color: '#e6a23c', icon: 'DocumentDelete' },
  { label: '作业未交', value: '--', color: '#409eff', icon: 'Document' },
  { label: '总预警数', value: '--', color: '#909399', icon: 'DataAnalysis' }
])

const searchParams = reactive({
  pageNum: 1,
  pageSize: 10,
  level: '',
  type: ''
})

const searchFields = [
  {
    prop: 'level',
    label: '预警等级',
    type: 'select',
    options: [
      { label: '全部', value: '' },
      { label: '严重', value: 'high' },
      { label: '中等', value: 'medium' },
      { label: '轻微', value: 'low' }
    ]
  },
  {
    prop: 'type',
    label: '预警类型',
    type: 'select',
    options: [
      { label: '全部', value: '' },
      { label: '旷课', value: 'absent' },
      { label: '挂科', value: 'score' },
      { label: '作业未提交', value: 'homework' }
    ]
  }
]

// 等级映射
const levelText = (level) => {
  const map = { high: '严重', medium: '中等', low: '轻微' }
  return map[level] || level
}

const levelClass = (level) => {
  const map = { high: 'level-high', medium: 'level-medium', low: 'level-low' }
  return map[level] || 'level-low'
}

// 类型映射
const typeText = (type) => {
  const map = { 
    absent: '旷课', 
    score: '挂科', 
    homework: '作业未交',
    score_warning: '成绩预警',
    attendance_warning: '考勤预警'
  }
  return map[type] || type
}

const typeClass = (type) => {
  const map = { 
    absent: 'type-absent', 
    score: 'type-score', 
    homework: 'type-homework',
    score_warning: 'type-score',
    attendance_warning: 'type-absent'
  }
  return map[type] || 'type-other'
}

// 获取预警列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await getWarningList(searchParams)
    warningList.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } catch (error) {
    console.error('获取预警列表失败', error)
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const res = await getWarningStats()
    const data = res.data
    if (data) {
      statCards.value[0].value = data.absentCount ?? 0
      statCards.value[1].value = data.failCount ?? 0
      statCards.value[2].value = data.homeworkCount ?? 0
      statCards.value[3].value = data.totalCount ?? 0
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 跳转详情
const goToDetail = (row) => {
  router.push({ path: '/warning/report', query: { id: row.id } })
}

// 导出 Excel
const handleExport = async () => {
  try {
    const res = await exportWarningReport({
      level: searchParams.level,
      type: searchParams.type
    })

    const blob = new Blob([res])
    const reader = new FileReader()
    reader.onload = (e) => {
      const text = e.target.result
      try {
        const json = JSON.parse(text)
        ElMessage.warning(json.msg || '导出失败')
      } catch {
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `学业预警报告_${new Date().getTime()}.xlsx`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        ElMessage.success('导出成功')
      }
    }
    reader.readAsText(blob)
  } catch (error) {
    console.error('导出失败', error)
    ElMessage.error('导出失败，请稍后重试')
  }
}

onMounted(() => {
  fetchList()
  fetchStats()
})
</script>

<style scoped>
/* 统计卡片样式 */
.stat-value {
  font-size: 28px;
  font-weight: 700;
  margin: 10px 0 6px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

/* ===== 预警等级样式 ===== */
.level-badge {
  display: inline-block;
  padding: 2px 14px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

/* 严重 - 深红 */
.level-high {
  color: #fff;
  background: linear-gradient(135deg, #f56c6c 0%, #c0392b 100%);
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.4);
}

/* 中等 - 橙黄 */
.level-medium {
  color: #fff;
  background: linear-gradient(135deg, #f0ad4e 0%, #d68910 100%);
  box-shadow: 0 2px 8px rgba(240, 173, 78, 0.4);
}

/* 轻微 - 浅蓝 */
.level-low {
  color: #fff;
  background: linear-gradient(135deg, #85c1e9 0%, #2e86c1 100%);
  box-shadow: 0 2px 8px rgba(52, 152, 219, 0.35);
}

/* ===== 预警类型样式 ===== */
.type-badge {
  display: inline-block;
  padding: 2px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

/* 旷课 - 红色系 */
.type-absent {
  color: #c0392b;
  background: #fde8e8;
  border: 1px solid #f5c8c8;
}

/* 挂科 - 橙色系 */
.type-score {
  color: #b7950b;
  background: #fef9e7;
  border: 1px solid #f9e79f;
}

/* 作业未交 - 蓝色系 */
.type-homework {
  color: #1a5276;
  background: #eaf2f8;
  border: 1px solid #aed6f1;
}

/* 其他类型 - 灰色系 */
.type-other {
  color: #5d6d7e;
  background: #f0f3f4;
  border: 1px solid #d5dbdb;
}

/* ===== 表格行悬停效果 ===== */
:deep(.el-table__row) {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

:deep(.el-table__row:hover) {
  background-color: #f8f9fa !important;
}

/* ===== 表格列头样式 ===== */
:deep(.el-table th.el-table__cell) {
  background-color: #f5f7fa !important;
  color: #2c3e50;
  font-weight: 600;
}

/* ===== 统计卡片悬浮效果 ===== */
.stat-card {
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}
</style>