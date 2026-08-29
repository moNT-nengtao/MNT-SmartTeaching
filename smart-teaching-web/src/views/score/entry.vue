<template>
  <div class="score-entry-page">
    <div class="page-card mb-20">
      <el-form :model="filterForm" inline>
        <el-form-item label="选择课程">
          <el-select
            v-model="filterForm.courseId"
            filterable
            style="width: 280px"
            @change="handleCourseChange"
          >
            <el-option
              v-for="c in courseList"
              :key="c.id"
              :label="`${c.name} (${c.semester})`"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="success" :icon="Download" @click="handleExportScore" :disabled="!filterForm.courseId || scoreList.length === 0">
            导出成绩
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="page-card" v-if="filterForm.courseId">
      <div class="page-header">
        <div class="header-left">
          <span class="page-title">成绩录入</span>
          <el-tag v-if="hasUnsavedChange" type="warning" effect="plain">存在未保存修改</el-tag>
        </div>
        <div class="header-right">
          <el-button @click="handleResetScore" :disabled="!hasUnsavedChange">重置</el-button>
          <el-button type="primary" @click="handleSaveAll" :disabled="!hasUnsavedChange">保存全部</el-button>
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="stats-bar" v-if="scoreList.length > 0">
        <el-tag type="info">总人数：{{ scoreList.length }}</el-tag>
        <el-tag type="success">平均分：{{ avgScore }}</el-tag>
        <el-tag type="warning">及格人数：{{ passCount }}</el-tag>
        <el-tag type="danger">不及格人数：{{ failCount }}</el-tag>
      </div>

      <el-table
        :data="scoreList"
        v-loading="loading"
        border
        stripe
        style="margin-top:12px"
        max-height="600"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column prop="className" label="班级" width="120" />

        <el-table-column label="平时成绩" width="160">
          <template #default="{ row }">
            <el-input-number
              v-model="row.usualScore"
              :min="0"
              :max="100"
              size="small"
              controls-position="right"
              @change="markModified"
              placeholder="请输入0-100"
            />
          </template>
        </el-table-column>

        <el-table-column label="期末成绩" width="160">
          <template #default="{ row }">
            <el-input-number
              v-model="row.finalScore"
              :min="0"
              :max="100"
              size="small"
              controls-position="right"
              @change="markModified"
              placeholder="请输入0-100"
            />
          </template>
        </el-table-column>

        <el-table-column label="综合成绩" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="scoreTagType(calcTotal(row))" size="large">
              {{ calcTotal(row) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="等级" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="scoreTagType(calcTotal(row))" size="small">
              {{ scoreToLevel(calcTotal(row)) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="calcTotal(row) >= 60 ? 'success' : 'danger'" size="small">
              {{ calcTotal(row) >= 60 ? '及格' : '不及格' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="备注" min-width="150">
          <template #default="{ row }">
            <el-input
              v-model="row.remark"
              size="small"
              placeholder="请输入备注"
              @input="markModified"
              clearable
            />
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="scoreList.length === 0 && !loading" description="暂无学生数据" />
    </div>

    <el-empty v-else description="请先选择授课课程" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { getCourseScoreList, enterScore, exportCourseScore } from '@/api/score' 
import { getCourseList } from '@/api/course'
import { useUserStore } from '@/store/user'

const loading = ref(false)
const courseList = ref([])
const scoreList = ref([])
const filterForm = reactive({ courseId: null })
const hasUnsavedChange = ref(false)
const userStore = useUserStore()

// 计算统计信息
const avgScore = computed(() => {
  if (scoreList.value.length === 0) return '--'
  const total = scoreList.value.reduce((sum, row) => sum + calcTotal(row), 0)
  return (total / scoreList.value.length).toFixed(1)
})

const passCount = computed(() => {
  return scoreList.value.filter(row => calcTotal(row) >= 60).length
})

const failCount = computed(() => {
  return scoreList.value.filter(row => calcTotal(row) < 60).length
})

/**
 * 计算综合成绩：平时30% + 期末70%
 */
const calcTotal = (row) => {
  const usual = Number(row.usualScore ?? 0)
  const final = Number(row.finalScore ?? 0)
  return Math.round(usual * 0.3 + final * 0.7)
}

const scoreTagType = (score) => {
  if (score >= 90) return 'success'
  if (score >= 60) return ''
  return 'danger'
}

const scoreToLevel = (score) => {
  if (score >= 90) return '优秀'
  if (score >= 80) return '良好'
  if (score >= 70) return '中等'
  if (score >= 60) return '及格'
  return '不及格'
}

// 标记页面发生修改
const markModified = () => {
  hasUnsavedChange.value = true
}

// 重置修改
const handleResetScore = async () => {
  try {
    await ElMessageBox.confirm('确认重置所有修改？', '提示', {
      confirmButtonText: '确认重置',
      cancelButtonText: '取消',
      type: 'warning'
    })
    hasUnsavedChange.value = false
    await fetchScoreList()
    ElMessage.success('已重置')
  } catch {
    // 用户取消
  }
}

// 课程切换，重置修改标记
const handleCourseChange = async () => {
  if (hasUnsavedChange.value) {
    try {
      await ElMessageBox.confirm('当前有未保存的修改，切换课程将丢失修改，确定继续？', '提示', {
        confirmButtonText: '确定切换',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      // 用户取消，恢复原课程
      return
    }
  }
  hasUnsavedChange.value = false
  await fetchScoreList()
}

// 获取当前教师授课课程列表
const fetchTeacherCourses = async () => {
  try {
    const teacherId = userStore.userInfo?.id
    if (!teacherId) {
      ElMessage.warning('未获取到教师信息')
      return
    }
    const res = await getCourseList({
      teacherId: teacherId,
      status: 1,
      pageNum: 1,
      pageSize: 999
    })
    courseList.value = res.data?.records || []
    if (courseList.value.length > 0) {
      filterForm.courseId = courseList.value[0].id
      await fetchScoreList()
    }
  } catch (error) {
    ElMessage.error('获取课程列表失败')
    console.error('获取课程列表失败:', error)
  }
}

// 获取课程学生成绩列表
const fetchScoreList = async () => {
  if (!filterForm.courseId) {
    scoreList.value = []
    return
  }
  loading.value = true
  try {
    const res = await getCourseScoreList(filterForm.courseId)
    // 确保每条记录都有 remark 字段
    scoreList.value = (res.data || []).map(row => ({
      ...row,
      remark: row.remark || ''
    }))
  } catch (error) {
    ElMessage.error('获取成绩列表失败')
    console.error('获取成绩列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 保存全部成绩
const handleSaveAll = async () => {
  if (!filterForm.courseId) {
    ElMessage.warning('请先选择课程')
    return
  }

  // 检查是否有成绩为空的学生
  const emptyStudents = scoreList.value.filter(
    row => (row.usualScore === null || row.usualScore === undefined || row.usualScore === '') ||
           (row.finalScore === null || row.finalScore === undefined || row.finalScore === '')
  )

  if (emptyStudents.length > 0) {
    try {
      await ElMessageBox.confirm(
        `有 ${emptyStudents.length} 名学生的成绩未录入完整，确定要保存吗？`,
        '提示',
        {
          confirmButtonText: '确定保存',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch {
      return
    }
  }

  const scores = scoreList.value.map((row) => ({
    studentId: row.studentId,
    usualScore: row.usualScore ?? null,
    finalScore: row.finalScore ?? null,
    totalScore: calcTotal(row),
    remark: row.remark || null
  }))

  const validScores = scores.filter(s => s.studentId)
  if (validScores.length === 0) {
    ElMessage.warning('没有可保存的学生成绩数据')
    return
  }

  try {
    await enterScore({
      courseId: filterForm.courseId,
      scores: validScores
    })
    ElMessage.success(`成功保存 ${validScores.length} 条成绩`)
    hasUnsavedChange.value = false
    await fetchScoreList()
  } catch (error) {
    ElMessage.error('保存成绩失败，请稍后重试')
    console.error(error)
  }
}

// 导出成绩
const handleExportScore = async () => {
  if (!filterForm.courseId) {
    ElMessage.warning('请先选择课程')
    return
  }

  if (scoreList.value.length === 0) {
    ElMessage.warning('当前课程暂无成绩数据可导出')
    return
  }

  try {
    const blobRes = await exportCourseScore(filterForm.courseId)

    const blob = new Blob([blobRes])
    const reader = new FileReader()
    reader.onload = (e) => {
      const text = e.target.result
      try {
        const jsonObj = JSON.parse(text)
        ElMessage.warning(jsonObj.msg || '导出失败，请重试')
      } catch {
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        const course = courseList.value.find(c => c.id === filterForm.courseId)
        const fileName = `${course?.name || '成绩'}_${new Date().toLocaleDateString()}.xlsx`
        link.href = url
        link.download = fileName
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

onMounted(() => {
  fetchTeacherCourses()
})

</script>

<style scoped>
.score-entry-page {
  padding: 16px;
}
.page-card {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}
.mb-20 {
  margin-bottom: 20px;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-right {
  display: flex;
  gap: 10px;
}
.page-title {
  font-size: 16px;
  font-weight: 600;
}
.stats-bar {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  flex-wrap: wrap;
}
</style>