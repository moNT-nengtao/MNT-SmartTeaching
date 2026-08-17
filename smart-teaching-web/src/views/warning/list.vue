<template>
  <div class="warning-page">
    <SearchForm v-model="searchParams" :fields="searchFields" @search="fetchList" @reset="fetchList" />

    <el-row :gutter="20" class="mb-20">
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
        <span class="page-title">学业预警列表</span>
        <el-button type="primary" :icon="Download" @click="handleExport">导出预警报告</el-button>
      </div>
      <el-table :data="warningList" v-loading="loading" border stripe @row-click="goToDetail">
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column label="预警等级" width="100">
          <template #default="{ row }">
            <el-tag :type="levelType(row.level)" effect="dark">{{ levelText(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预警类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="预警原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="生成时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="goToDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination v-model:page="searchParams.page" v-model:page-size="searchParams.pageSize" :total="total" @change="fetchList" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Download } from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { getWarningList, exportWarningReport } from '@/api/warning'

const router = useRouter()
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
  page: 1,
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
      { label: '挂科', value: 'fail' },
      { label: '作业未提交', value: 'homework' }
    ]
  }
]

const levelText = (level) => ({ high: '严重', medium: '中等', low: '轻微' }[level] || level)
const levelType = (level) => ({ high: 'danger', medium: 'warning', low: 'info' }[level] || 'info')

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getWarningList(searchParams)
    warningList.value = res.data?.list ?? res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } finally {
    loading.value = false
  }
}

const goToDetail = (row) => {
  router.push({ path: '/warning/report', query: { id: row.id } })
}

const handleExport = () => {
  exportWarningReport(searchParams)
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.stat-value {
  font-size: 28px;
  font-weight: 700;
  margin: 10px 0 6px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
