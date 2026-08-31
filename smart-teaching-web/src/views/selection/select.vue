<template>
  <div class="selection-page">
    <!-- 选课状态提示 -->
    <el-alert
      v-if="!isSelectionOpen"
      title="当前不在选课开放时间内"
      type="warning"
      :closable="false"
      class="mb-20"
    />

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 全部课程 -->
      <el-tab-pane label="全部课程" name="all">
        <SearchForm v-model="searchParams" :fields="searchFields" @search="fetchList" @reset="fetchList" />
        <el-row :gutter="20" v-loading="listLoading">
          <el-col :span="8" v-for="course in courseList" :key="course.courseId">
            <div class="course-card page-card" :class="{ hot: course.selectedCount >= course.maxStudents * 0.9 }">
              <div class="course-header">
                <span class="course-name">{{ course.courseName }}</span>
                <div class="tag-wrap">
                  <el-tag v-if="course.selectedCount >= course.maxStudents" type="danger" size="small">已满</el-tag>
                  <el-tag v-else-if="course.selectedCount >= course.maxStudents * 0.9" type="warning" size="small">热门</el-tag>
                </div>
              </div>
              <div class="course-info">
                <p><el-icon><User /></el-icon> 授课教师：{{ course.teacherName }}</p>
                <p><el-icon><Location /></el-icon> 上课时间：{{ course.scheduleTime || '--' }}</p>
                <p><el-icon><School /></el-icon> 学分：{{ course.credit }}</p>
                <p>
                  <el-icon><Tickets /></el-icon>
                  剩余名额：
                  <span :class="{ 'text-danger': course.remaining <= 5 }">
                    {{ course.remaining }} / {{ course.maxStudents }}
                  </span>
                </p>
              </div>
              <div class="course-footer">
                <div class="rate-wrap">
                  <el-rate
                    v-if="course.avgScore != null"
                    :model-value="Number(course.avgScore).toFixed(1)"
                    disabled
                    show-score
                    text-color="#ff9900"
                    score-template="{value}"
                    :max="5"
                  />
                  <span v-else class="no-rate-text">暂无评价</span>
                </div>
                <el-button
                  v-if="!course.isSelected"
                  type="primary"
                  size="small"
                  :disabled="!isSelectionOpen || course.remaining <= 0 || actionLoading"
                  @click="handleSelect(course)"
                >
                  选课
                </el-button>
                <el-button v-else type="danger" size="small" :disabled="actionLoading" @click="handleDrop(course)">退课</el-button>
              </div>
            </div>
          </el-col>
        </el-row>
        <Pagination v-model:page="searchParams.page" v-model:page-size="searchParams.pageSize" :total="total" @change="fetchList" />
      </el-tab-pane>

      <!-- 智能推荐 -->
      <el-tab-pane label="智能推荐" name="recommend">
        <el-row :gutter="20" v-loading="recommendLoading">
          <el-col :span="8" v-for="course in recommendList" :key="course.courseId">
            <div class="course-card page-card recommend-card">
              <el-tag type="success" size="small" class="recommend-tag">智能推荐</el-tag>
              <div class="course-header">
                <span class="course-name">{{ course.courseName }}</span>
              </div>
              <div class="course-info">
                <p>推荐理由：{{ course.reason || '综合匹配你的选课偏好' }}</p>
                <p>授课教师：{{ course.teacherName }}</p>
                <p>剩余名额：{{ course.remaining }} / {{ course.maxStudents }}</p>
              </div>
              <div class="recommend-footer">
                <el-button
                  v-if="!course.isSelected"
                  type="primary"
                  size="small"
                  :disabled="!isSelectionOpen || course.remaining <= 0 || actionLoading"
                  @click="handleSelect(course)"
                >
                  选课
                </el-button>
                <el-button v-else type="danger" size="small" :disabled="actionLoading" @click="handleDrop(course)">退课</el-button>
              </div>
            </div>
          </el-col>
        </el-row>
        <el-empty v-if="recommendList.length === 0 && !recommendLoading" description="暂无推荐课程" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Location, School, Tickets } from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { getSelectionCourseList, getRecommendCourses, selectCourse, dropCourse, getSelectionConfig } from '@/api/selection'

const activeTab = ref('all')
const isSelectionOpen = ref(true)
const courseList = ref([])
const recommendList = ref([])
const total = ref(0)

// ========== 新增加载状态 ==========
const listLoading = ref(false)
const recommendLoading = ref(false)
const actionLoading = ref(false)

const searchParams = reactive({ page: 1, pageSize: 10, keyword: '', collegeId: '' })
const searchFields = [
  { prop: 'keyword', label: '课程名', type: 'input' }
]

const handleTabChange = async (tabName) => {
  if (tabName === 'recommend') {
    await fetchRecommend()
  }
}

const fetchConfig = async () => {
  try {
    const res = await getSelectionConfig()
    const now = Date.now()
    const startTime = new Date(res.data.startTime).getTime()
    const endTime = new Date(res.data.endTime).getTime()
    isSelectionOpen.value = startTime <= now && now <= endTime
  } catch (e) {
    console.error('获取选课配置失败', e)
  }
}

const fetchList = async () => {
  listLoading.value = true
  try {
    const res = await getSelectionCourseList(searchParams)
    const list = res.data?.list ?? res.data?.records ?? []
    courseList.value = list
    total.value = res.data?.total ?? 0
  } catch (e) {
    console.error('获取课程列表失败', e)
  } finally {
    listLoading.value = false
  }
}

const fetchRecommend = async () => {
  recommendLoading.value = true
  try {
    const res = await getRecommendCourses()
    recommendList.value = res.data || []
  } catch (e) {
    console.error('获取推荐课程失败', e)
  } finally {
    recommendLoading.value = false
  }
}

const handleSelect = (course) => {
  ElMessageBox.confirm(`确定选择课程「${course.courseName}」吗？`, '选课确认', { type: 'info' })
    .then(async () => {
      actionLoading.value = true
      try {
        await selectCourse(course.courseId)
        ElMessage.success('选课成功')
        await fetchList()
        if (activeTab.value === 'recommend') await fetchRecommend()
      } finally {
        actionLoading.value = false
      }
    })
    .catch(() => {})
}

const handleDrop = (course) => {
  ElMessageBox.confirm(`确定退选课程「${course.courseName}」吗？`, '退课确认', { type: 'warning' })
    .then(async () => {
      actionLoading.value = true
      try {
        await dropCourse(course.courseId)
        ElMessage.success('退课成功')
        await fetchList()
        if (activeTab.value === 'recommend') await fetchRecommend()
      } finally {
        actionLoading.value = false
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchConfig()
  fetchList()
  fetchRecommend()
})
</script>

<style scoped>
.course-card {
  margin-bottom: 20px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.course-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.course-card.hot {
  border-left: 3px solid var(--warning-color);
}

.course-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.course-name {
  font-size: 16px;
  font-weight: 600;
}

.tag-wrap {
  display: flex;
  gap: 4px;
}

.course-info p {
  margin: 6px 0;
  color: var(--text-regular);
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.course-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}

.rate-wrap {
  display: flex;
  align-items: center;
}

.no-rate-text {
  font-size: 13px;
  color: #909399;
}

.text-danger {
  color: var(--danger-color);
  font-weight: 600;
}

.recommend-card {
  position: relative;
  border-left: 3px solid var(--success-color);
  padding-top: 32px;
}

.recommend-tag {
  position: absolute;
  top: 12px;
  left: 12px;
}

.recommend-footer {
  margin-top: 12px;
}
</style>
