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

      <!-- ✅下一节课倒计时 完整保留 -->
      <div v-if="nextClass" class="next-class-bar">
        <el-icon :size="20"><Bell /></el-icon>
        <span>下一节课：<strong>{{ nextClass.courseName }}</strong> {{ nextClass.time }} {{ nextClass.classroom }}</span>
        <el-tag type="success" effect="dark">{{ countdown }}</el-tag>
      </div>

      <!-- 周日历网格 -->
      <div class="timetable">
        <div class="timetable-header">
          <div class="time-col">节次</div>
          <div v-for="day in weekDays" :key="day.key" class="day-col" :class="{ today: day.isToday }">
            {{ day.name }}<br /><small>{{ day.date }}</small>
          </div>
        </div>
        <div class="timetable-body">
          <div v-for="section in sections" :key="section.value" class="timetable-row">
            <div class="time-col">
              <div class="section-label">{{ section.label }}</div>
              <div class="section-time">{{ section.time }}</div>
            </div>
            <div
              v-for="day in weekDays"
              :key="day.key + '-' + section.value"
              class="day-col course-cell"
              @click="showCourseDetail(getCourse(day.key, section.value))"
            >
              <div
                v-if="getCourse(day.key, section.value)"
                class="course-block"
                :style="{ backgroundColor: getCourse(day.key, section.value).color || '#409eff' }"
              >
                <div class="course-name">{{ getCourse(day.key, section.value).courseName }}</div>
                <div class="course-teacher">👨‍🏫 {{ getCourse(day.key, section.value).teacherName }}</div>
                <div class="course-room">📍 {{ getCourse(day.key, section.value).classroom }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 课程详情弹窗：弃置颜色、收藏、备注编辑；仅保留【设置上课提醒】占位按钮 -->
    <el-dialog v-model="detailVisible" :title="currentCourse?.courseName" width="400px">
      <div v-if="currentCourse" class="course-detail">
        <p><strong>授课教师：</strong>{{ currentCourse.teacherName }}</p>
        <p><strong>上课时间：</strong>{{ currentCourse.time }}</p>
        <p><strong>上课地点：</strong>{{ currentCourse.classroom }}</p>
        <p><strong>周次：</strong>{{ currentCourse.weekRange }}</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <!-- 📌上课提醒占位按钮，功能待后端实现 -->
          <el-button type="primary" @click="openRemindSetting">设置上课提醒</el-button>
          <el-button @click="detailVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 📌上课提醒设置弹窗【占位UI，后端接口待实现】 -->
    <el-dialog v-model="remindDialogVisible" title="设置上课提醒" width="360px">
      <div v-if="currentCourse">
        <p>课程：{{ currentCourse.courseName }}</p>
        <el-radio-group v-model="remindMinute">
          <el-radio :label="10">提前10分钟</el-radio>
          <el-radio :label="20">提前20分钟</el-radio>
          <el-radio :label="30">提前30分钟</el-radio>
          <el-radio :label="0">关闭提醒</el-radio>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button @click="remindDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRemind">保存设置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, Refresh } from '@element-plus/icons-vue'
import { getWeekSchedule, getNextClass } from '@/api/schedule'

const currentWeek = ref(1)
const scheduleData = ref([])
const nextClass = ref(null)
const countdown = ref('')
const detailVisible = ref(false)
const currentCourse = ref(null)

// 上课提醒弹窗状态【占位】
const remindDialogVisible = ref(false)
const remindMinute = ref(10)

// 写死大学标准上课时间
const sections = [
  { label: '第1-2节', value: 1, time: '08:00‑09:40' },
  { label: '第3-4节', value: 2, time: '10:00‑11:40' },
  { label: '第5-6节', value: 3, time: '14:00‑15:40' },
  { label: '第7-8节', value: 4, time: '16:00‑17:40' },
  { label: '第9-10节', value: 5, time: '19:00‑20:40' },
  { label: '第11-12节', value: 6, time: '21:00‑22:40' }
]

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

/**
 * 根据星期key、lesson数字匹配课程
 * @param {number} day 星期1‑7
 * @param {number} lessonValue 节次数字1‑6
 */
const getCourse = (day, lessonValue) => {
  return scheduleData.value.find((c) => c.weekday === day && c.lesson === lessonValue)
}

const showCourseDetail = (course) => {
  if (!course) return
  currentCourse.value = { ...course }
  detailVisible.value = true
}

// 打开提醒设置弹窗【占位】
const openRemindSetting = () => {
  detailVisible.value = false
  remindDialogVisible.value = true
}

// 提交上课提醒设置【占位，接口待后端完成】
const submitRemind = async () => {
  ElMessage.info('上课提醒设置功能（待后端接口实现）：' + remindMinute.value + '分钟')
  // TODO: 对接 put 设置上课提醒接口
  // await setRemindApi({ scheduleId: currentCourse.value.scheduleId, offset: remindMinute.value })
  remindDialogVisible.value = false
}

const fetchSchedule = async () => {
  try {
    const res = await getWeekSchedule({ week: currentWeek.value })
    scheduleData.value = res.data || []
    console.log('课表数据:', scheduleData.value)
  } catch (e) {
    console.error('获取课表失败:', e)
  }
}

const fetchNextClass = async () => {
  try {
    const res = await getNextClass()
    nextClass.value = res.data
  } catch (e) {
  }
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
  padding: 8px 4px;
  text-align: center;
  border-right: 1px solid var(--border-color);
  font-size: 13px;
  color: var(--text-regular);
}
.section-label {
  font-weight: 600;
}
.section-time {
  font-size: 11px;
  color: #666;
  margin-top: 2px;
}
.day-col {
  flex: 1;
  padding: 4px;
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
  min-height: 90px;
  cursor: pointer;
  display: flex;
}
.course-cell:hover {
  background: #f5f7fa;
}
.course-block {
  width:100%;
  height: 100%;
  border-radius: 6px;
  padding: 8px 6px;
  color: #fff;
  font-size: 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-evenly;
  gap: 3px;
  word-break: break-all;
  overflow:hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.15);
}
.course-name {
  font-weight: 700;
  font-size: 13.5px;
  line-height:1.3;
}
.course-teacher {
  font-size: 11.5px;
  opacity: 0.92;
  line-height:1.2;
}
.course-room {
  font-size: 11.5px;
  opacity: 0.92;
  line-height:1.2;
}
.course-detail p {
  margin: 8px 0;
}
.dialog-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>
