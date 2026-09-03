<template>
  <div class="attendance-student-page">
    <!-- 当前待签到卡片：写明课程，同一时间段只允许完成当前签到 -->
    <div class="current-session-card" v-if="currentSession">
      <div v-if="!currentSession.checkedIn" class="cs-inner cs-active">
        <div class="cs-left">
          <div class="cs-tag">当前待签到</div>
          <div class="cs-course">{{ currentSession.courseName }}</div>
          <div class="cs-meta">授课教师：{{ currentSession.teacherName || '—' }}</div>
        </div>
        <div class="cs-right">
          <div class="cs-label">剩余时间</div>
          <div class="cs-time">{{ currentRemain }}</div>
        </div>
      </div>
      <div v-else class="cs-inner cs-done">
        <div class="cs-left">
          <div class="cs-tag cs-tag-done">已完成</div>
          <div class="cs-course">{{ currentSession.courseName }}</div>
          <div class="cs-meta">考勤状态：{{ statusText(currentSession.status) }}</div>
        </div>
        <div class="cs-right cs-checkmark">✓</div>
      </div>
    </div>
    <el-empty
      v-else-if="!currentLoading && !currentSession"
      class="no-current"
      description="当前没有进行中的签到，请等待老师发起签到"
      :image-size="70"
    />

    <el-row :gutter="20">
      <el-col :span="10">
        <div class="page-card">
          <div class="page-header">
            <span class="page-title">课堂签到</span>
          </div>
          <div class="checkin-form" v-show="!currentSession || !currentSession.checkedIn">
            <div class="pattern-box-student">
              <div class="pattern-tip">滑动绘制老师给出的签到图案</div>
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
                  <!-- 已确认的固定连线 -->
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
                  <!-- 实时跟随的临时轨迹线 -->
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
                    :class="{ active: inputPattern.includes(idx-1) }"
                    :data-index="idx-1"
                  >
                    <div class="lock-circle">
                      <div class="lock-inner"></div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="pattern-ops-student">
                <el-button size="small" @click="clearInputPattern">清空重画</el-button>
                <div class="path-show">已绘制 {{ inputPattern.length }} 个节点</div>
              </div>
            </div>
            <el-button
              type="primary"
              size="large"
              class="checkin-btn"
              :loading="loading"
              :disabled="inputPattern.length < 3 || (currentSession && currentSession.checkedIn)"
              @click="handleCheckin"
            >
              立即签到
            </el-button>
            <el-result
              v-if="checkinResult"
              :icon="checkinResult.success ? 'success' : 'error'"
              :title="checkinResult.success ? '签到成功' : '签到失败'"
              :sub-title="checkinResult.message"
            />
          </div>
        </div>
      </el-col>
      <el-col :span="14">
        <div class="page-card">
          <div class="page-header">
            <span class="page-title">我的考勤记录</span>
            <el-tag type="success">出勤率 {{ attendanceRate }}%</el-tag>
          </div>
          <el-table :data="attendanceList" v-loading="listLoading" border stripe>
            <el-table-column prop="courseName" label="课程" min-width="140" />
            <el-table-column prop="sessionDate" label="日期" width="120" />
            <el-table-column prop="checkinTime" label="签到时间" width="170" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">
                  {{ statusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="attendanceList.length === 0 && !listLoading" description="暂无考勤记录" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { submitCheckin, getMyAttendance, getStudentCurrentSession } from '@/api/attendance'

const lockWrapRef = ref(null)
const loading = ref(false)
const listLoading = ref(false)
const inputPattern = ref([])
const checkinResult = ref(null)
const attendanceList = ref([])
const linePath = ref('')
const tempPath = ref('') // 临时轨迹线
const isDrawing = ref(false)
const trackPoint = ref(null)
const cellCenter = ref([])
// 当前待签到会话
const currentSession = ref(null)
const currentLoading = ref(false)
const currentRemain = ref('')
let checkinTimer = null

const attendanceRate = computed(() => {
  if (attendanceList.value.length === 0) return 0
  const attended = attendanceList.value.filter(a => a.status === 1 || a.status === 2).length
  return Math.round((attended / attendanceList.value.length) * 100)
})

const statusText = (status) => ({
  0: '缺勤',
  1: '考勤成功',
  2: '迟到',
  3: '请假',
  4: '旷课',
  5: '手动签到'
}[status] || '未知')

const statusType = (status) => ({
  0: 'danger',
  1: 'success',
  2: 'warning',
  3: 'info',
  4: 'danger',
  5: 'primary'
}[status] || 'info')

// 计算每个格子的中心点坐标
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

const clearInputPattern = () => {
  inputPattern.value = []
  linePath.value = ''
  tempPath.value = ''
  trackPoint.value = null
  isDrawing.value = false
}

// 基于距离判定选中格子，解决误触问题
const getHoverIndex = (clientX, clientY) => {
  if (!lockWrapRef.value || cellCenter.value.length === 0) return -1
  const wrapRect = lockWrapRef.value.getBoundingClientRect()
  const x = clientX - wrapRect.left
  const y = clientY - wrapRect.top
  const ACTIVATE_THRESHOLD = 22 // 激活阈值：距离中心点22px内才触发

  for (let i = 0; i < cellCenter.value.length; i++) {
    const center = cellCenter.value[i]
    const dx = x - center.x
    const dy = y - center.y
    if (Math.sqrt(dx * dx + dy * dy) < ACTIVATE_THRESHOLD) {
      return i
    }
  }
  return -1
}

// 更新已确认的固定路径
const updateSvgPath = () => {
  if (inputPattern.value.length < 1) {
    linePath.value = ''
    return
  }
  let d = `M ${cellCenter.value[inputPattern.value[0]].x} ${cellCenter.value[inputPattern.value[0]].y}`
  for (let i = 1; i < inputPattern.value.length; i++) {
    d += ` L ${cellCenter.value[inputPattern.value[i]].x} ${cellCenter.value[inputPattern.value[i]].y}`
  }
  linePath.value = d
}

// 更新实时临时轨迹线（从最后一个选中点到当前鼠标位置）
const updateTempPath = (clientX, clientY) => {
  if (inputPattern.value.length < 1 || !lockWrapRef.value) {
    tempPath.value = ''
    return
  }
  const wrapRect = lockWrapRef.value.getBoundingClientRect()
  const lastIdx = inputPattern.value[inputPattern.value.length - 1]
  const lastCenter = cellCenter.value[lastIdx]
  const x = clientX - wrapRect.left
  const y = clientY - wrapRect.top
  tempPath.value = `M ${lastCenter.x} ${lastCenter.y} L ${x} ${y}`
}

const updateTrackPoint = (clientX, clientY) => {
  if (!lockWrapRef.value) return
  const wrapRect = lockWrapRef.value.getBoundingClientRect()
  trackPoint.value = {
    x: clientX - wrapRect.left,
    y: clientY - wrapRect.top
  }
}

const onMouseDown = (e) => {
  calcCellPositions()
  isDrawing.value = true
  trackPoint.value = null
  tempPath.value = ''
  const idx = getHoverIndex(e.clientX, e.clientY)
  // 禁止重复选中同一个点
  if (idx !== -1 && !inputPattern.value.includes(idx)) {
    inputPattern.value.push(idx)
    updateSvgPath()
  }
}

const onMouseMove = (e) => {
  if (!isDrawing.value) return
  updateTrackPoint(e.clientX, e.clientY)
  updateTempPath(e.clientX, e.clientY) // 更新实时跟随线
  
  const idx = getHoverIndex(e.clientX, e.clientY)
  if (idx !== -1 && !inputPattern.value.includes(idx)) {
    inputPattern.value.push(idx)
    updateSvgPath()
  }
}

const onMouseUp = () => {
  tempPath.value = '' // 抬起时清除临时轨迹
  // 最少3个点校验，不足则提示并清空
  if (inputPattern.value.length > 0 && inputPattern.value.length < 3) {
    ElMessage.warning('图案太简单，请至少连接3个点')
    clearInputPattern()
    return
  }
  isDrawing.value = false
  trackPoint.value = null
}

const onTouchStart = (e) => {
  calcCellPositions()
  isDrawing.value = true
  trackPoint.value = null
  tempPath.value = ''
  const touch = e.touches[0]
  const idx = getHoverIndex(touch.clientX, touch.clientY)
  // 禁止重复选中同一个点
  if (idx !== -1 && !inputPattern.value.includes(idx)) {
    inputPattern.value.push(idx)
    updateSvgPath()
  }
}

const onTouchMove = (e) => {
  if (!isDrawing.value) return
  const touch = e.touches[0]
  updateTrackPoint(touch.clientX, touch.clientY)
  updateTempPath(touch.clientX, touch.clientY) // 更新实时跟随线
  
  const idx = getHoverIndex(touch.clientX, touch.clientY)
  if (idx !== -1 && !inputPattern.value.includes(idx)) {
    inputPattern.value.push(idx)
    updateSvgPath()
  }
}

const onTouchEnd = () => {
  tempPath.value = '' // 抬起时清除临时轨迹
  // 最少3个点校验，不足则提示并清空
  if (inputPattern.value.length > 0 && inputPattern.value.length < 3) {
    ElMessage.warning('图案太简单，请至少连接3个点')
    clearInputPattern()
    return
  }
  isDrawing.value = false
  trackPoint.value = null
}

const handleCheckin = async () => {
  if (inputPattern.value.length < 3) {
    ElMessage.warning('请绘制签到图案，至少连接3个节点')
    return
  }
  loading.value = true
  try {
    const res = await submitCheckin({ pattern: inputPattern.value })
    checkinResult.value = { success: true, message: `课程「${res.data?.courseName}」签到成功` }
    ElMessage.success('签到成功')
    // 同步当前待签到卡片为已完成，锁定本次签到
    if (currentSession.value && currentSession.value.sessionId === res.data?.sessionId) {
      currentSession.value.checkedIn = true
      currentSession.value.status = res.data?.status ?? 1
      currentSession.value.statusText = statusText(res.data?.status ?? 1)
    }
    currentRemain.value = '已完成'
    stopCurrentCountdown()
    fetchAttendance()
  } catch (e) {
    checkinResult.value = { success: false, message: e.response?.data?.msg || '如有特殊原因请寻求教师' }
  } finally {
    loading.value = false
  }
}

const fetchAttendance = async () => {
  listLoading.value = true
  try {
    const res = await getMyAttendance()
    attendanceList.value = res.data || []
  } finally {
    listLoading.value = false
  }
}

// 进入页面：查询当前待签到会话（写明课程），并启动剩余时间倒计时
const checkStudentCurrent = async () => {
  currentLoading.value = true
  try {
    const res = await getStudentCurrentSession()
    currentSession.value = res.data || null
    if (currentSession.value && !currentSession.value.checkedIn) {
      currentRemain.value = formatRemain(currentSession.value.remainingSeconds ?? 0)
      startCurrentCountdown()
    } else if (currentSession.value && currentSession.value.checkedIn) {
      currentRemain.value = '已完成'
    }
  } finally {
    currentLoading.value = false
  }
}

const startCurrentCountdown = () => {
  stopCurrentCountdown()
  checkinTimer = setInterval(() => {
    if (!currentSession.value || currentSession.value.checkedIn) {
      stopCurrentCountdown()
      return
    }
    const remain = parseRemain(currentRemain.value)
    if (remain <= 0) {
      currentRemain.value = '已结束'
      stopCurrentCountdown()
      return
    }
    currentRemain.value = formatRemain(remain - 1)
  }, 1000)
}

const stopCurrentCountdown = () => {
  if (checkinTimer) {
    clearInterval(checkinTimer)
    checkinTimer = null
  }
}

const formatRemain = (seconds) => {
  const s = Math.max(0, Number(seconds))
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${m}分${sec}秒`
}

const parseRemain = (str) => {
  const m = /(\d+)分(\d+)秒/.exec(str)
  if (m) return Number(m[1]) * 60 + Number(m[2])
  return 0
}

onMounted(() => {
  fetchAttendance()
  checkStudentCurrent()
})
onUnmounted(() => {
  stopCurrentCountdown()
})
</script>

<style scoped>
.current-session-card {
  margin-bottom: 20px;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}
.cs-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 28px;
}
.cs-active {
  background: linear-gradient(135deg, #ff9a56, #ff6a3d);
  color: #fff;
}
.cs-done {
  background: linear-gradient(135deg, #67c23a, #4caf50);
  color: #fff;
}
.cs-tag {
  display: inline-block;
  font-size: 13px;
  padding: 2px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.22);
  margin-bottom: 8px;
}
.cs-tag-done {
  background: rgba(255, 255, 255, 0.25);
}
.cs-course {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 1px;
}
.cs-meta {
  font-size: 13px;
  opacity: 0.9;
  margin-top: 6px;
}
.cs-right {
  text-align: right;
}
.cs-label {
  font-size: 13px;
  opacity: 0.85;
}
.cs-time {
  font-size: 32px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  letter-spacing: 1px;
}
.cs-checkmark {
  font-size: 40px;
  font-weight: 700;
}
.no-current {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 14px;
  border: 1px dashed #dcdfe6;
  padding: 8px 0;
}
.checkin-form {
  text-align: center;
  padding: 20px 0;
}
.pattern-tip {
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}
.lock-pattern-wrap {
  position: relative;
  width: 260px;
  height: 260px;
  margin: 0 auto;
  user-select: none;
  touch-action: none;
  overflow: hidden; /* 限制线条不超出区域 */
}

/* ========== 核心修复：连线层级提到最上层 ========== */
.lock-svg {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 10; /* 连线层级高于圆圈 */
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

/* 圆圈层级降低，让连线显示在上面 */
.lock-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(3, 1fr);
  width: 100%;
  height: 100%;
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

/* ========== 核心修复：激活圆改为半透明，不遮挡连线 ========== */
.lock-cell.active .lock-circle {
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.35); /* 半透明蓝色背景 */
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
  background: rgba(255, 255, 255, 0.85); /* 半透明白色圆心 */
}

/* 激活波纹特效 */
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

.pattern-ops-student {
  margin-top: 12px;
}
.path-show {
  margin-top: 6px;
  font-size: 13px;
  color: #666;
}
.checkin-btn {
  width: 200px;
  margin-top: 20px;
}
</style>
