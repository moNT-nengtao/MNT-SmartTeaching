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

    <!-- 退课状态提示 -->
    <el-alert
      v-if="!isDropOpen"
      title="当前不在退课开放时间内，无法退课"
      type="info"
      :closable="false"
      class="mb-20"
    />

    <el-tabs v-model="activeTab">
      <!-- 全部课程 -->
      <el-tab-pane label="全部课程" name="all">
        <SearchForm v-model="searchParams" :fields="searchFields" @search="fetchList" @reset="fetchList" />
        <el-row :gutter="20">
          <el-col :span="8" v-for="course in courseList" :key="course.id">
            <div class="course-card page-card" :class="{ hot: course.selectedCount >= course.maxStudents * 0.9 }">
              <div class="course-header">
                <span class="course-name">{{ course.courseName }}</span>
                <el-tag v-if="course.selectedCount >= course.maxStudents" type="danger" size="small">已满</el-tag>
                <el-tag v-else-if="course.selectedCount >= course.maxStudents * 0.9" type="warning" size="small">热门</el-tag>
              </div>
              <div class="course-info">
                <p><el-icon><User /></el-icon> 授课教师：{{ course.teacherName }}</p>
                <p><el-icon><Location /></el-icon> 上课时间：{{ course.scheduleTime }}</p>
                <p><el-icon><School /></el-icon> 学分：{{ course.credit }} | 学时：{{ course.hours }}</p>
                <p>
                  <el-icon><Tickets /></el-icon>
                  剩余名额：
                  <span :class="{ 'text-danger': course.remaining <= 5 }">
                    {{ course.remaining }} / {{ course.maxStudents }}
                  </span>
                </p>
              </div>
              <div class="course-footer">
                <el-rate v-model="course.rating" disabled show-score text-color="#ff9900" score-template="{value}" :max="5" />
                <div class="footer-buttons">
                  <el-button
                    v-if="!course.isSelected"
                    type="primary"
                    size="small"
                    :disabled="!isSelectionOpen || course.remaining <= 0"
                    @click="handleSelect(course)"
                  >
                    选课
                  </el-button>
                  <el-button
                    v-else
                    type="danger"
                    size="small"
                    :disabled="!isDropOpen"
                    @click="handleDrop(course)"
                  >
                    退课
                  </el-button>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
        <Pagination v-model:page="searchParams.page" v-model:page-size="searchParams.pageSize" :total="total" @change="fetchList" />
      </el-tab-pane>

      <!-- 智能推荐 -->
      <el-tab-pane label="智能推荐" name="recommend">
        <el-row :gutter="20">
          <el-col :span="8" v-for="course in recommendList" :key="course.id">
            <div class="course-card page-card recommend-card">
              <el-tag type="success" size="small" class="recommend-tag">智能推荐</el-tag>
              <div class="course-header">
                <span class="course-name">{{ course.courseName }}</span>
              </div>
              <div class="course-info">
                <p>推荐理由：{{ course.reason }}</p>
                <p>授课教师：{{ course.teacherName }}</p>
                <p>剩余名额：{{ course.remaining }} / {{ course.maxStudents }}</p>
              </div>
              <el-button
                type="primary"
                size="small"
                :disabled="!isSelectionOpen || course.remaining <= 0"
                @click="handleSelect(course)"
              >
                选课
              </el-button>
            </div>
          </el-col>
        </el-row>
        <el-empty v-if="recommendList.length === 0" description="暂无推荐课程" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Location, School, Tickets } from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { getSelectionCourseList, getRecommendCourses, selectCourse, dropCourse, getSelectionConfig } from '@/api/selection'

const activeTab = ref('all')
const isSelectionOpen = ref(true)
const isDropOpen = ref(true)
const courseList = ref([])
const recommendList = ref([])
const total = ref(0)

const searchParams = reactive({ page: 1, pageSize: 12, keyword: '', collegeId: '' })
const searchFields = [
  { prop: 'keyword', label: '课程名', type: 'input' }
]

const fetchConfig = async () => {
  try {
    const res = await getSelectionConfig()
    const now = new Date()
    const startTime = new Date(res.data.startTime)
    const endTime = new Date(res.data.endTime)
    
    // 选课时间范围
    isSelectionOpen.value = startTime <= now && now <= endTime
    
    // 退课时间范围（可以单独配置，或者使用选课时间范围）
    // 方案一：退课使用选课时间范围
    isDropOpen.value = startTime <= now && now <= endTime
    
    // 方案二：退课时间范围更长（例如选课结束后的3天内仍可退课）
    // const dropEndTime = new Date(endTime)
    // dropEndTime.setDate(dropEndTime.getDate() + 3)
    // isDropOpen.value = startTime <= now && now <= dropEndTime
    
    // 方案三：如果后端返回了退课时间范围
    // isDropOpen.value = new Date(res.data.dropStartTime) <= now && now <= new Date(res.data.dropEndTime)
  } catch (e) {
    console.error('获取选课配置失败', e)
  }
}

const fetchList = async () => {
  try {
    const res = await getSelectionCourseList(searchParams)
    const list = res.data?.list ?? res.data?.records ?? []
    courseList.value = list.map((c) => ({
      ...c,
      remaining: c.maxStudents - c.selectedCount,
      rating: c.avgScore || 0
    }))
    total.value = res.data?.total ?? 0
  } catch (e) {
    console.error('获取课程列表失败', e)
  }
}

const fetchRecommend = async () => {
  try {
    const res = await getRecommendCourses()
    recommendList.value = res.data || []
  } catch (e) {
    console.error('获取推荐课程失败', e)
  }
}

const handleSelect = (course) => {
  if (!isSelectionOpen.value) {
    ElMessage.warning('当前不在选课时间内')
    return
  }
  if (course.remaining <= 0) {
    ElMessage.warning('该课程已满')
    return
  }
  
  ElMessageBox.confirm(`确定选择课程「${course.courseName}」吗？`, '选课确认', { type: 'info' })
    .then(async () => {
      await selectCourse(course.id)
      ElMessage.success('选课成功')
      fetchList()
    })
    .catch(() => {})
}

const handleDrop = (course) => {
  if (!isDropOpen.value) {
    ElMessage.warning('当前不在退课时间内')
    return
  }
  
  ElMessageBox.confirm(`确定退选课程「${course.courseName}」吗？`, '退课确认', { type: 'warning' })
    .then(async () => {
      await dropCourse(course.id)
      ElMessage.success('退课成功')
      fetchList()
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

.footer-buttons {
  display: flex;
  gap: 8px;
}

.text-danger {
  color: var(--danger-color);
  font-weight: 600;
}

.recommend-card {
  position: relative;
  border-left: 3px solid var(--success-color);
}

.recommend-tag {
  position: absolute;
  top: 12px;
  right: 12px;
}
</style>