<template>
  <div class="attendance-teacher-page">
    <el-row :gutter="20">
      <el-col :span="10">
        <div class="page-card">
          <div class="page-header">
            <span class="page-title">发起九宫格签到</span>
          </div>
          <el-form :model="form" label-width="100px" v-show="!sessionShow">
            <el-form-item label="选择课程">
              <el-select v-model="form.courseId" filterable style="width: 100%">
                <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="签到时长">
              <el-input-number v-model="form.duration" :min="1" :max="20" />
              <span style="margin-left: 8px; color: var(--text-secondary)">分钟（最长20分钟）</span>
            </el-form-item>
          </el-form>
          <!-- 锁屏手势九宫格（会话期间保留图案展示） -->
          <div class="pattern-box">
            <div class="pattern-title">滑动绘制签到图案</div>
            <div
              class="lock-pattern-wrap"
              @mousedown="onMouseDown"
              @mousemove="onMouseMove"
              @mouseup="onMouseUp"
              @mouseleave="onMouseUp"
              @touchstart.prevent="onTouchStart"
              @touchmove.prevent="onTouchMove"
              @touchend.prevent="onTouchEnd"
              ref="lockWrapRef"
            >
              <svg class="lock-svg">
                <path
                  v-if="linePath"
                  :d="linePath"
                  stroke="url(#lineGradient)"
                  stroke-width="8"
                  fill="none"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  class="fixed-line"
                />
                <path
                  v-if="tempPath"
                  :d="tempPath"
                  stroke="url(#lineGradient)"
                  stroke-width="6"
                  fill="none"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  class="temp-line"
                />
                <defs>
                  <linearGradient id="lineGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="#409eff" />
                    <stop offset="100%" stop-color="#667eea" />
                  </linearGradient>
                </defs>
                <circle
                  v-if="isDrawing && trackPoint"
                  :cx="trackPoint.x"
                  :cy="trackPoint.y"
                  r="5"
                  fill="#409eff"
                  class="track-dot"
                />
              </svg>
              <div class="lock-grid">
                <div
                  v-for="idx in 9"
                  :key="idx-1"
                  class="lock-cell"
                  :class="{ active: selectedPattern.includes(idx-1) }"
                  :data-index="idx-1"
                >
                  <div class="lock-circle">
                    <div class="lock-inner"></div>
                  </div>
                </div>
              </div>
            </div>
            <!-- 只有未开启签到会话时，才显示清空按钮与节点计数 -->
            <div class="pattern-ops" v-if="!sessionShow">
              <el-button size="small" @click="clearPattern">清空图案</el-button>
              <span class="pattern-desc">
                已绘制 {{ selectedPattern.length }} 个节点
              </span>
            </div>
          </div>
          <el-form-item v-show="!sessionShow">
            <el-button
              type="primary"
              :icon="VideoPlay"
              @click="handleGenerate"
              :disabled="!form.courseId || selectedPattern.length < 3"
            >
              生成签到会话
            </el-button>
          </el-form-item>
          <!-- 进行中会话：课程 + 签到图案图形 + 剩余时间 + 结束签到；移除数字序列文本 -->
          <div v-if="sessionShow" class="code-display">
            <div class="cd-course">{{ sessionCourseName || '课堂签到' }}</div>
            <div class="cd-tip">签到进行中，请提醒学生对照上方九宫格图案完成签到</div>
            <div class="cd-countdown">
              <span class="cd-unit">剩余</span>
              <span class="cd-time">{{ remainingTime }}</span>
            </div>
            <el-button type="danger" round :icon="VideoPause" :loading="ending" @click="handleEnd">结束签到</el-button>
          </div>
        </div>
      </el-col>
      <el-col :span="14">
        <div class="page-card">
          <div class="page-header">
            <span class="page-title">课堂考勤名单</span>
            <el-select
              v-model="viewSessionId"
              class="session-switch"
              placeholder="切换考勤会话"
              clearable
              @change="onSwitchSession"
            >
              <el-option
                v-if="sessionShow && activeSessionId"
                :label="'当前签到 · ' + sessionCourseName"
                :value="activeSessionId"
              />
              <el-option
                v-for="s in historyList"
                :key="s.sessionId"
                :label="historyLabel(s)"
                :value="s.sessionId"
              />
            </el-select>
            <el-tag v-if="viewSessionId" :type="viewStatus === 1 ? 'success' : 'info'">
              {{ viewStatus === 1 ? `应到 ${stats.total || 0} 人` : '历史考勤 · 只读' }}
            </el-tag>
            <el-tag v-else type="info">暂无考勤会话</el-tag>
          </div>
          <!-- 考勤统计概览（进行中/历史会话均展示，避免结束后列表缩短一截） -->
          <div v-if="viewSessionId" class="stats-bar">
            <span class="stat-item"><i class="dot" style="background:#67c23a"></i>考勤成功 {{ stats.present || 0 }}</span>
            <span class="stat-item"><i class="dot" style="background:#13c2c2"></i>手动签到 {{ stats.manual || 0 }}</span>
            <span class="stat-item"><i class="dot" style="background:#e6a23c"></i>迟到 {{ stats.late || 0 }}</span>
            <span class="stat-item"><i class="dot" style="background:#409eff"></i>请假 {{ stats.leave || 0 }}</span>
            <span class="stat-item"><i class="dot" style="background:#f56c6c"></i>缺勤 {{ stats.absent || 0 }}</span>
            <span class="stat-item"><i class="dot" style="background:#c0392b"></i>旷课 {{ stats.truant || 0 }}</span>
            <span class="stat-item rate-item">出勤率 {{ stats.rate || 0 }}%</span>
          </div>
          <el-table :data="records" v-loading="loading" border stripe max-height="460">
            <el-table-column type="index" label="#" width="50" align="center" />
            <el-table-column prop="studentNo" label="学号" width="110" />
            <el-table-column prop="studentName" label="姓名" width="90" />
            <el-table-column prop="className" label="班级" width="110" />
            <el-table-column prop="checkinTime" label="签到时间" width="170">
              <template #default="{ row }">
                <span v-if="row.checkinTime">{{ formatTime(row.checkinTime) }}</span>
                <span v-else style="color: #c0c4cc">—</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small" effect="light">
                  {{ statusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column v-if="viewStatus === 1" label="考勤操作" width="260" align="center">
              <template #default="{ row }">
                <el-button
                  link
                  type="success"
                  size="small"
                  :disabled="row.status !== 0"
                  @click="handleManualCheckin(row)"
                >手动签到</el-button>
                <el-button
                  link
                  type="warning"
                  size="small"
                  :disabled="row.status === 2"
                  @click="handleUpdateStatus(row, 2)"
                >迟到</el-button>
                <el-button
                  link
                  type="primary"
                  size="small"
                  :disabled="row.status === 3"
                  @click="handleUpdateStatus(row, 3)"
                >请假</el-button>
                <el-button
                  link
                  type="danger"
                  size="small"
                  :disabled="row.status === 4"
                  @click="handleUpdateStatus(row, 4)"
                >旷课</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!viewSessionId && records.length === 0 && !loading" class="list-empty">
            <el-empty description="暂无考勤会话，发起签到后将在此展示完整考勤名单" :image-size="80" />
          </div>
          <div v-else-if="records.length === 0 && !loading" class="list-empty">
            <el-empty description="该会话暂无考勤记录" :image-size="80" />
          </div>
          <div class="mt-20 text-right">
            <el-button type="primary" :icon="Download" :disabled="!viewSessionId" @click="handleExport">导出考勤报表</el-button>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoPlay, VideoPause, Download } from '@element-plus/icons-vue'
import {
  generateCheckinCode,
  getSessionDetail,
  getTeacherCurrentSession,
  updateAttendanceStatus,
  manualCheckin,
  getAttendanceSessions,
  endAttendanceSession,
  exportAttendance,
  getTeacherCourseOptions
} from '@/api/attendance'
const lockWrapRef = ref(null)
const loading = ref(false)
const ending = ref(false)
const courseList = ref([])
const records = ref([])
const stats = ref({})
// 左侧：是否有进行中的签到会话（控制表单/九宫格锁定/倒计时）
const sessionShow = ref(false)
const activeSessionId = ref(null)
const sessionCourseName = ref('')
const remainingTime = ref('')
// 右侧：当前查看的会话（可能是进行中，也可能是历史）
const viewSessionId = ref(null)
const viewStatus = ref(0)
const viewCourseName = ref('')
const historyList = ref([])
let timer = null
let pollTimer = null
const selectedPattern = ref([])
const linePath = ref('')
const tempPath = ref('')
const isDrawing = ref(false)
const trackPoint = ref(null)
const cellCenter = ref([])
const form = reactive({
  courseId: null,
  duration: 10
})
// ============ 九宫格绘制逻辑 ============
const calcCellPositions = () => {
  if (!lockWrapRef.value) return
  cellCenter.value = []
  const cells = lockWrapRef.value.querySelectorAll('.lock-cell')
  const wrapRect = lockWrapRef.value.getBoundingClientRect()
  cells.forEach(cell => {
    const rect = cell.getBoundingClientRect()
    cellCenter.value.push({
      x: rect.left - wrapRect.left + rect.width / 2,
      y: rect.top - wrapRect.top + rect.height / 2
    })
  })
}
const clearPattern = () => {
  selectedPattern.value = []
  linePath.value = ''
  tempPath.value = ''
  trackPoint.value = null
  isDrawing.value = false
}
const getHoverIndex = (clientX, clientY) => {
  if (!lockWrapRef.value || cellCenter.value.length === 0) return -1
  const wrapRect = lockWrapRef.value.getBoundingClientRect()
  const x = clientX - wrapRect.left
  const y = clientY - wrapRect.top
  const ACTIVATE_THRESHOLD = 22
  for (let i = 0; i < cellCenter.value.length; i++) {
    const center = cellCenter.value[i]
    const dx = x - center.x
    const dy = y - center.y
    if (Math.sqrt(dx * dx + dy * dy) < ACTIVATE_THRESHOLD) return i
  }
  return -1
}
const updateSvgPath = () => {
  if (selectedPattern.value.length < 1) {
    linePath.value = ''
    return
  }
  let d = `M ${cellCenter.value[selectedPattern.value[0]].x} ${cellCenter.value[selectedPattern.value[0]].y}`
  for (let i = 1; i < selectedPattern.value.length; i++) {
    d += ` L ${cellCenter.value[selectedPattern.value[i]].x} ${cellCenter.value[selectedPattern.value[i]].y}`
  }
  linePath.value = d
}
const updateTempPath = (clientX, clientY) => {
  if (selectedPattern.value.length < 1 || !lockWrapRef.value) {
    tempPath.value = ''
    return
  }
  const wrapRect = lockWrapRef.value.getBoundingClientRect()
  const lastIdx = selectedPattern.value[selectedPattern.value.length - 1]
  const lastCenter = cellCenter.value[lastIdx]
  const x = clientX - wrapRect.left
  const y = clientY - wrapRect.top
  tempPath.value = `M ${lastCenter.x} ${lastCenter.y} L ${x} ${y}`
}
const updateTrackPoint = (clientX, clientY) => {
  if (!lockWrapRef.value) return
  const wrapRect = lockWrapRef.value.getBoundingClientRect()
  trackPoint.value = { x: clientX - wrapRect.left, y: clientY - wrapRect.top }
}
const appendPoint = (clientX, clientY) => {
  const idx = getHoverIndex(clientX, clientY)
  if (idx !== -1 && !selectedPattern.value.includes(idx)) {
    selectedPattern.value.push(idx)
    updateSvgPath()
  }
}
const onMouseDown = (e) => {
  if (sessionShow.value) return
  calcCellPositions()
  isDrawing.value = true
  trackPoint.value = null
  tempPath.value = ''
  appendPoint(e.clientX, e.clientY)
}
const onMouseMove = (e) => {
  if (!isDrawing.value) return
  updateTrackPoint(e.clientX, e.clientY)
  updateTempPath(e.clientX, e.clientY)
  appendPoint(e.clientX, e.clientY)
}
const onMouseUp = () => {
  tempPath.value = ''
  if (selectedPattern.value.length > 0 && selectedPattern.value.length < 3) {
    ElMessage.warning('图案太简单，请至少连接3个点')
    clearPattern()
    return
  }
  isDrawing.value = false
  trackPoint.value = null
}
const onTouchStart = (e) => {
  if (sessionShow.value) return
  calcCellPositions()
  isDrawing.value = true
  trackPoint.value = null
  tempPath.value = ''
  const touch = e.touches[0]
  appendPoint(touch.clientX, touch.clientY)
}
const onTouchMove = (e) => {
  if (!isDrawing.value) return
  const touch = e.touches[0]
  updateTrackPoint(touch.clientX, touch.clientY)
  updateTempPath(touch.clientX, touch.clientY)
  appendPoint(touch.clientX, touch.clientY)
}
const onTouchEnd = () => {
  tempPath.value = ''
  if (selectedPattern.value.length > 0 && selectedPattern.value.length < 3) {
    ElMessage.warning('图案太简单，请至少连接3个点')
    clearPattern()
    return
  }
  isDrawing.value = false
  trackPoint.value = null
}
// ============ 考勤业务逻辑 ============
const statusText = (status) => ({
  0: '缺勤', 1: '考勤成功', 2: '迟到', 3: '请假', 4: '旷课', 5: '手动签到'
}[status] || '未知')
const statusType = (status) => ({
  0: 'danger', 1: 'success', 2: 'warning', 3: 'info', 4: 'danger', 5: 'primary'
}[status] || 'info')

const formatTime = (t) => {
  if (!t) return ''
  return String(t).replace('T', ' ').substring(0, 19)
}
const formatDate = (t) => {
  if (!t) return ''
  return String(t).replace('T', ' ').substring(0, 10)
}
const historyLabel = (s) => `${s.courseName || '课程'} · ${formatDate(s.sessionDate)}（已结束）`
const loadCourses = async () => {
  try {
    const res = await getTeacherCourseOptions()
    courseList.value = res.data || []
  } catch (e) {}
}
// 加载该教师历史考勤会话（仅已结束，供名单切换）
const loadHistory = async () => {
  try {
    const res = await getAttendanceSessions()
    historyList.value = (res.data || []).filter(s => s.sessionId !== activeSessionId.value && s.status === 0)
  } catch (e) {}
}
// 进入页面：查询该教师是否有进行中的签到会话（同一教师同一时刻仅一个），有则恢复展示
const checkTeacherCurrent = async () => {
  try {
    const res = await getTeacherCurrentSession()
    const s = res.data
    if (s && s.sessionId) {
      activeSessionId.value = s.sessionId
      sessionShow.value = true
      sessionCourseName.value = s.courseName || ''
      remainingTime.value = formatRemain(s.remainingSeconds ?? 0)
      // 回显签到图案（九宫格保留展示）
      if (Array.isArray(s.pattern) && s.pattern.length) {
        selectedPattern.value = s.pattern
        await nextTick()
        calcCellPositions()
        updateSvgPath()
      }
      // 右侧默认查看进行中会话
      viewSessionId.value = s.sessionId
      viewStatus.value = 1
      viewCourseName.value = s.courseName || ''
      startTimers()
      await fetchView()
      await loadHistory()
      ElMessage.info('检测到您有进行中的签到会话，已自动恢复。请先结束当前签到，才能发起新的签到。')
      return
    }
  } catch (e) {}
  // 无进行中会话：默认展示最近一条历史考勤
  await loadHistory()
  if (historyList.value.length > 0) {
    const latest = historyList.value[0]
    viewSessionId.value = latest.sessionId
    viewStatus.value = 0
    viewCourseName.value = latest.courseName || ''
    fetchView()
  }
}
const onSwitchSession = async (value) => {
  if (!value) {
    records.value = []
    stats.value = {}
    viewSessionId.value = null
    viewStatus.value = 0
    return
  }
  if (value === activeSessionId.value) {
    viewStatus.value = 1
    viewCourseName.value = sessionCourseName.value
  } else {
    const s = historyList.value.find(h => h.sessionId === value)
    viewStatus.value = s ? s.status : 0
    viewCourseName.value = s ? s.courseName || '' : ''
  }
  await fetchView()
}
const handleGenerate = async () => {
  if (selectedPattern.value.length < 3) {
    ElMessage.warning('请绘制签到图案，至少连接3个节点')
    return
  }
  try {
    const payload = {
      ...form,
      pattern: selectedPattern.value
    }
    const res = await generateCheckinCode(payload)
    activeSessionId.value = res.data.sessionId
    sessionShow.value = true
    sessionCourseName.value = res.data.courseName || courseList.value.find(c => c.id === form.courseId)?.courseName || ''
    remainingTime.value = formatRemain(res.data.remainingSeconds ?? (form.duration || 10) * 60)
    viewSessionId.value = activeSessionId.value
    viewStatus.value = 1
    viewCourseName.value = sessionCourseName.value
    startTimers()
    await fetchView()
    await loadHistory()
    ElMessage.success('九宫格签到会话已开启')
  } catch (e) {}
}
const fetchView = async () => {
  if (!viewSessionId.value) {
    records.value = []
    stats.value = {}
    return
  }
  loading.value = true
  try {
    const res = await getSessionDetail(viewSessionId.value)
    const data = res.data || {}
    records.value = data.records || []
    stats.value = data.stats || {}
    if (data.session) {
      viewStatus.value = data.session.status
      if (data.session.courseName) viewCourseName.value = data.session.courseName
      // 查看的是进行中会话：同步左侧倒计时
      if (data.session.status === 1 && viewSessionId.value === activeSessionId.value) {
        sessionCourseName.value = data.session.courseName || ''
        if (data.session.remainingSeconds != null) {
          remainingTime.value = formatRemain(data.session.remainingSeconds)
        }
      }
    }
  } finally {
    loading.value = false
  }
}
const formatRemain = (seconds) => {
  const s = Math.max(0, Number(seconds))
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${m}分${sec}秒`
}
const startTimers = () => {
  stopTimers()
  // 本地秒级倒计时（进行中会话）
  timer = setInterval(() => {
    const remain = parseRemain(remainingTime.value)
    if (remain <= 0) {
      if (remainingTime.value && remainingTime.value !== '已结束') {
        remainingTime.value = '已结束'
      }
      return
    }
    remainingTime.value = formatRemain(remain - 1)
  }, 1000)
  // 每10秒拉取后端最新名单与剩余时间
  pollTimer = setInterval(() => {
    fetchView()
  }, 10000)
}
const parseRemain = (str) => {
  const m = /(\d+)分(\d+)秒/.exec(str)
  if (m) return Number(m[1]) * 60 + Number(m[2])
  return 0
}
const stopTimers = () => {
  if (timer) { clearInterval(timer); timer = null }
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}
const handleManualCheckin = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定为 ${row.studentName}（${row.studentNo}）执行手动签到吗？\n仅用于学生到场但无法自主签到的情况，状态将标记为「手动签到」特殊留痕。`,
      '手动签到',
      { type: 'warning', confirmButtonText: '确认签到' }
    )
    await manualCheckin(row.recordId)
    ElMessage.success('手动签到成功')
    fetchView()
  } catch (e) {}
}
const handleUpdateStatus = async (row, status) => {
  const label = status === 2 ? '迟到' : (status === 4 ? '旷课' : '请假')
  const tip = status === 4
    ? `确定将 ${row.studentName}（${row.studentNo}）标记为「旷课」吗？标记后将联动生成旷课预警。`
    : `确定将 ${row.studentName}（${row.studentNo}）标记为「${label}」吗？`
  try {
    await ElMessageBox.confirm(tip, '考勤修改', { type: 'warning' })
    await updateAttendanceStatus(row.recordId, status)
    ElMessage.success('考勤状态已修改')
    fetchView()
  } catch (e) {}
}
const handleEnd = () => {
  ElMessageBox.confirm('确定结束本次签到吗？结束后未签到学生将落定为旷课，并联动生成旷课预警。', '提示', { type: 'warning' })
    .then(async () => {
      ending.value = true
      try {
        await endAttendanceSession(activeSessionId.value)
        ElMessage.success('签到已结束')
        sessionShow.value = false
        sessionCourseName.value = ''
        remainingTime.value = ''
        stopTimers()
        clearPattern()
        // 结束后默认展示最近一条历史考勤
        await loadHistory()
        if (historyList.value.length > 0) {
          const latest = historyList.value[0]
          viewSessionId.value = latest.sessionId
          viewStatus.value = 0
          viewCourseName.value = latest.courseName || ''
          fetchView()
        } else {
          viewSessionId.value = null
          viewStatus.value = 0
          records.value = []
          stats.value = {}
        }
      } catch (e) {
      } finally {
        ending.value = false
      }
    })
    .catch(() => {})
}
const handleExport = () => {
  if (!viewSessionId.value) return
  exportAttendance(viewSessionId.value).then((res) => {
    const blob = new Blob([res])
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `考勤记录_${viewSessionId.value}_${new Date().getTime()}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  }).catch(() => {
    ElMessage.error('导出失败')
  })
}
onMounted(async () => {
  loadCourses()
  checkTeacherCurrent()
})
onUnmounted(() => {
  stopTimers()
})
</script>
<style scoped>
.pattern-box {
  margin: 16px 0;
}
.pattern-title {
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
  text-align: center;
}
.lock-pattern-wrap {
  position: relative;
  width: 260px;
  height: 260px;
  margin: 0 auto;
  user-select: none;
  touch-action: none;
  overflow: hidden;
}
.lock-svg {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 10;
}
.fixed-line {
  opacity: 0.95;
  filter: drop-shadow(0 0 2px rgba(0,0,0,0.3)) drop-shadow(0 0 6px rgba(64, 158, 255, 0.8));
}
.temp-line {
  opacity: 0.6;
  filter: drop-shadow(0 0 4px rgba(64, 158, 255, 0.6));
}
.track-dot {
  filter: drop-shadow(0 0 2px rgba(0,0,0,0.3)) drop-shadow(0 0 6px rgba(64, 158, 255, 0.9));
}
.lock-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(3, 1fr);
  width: 100%;
  height: 100%;
  gap: 0;
}
.lock-cell {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.lock-circle {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: 2px solid #c0c4cc;
  background: transparent;
  position: relative;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}
.lock-cell.active .lock-circle {
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.35);
  transform: scale(1.15);
  box-shadow:
    0 0 0 6px rgba(64, 158, 255, 0.15),
    0 0 20px rgba(64, 158, 255, 0.5);
}
.lock-inner {
  position: absolute;
  width: 22px;
  height: 22px;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background: #fff;
  opacity: 0;
  transition: all 0.2s ease;
}
.lock-cell.active .lock-inner {
  opacity: 1;
  background: rgba(255, 255, 255, 0.85);
}
.lock-cell.active .lock-circle::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2px solid #409eff;
  transform: translate(-50%, -50%) scale(1);
  animation: ripple 0.6s ease-out;
  pointer-events: none;
}
@keyframes ripple {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.8;
  }
  100% {
    transform: translate(-50%, -50%) scale(1.8);
    opacity: 0;
  }
}
.pattern-ops {
  margin-top: 12px;
  text-align: center;
}
.pattern-desc {
  margin-left: 8px;
  font-size: 13px;
  color: #666;
}
.code-display {
  text-align: center;
  padding: 36px 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 14px;
  color: #fff;
  margin-top: 20px;
}
.cd-course {
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 1px;
  margin-bottom: 6px;
}
.cd-tip {
  font-size: 13px;
  opacity: 0.85;
  margin: 16px 0 20px;
}
.cd-countdown {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 10px;
  margin-bottom: 24px;
}
.cd-unit {
  font-size: 15px;
  opacity: 0.9;
}
.cd-time {
  font-size: 40px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  letter-spacing: 2px;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.25);
}
.list-empty {
  padding: 10px 0;
}
.stats-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  padding: 10px 4px 14px;
  font-size: 13px;
  color: #606266;
}
.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}
.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.session-switch {
  width: 220px;
  margin-left: 12px;
}
.rate-item {
  margin-left: auto;
  font-weight: 600;
  color: #303133;
}
</style>
