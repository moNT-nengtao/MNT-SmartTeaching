<template>
  <div class="schedule-page">
    <div class="page-card">
      <div class="page-header">
        <span class="page-title">我的课表</span>
        <div class="header-actions">
          <el-select v-model="currentWeek" style="width: 140px" @change="fetchSchedule">
            <el-option v-for="w in 20" :key="w" :label="`第 ${w} 周`" :value="w" />
          </el-select>
          <el-button :icon="Refresh" @click="fetchSchedule">刷新</el-button>
        </div>
      </div>

      <!-- 下一节课倒计时 -->
      <div v-if="nextClass" class="next-class-bar">
        <el-icon :size="20"><Bell /></el-icon>
        <span>下一节课：<strong>{{ nextClass.courseName }}</strong> {{ nextClass.time }} {{ nextClass.classroom }}</span>
        <el-tag type="success" effect="dark">{{ countdown }}</el-tag>
      </div>

      <!-- 周日历网格 -->
      <div class="timetable">
        <div class="timetable-header">
          <div class="time-col">节次</div>
          <div v-for="day in weekDays" :key="day" class="day-col" :class="{ today: day.isToday }">
            {{ day.name }}<br /><small>{{ day.date }}</small>
          </div>
        </div>
        <div class="timetable-body">
          <div v-for="section in sections" :key="section" class="timetable-row">
            <div class="time-col">{{ section }}</div>
            <div
              v-for="day in weekDays"
              :key="day.key + '-' + section"
              class="day-col course-cell"
              @click="showCourseDetail(getCourse(day.key, section))"
            >
              <div
                v-if="getCourse(day.key, section)"
                class="course-block"
                :style="{ backgroundColor: getCourse(day.key, section).color || '#409eff' }"
              >
                <div class="course-name">{{ getCourse(day.key, section).courseName }}</div>
                <div class="course-room">{{ getCourse(day.key, section).classroom }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 课程详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentCourse?.courseName" width="400px">
      <div v-if="currentCourse" class="course-detail">
        <p><strong>授课教师：</strong>{{ currentCourse.teacherName }}</p>
        <p><strong>上课时间：</strong>{{ currentCourse.time }}</p>
        <p><strong>上课地点：</strong>{{ currentCourse.classroom }}</p>
        <p><strong>周次：</strong>{{ currentCourse.weekRange }}</p>
        <el-divider />
        <el-form-item label="课程颜色">
          <el-color-picker v-model="currentCourse.color" @change="handleColorChange" />
        </el-form-item>
        <el-form-item label="课程备忘">
          <el-input v-model="currentCourse.memo" type="textarea" :rows="3" placeholder="记录课程相关备忘..." />
          <el-button type="primary" size="small" class="mt-10" @click="handleSaveMemo">保存备忘</el-button>
        </el-form-item>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, Refresh } from '@element-plus/icons-vue'
import { getWeekSchedule, getNextClass, setCourseColor, setCourseMemo } from '@/api/schedule'

const currentWeek = ref(1)
const scheduleData = ref([])
const nextClass = ref(null)
const countdown = ref('')
const detailVisible = ref(false)
const currentCourse = ref(null)

const sections = ['第1-2节', '第3-4节', '第5-6节', '第7-8节', '第9-10节', '第11-12节']

const weekDays = computed(() => {
  const today = new Date()
  const dayOfWeek = today.getDay() || 7
  const monday = new Date(today)
  monday.setDate(today.getDate() - dayOfWeek + 1)
  const names = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return names.map((name, i) => {
    const d = new Date(monday)
    d.setDate(monday.getDate() + i)
    return {
      key: i + 1,
      name,
      date: `${d.getMonth() + 1}/${d.getDate()}`,
      isToday: d.toDateString() === today.toDateString()
    }
  })
})

const getCourse = (day, section) => {
  return scheduleData.value.find((c) => c.weekday === day && c.section === section)
}

const showCourseDetail = (course) => {
  if (!course) return
  currentCourse.value = { ...course }
  detailVisible.value = true
}

const handleColorChange = async (color) => {
  await setCourseColor(currentCourse.value.id, color)
  ElMessage.success('颜色已更新')
  fetchSchedule()
}

const handleSaveMemo = async () => {
  await setCourseMemo(currentCourse.value.id, currentCourse.value.memo)
  ElMessage.success('备忘已保存')
}

const fetchSchedule = async () => {
  try {
    const res = await getWeekSchedule({ week: currentWeek.value })
    scheduleData.value = res.data || []
  } catch (e) {}
}

const fetchNextClass = async () => {
  try {
    const res = await getNextClass()
    nextClass.value = res.data
  } catch (e) {}
}

let timer = null
const updateCountdown = () => {
  if (!nextClass.value) return
  const now = new Date()
  const classTime = new Date(nextClass.value.startTimestamp)
  const diff = classTime - now
  if (diff <= 0) {
    countdown.value = '已上课'
    return
  }
  const h = Math.floor(diff / 3600000)
  const m = Math.floor((diff % 3600000) / 60000)
  const s = Math.floor((diff % 60000) / 1000)
  countdown.value = `${h}时${m}分${s}秒后上课`
}

onMounted(() => {
  fetchSchedule()
  fetchNextClass()
  timer = setInterval(updateCountdown, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.next-class-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: linear-gradient(90deg, #ecf5ff, #f0f9eb);
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
}

.timetable {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.timetable-header,
.timetable-row {
  display: flex;
}

.timetable-header {
  background: #f5f7fa;
  font-weight: 600;
}

.time-col {
  width: 100px;
  min-width: 100px;
  padding: 12px 8px;
  text-align: center;
  border-right: 1px solid var(--border-color);
  font-size: 13px;
  color: var(--text-regular);
}

.day-col {
  flex: 1;
  padding: 12px 8px;
  text-align: center;
  border-right: 1px solid var(--border-color);
  font-size: 13px;
}

.day-col:last-child {
  border-right: none;
}

.day-col.today {
  background: #ecf5ff;
  color: var(--primary-color);
  font-weight: 600;
}

.course-cell {
  min-height: 70px;
  cursor: pointer;
  padding: 4px;
}

.course-cell:hover {
  background: #f5f7fa;
}

.course-block {
  height: 100%;
  border-radius: 6px;
  padding: 8px;
  color: #fff;
  font-size: 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.course-name {
  font-weight: 600;
  font-size: 13px;
}

.course-room {
  font-size: 11px;
  opacity: 0.9;
}

.course-detail p {
  margin: 8px 0;
}
</style>
