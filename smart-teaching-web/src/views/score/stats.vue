<template>
  <div class="score-stats-page">
    <SearchForm v-model="searchParams" :fields="searchFields" @search="handleSearch" @reset="handleReset" />
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
        <ChartCard title="成绩分布" :option="pieOption" :loading="loading" :height="320" />
      </el-col>
      <el-col :span="12">
        <ChartCard title="各学院平均分对比" :option="barOption" :loading="loading" :height="320" />
      </el-col>
    </el-row>
    <div class="page-card mt-20">
      <div class="page-header">
        <span class="page-title">异常成绩筛查</span>
        <el-button type="primary" :icon="Download" @click="handleExport">导出成绩</el-button>
      </div>
      <el-table :data="abnormalList" v-loading="loading" border stripe>
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column prop="courseName" label="课程" min-width="140" />
        <el-table-column prop="totalScore" label="成绩" width="100">
          <template #default="{ row }">
            <el-tag type="danger">{{ row.totalScore }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="异常原因" />
      </el-table>
      <!-- 分页组件 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import SearchForm from '@/components/SearchForm.vue'
import ChartCard from '@/components/ChartCard.vue'
import { getScoreStats, getAbnormalScores, exportScore } from '@/api/score'
import { getCollegeList } from '@/api/org'  // 导入学院列表接口
const loading = ref(false)
const abnormalList = ref([])
const pieOption = ref({})
const barOption = ref({})
// 统计卡片
const statCards = ref([
  { label: '总人数', value: '--', color: '#409eff' },
  { label: '平均分', value: '--', color: '#67c23a' },
  { label: '及格率', value: '--', color: '#e6a23c' },
  { label: '挂科人数', value: '--', color: '#f56c6c' }
])
// 学院列表
const collegeOptions = ref([])
// ==================== 学期工具函数 ====================
/**
 * 获取当前学期编号
 * 2-7月：春季学期（2），8-12月：秋季学期（1），1月：春季学期（2）
 */
const getCurrentSemesterNumber = (month) => {
  if (month >= 8 && month <= 12) return 1
  return 2
}
/**
 * 获取当前学期字符串
 */
const getCurrentSemester = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const sem = getCurrentSemesterNumber(month)
  return `${year}-${year + 1}-${sem}`
}
/**
 * 生成学期选项列表
 * @param {number} startYear - 起始年份，默认2020
 * @param {number} futureCount - 未来学期数量，默认2
 */
const generateSemesterOptions = (startYear = 2020, futureCount = 2) => {
  const options = []
  const now = new Date()
  const currentYear = now.getFullYear()
  const currentMonth = now.getMonth() + 1
  const currentSem = getCurrentSemesterNumber(currentMonth)
  // 计算结束年份和学期
  let endYear = currentYear
  let endSem = currentSem + futureCount
  while (endSem > 2) {
    endSem -= 2
    endYear += 1
  }
  // 生成所有学期
  for (let year = startYear; year <= endYear; year++) {
    for (let sem = 1; sem <= 2; sem++) {
      // 跳过未来超出范围的学期
      if (year === endYear && sem > endSem) continue
      if (year > endYear) continue
      const label = `${year}-${year + 1}-${sem}`
      options.push({ label, value: label })
    }
  }
  // 按时间倒序（最新的在前）
  return options.reverse()
}
// ==================== 搜索参数 ====================
const searchParams = reactive({
  semester: getCurrentSemester(),
  collegeId: ''
})
// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
// 学期选项（从2020年开始，往后延续2个学期）
const semesterOptions = generateSemesterOptions(2020, 2)
// 搜索表单配置
const searchFields = computed(() => [
  {
    prop: 'semester',
    label: '学期',
    type: 'select',
    options: [
      { label: '全部', value: '' },
      ...semesterOptions
    ]
  },
  {
    prop: 'collegeId',
    label: '学院',
    type: 'select',
    options: [
      { label: '全部', value: '' },
      ...collegeOptions.value
    ]
  }
])
// ==================== 获取学院列表 ====================
const fetchCollegeList = async () => {
  try {
    const res = await getCollegeList()
    if (res.data && Array.isArray(res.data)) {
      collegeOptions.value = res.data.map(item => ({
        label: item.name,
        value: item.id
      }))
    }
  } catch (error) {
    console.error('获取学院列表失败:', error)
    // 接口失败时，使用空列表，用户仍可选择"全部"
  }
}
// ==================== 数据获取 ====================
const fetchData = async () => {
  loading.value = true
  try {
    // 构建异常成绩查询参数（包含分页）
    const abnormalParams = {
      semester: searchParams.semester,
      collegeId: searchParams.collegeId,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    // 并发请求
    const [statsRes, abnormalRes] = await Promise.all([
      getScoreStats({
        semester: searchParams.semester,
        collegeId: searchParams.collegeId
      }),
      getAbnormalScores(abnormalParams)
    ])
    // 更新统计数据
    if (statsRes.data) {
      // 注意：全部从 statCards 子对象取！！
      const card = statsRes.data.statCards
      statCards.value = [
        { label: '总人数', value: card?.totalStudents ?? '--', color: '#409eff' },
        { label: '平均分', value: card?.avgScore ?? '--', color: '#67c23a' },
        { label: '及格率', value: card?.passRate ? card.passRate + '%' : '--', color: '#e6a23c' },
        { label: '挂科人数', value: card?.failCount ?? '--', color: '#f56c6c' }
      ]
      // ========== 组装饼图 option ==========
      const pie = statsRes.data.pieData
      if (pie && pie.chartData) {
        pieOption.value = {
          tooltip: { trigger: 'item' },
          legend: { bottom: 5 },
          series: [
            {
              type: 'pie',
              radius: '60%',
              data: pie.chartData
            }
          ]
        }
      }
      // ========== 组装柱状图 option ==========
      const bar = statsRes.data.barData
      if (bar) {
        barOption.value = {
          tooltip: { trigger: 'axis' },
          xAxis: {
            type: 'category',
            data: bar.colleges
          },
          yAxis: { type: 'value' },
          series: [
            {
              name: '平均分',
              type: 'bar',
              data: bar.avgScores
            }
          ]
        }
      }
    }
    // 更新异常成绩列表
    if (abnormalRes.data) {
      abnormalList.value = abnormalRes.data.list || []
      pagination.total = abnormalRes.data.total || 0
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
// ==================== 事件处理 ====================
const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}
const handleReset = () => {
  pagination.pageNum = 1
  fetchData()
}
const handleCurrentChange = (val) => {
  pagination.pageNum = val
  fetchData()
}
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNum = 1
  fetchData()
}

// ==========【修复后的导出函数】==========
const handleExport = async () => {
  try {
    const blobRes = await exportScore({
      semester: searchParams.semester,
      collegeId: searchParams.collegeId
    })
    const blob = new Blob([blobRes])
    const reader = new FileReader()
    reader.onload = (e) => {
      const text = e.target.result
      try {
        // 判断是否是后端返回的JSON提示(无数据/错误)
        const jsonObj = JSON.parse(text)
        ElMessage.warning(jsonObj.msg)
      } catch {
        // JSON解析失败，代表是Excel二进制，执行下载
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '异常成绩导出.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        ElMessage.success('导出成功')
      }
    }
    reader.readAsText(blob)
  } catch (err) {
    console.error('导出异常：', err)
    ElMessage.error('导出请求发生错误')
  }
}

// ==================== 生命周期 ====================
onMounted(() => {
  // 并行获取学院列表和统计数据
  Promise.all([
    fetchCollegeList(),
    fetchData()
  ])
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
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
