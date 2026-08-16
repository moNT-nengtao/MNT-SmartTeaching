<template>
  <div class="attendance-teacher-page">
    <el-row :gutter="20">
      <el-col :span="10">
        <div class="page-card">
          <div class="page-header">
            <span class="page-title">发起签到</span>
          </div>
          <el-form :model="form" label-width="100px">
            <el-form-item label="选择课程">
              <el-select v-model="form.courseId" filterable style="width: 100%">
                <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="签到时长">
              <el-input-number v-model="form.duration" :min="1" :max="60" />
              <span style="margin-left: 8px; color: var(--text-secondary)">分钟</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="VideoPlay" @click="handleGenerate" :disabled="!form.courseId">
                生成签到码
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 签到码展示 -->
          <div v-if="checkinCode" class="code-display">
            <div class="code-label">当前签到码</div>
            <div class="code-number">{{ checkinCode }}</div>
            <div class="countdown">
              剩余时间：<el-tag type="danger" effect="dark">{{ remainingTime }}</el-tag>
            </div>
            <el-button type="danger" :icon="VideoPause" @click="handleEnd">结束签到</el-button>
          </div>
        </div>
      </el-col>

      <el-col :span="14">
        <div class="page-card">
          <div class="page-header">
            <span class="page-title">实时签到名单</span>
            <el-tag type="success">已签到 {{ checkedList.length }} 人</el-tag>
          </div>
          <el-table :data="checkedList" v-loading="loading" border stripe max-height="400">
            <el-table-column prop="studentNo" label="学号" width="120" />
            <el-table-column prop="studentName" label="姓名" width="100" />
            <el-table-column prop="className" label="班级" width="120" />
            <el-table-column prop="checkinTime" label="签到时间" width="170" />
            <el-table-column label="状态" width="80">
              <template #default>
                <el-tag type="success" size="small">已签到</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="checkedList.length === 0 && !loading" description="暂无签到记录" />

          <div class="mt-20 text-right">
            <el-button type="primary" :icon="Download" @click="handleExport">导出考勤报表</el-button>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoPlay, VideoPause, Download } from '@element-plus/icons-vue'
import { generateCheckinCode, getCheckinList, exportAttendance } from '@/api/attendance'

const loading = ref(false)
const courseList = ref([])
const checkedList = ref([])
const checkinCode = ref('')
const sessionId = ref(null)
const remainingTime = ref('')
let timer = null
let endTime = null

const form = reactive({
  courseId: null,
  duration: 10
})

const handleGenerate = async () => {
  try {
    const res = await generateCheckinCode(form)
    checkinCode.value = res.data.code
    sessionId.value = res.data.sessionId
    endTime = Date.now() + form.duration * 60 * 1000
    startCountdown()
    fetchCheckedList()
    ElMessage.success('签到码已生成，请告知学生')
  } catch (e) {}
}

const startCountdown = () => {
  timer = setInterval(() => {
    const diff = endTime - Date.now()
    if (diff <= 0) {
      remainingTime.value = '已结束'
      clearInterval(timer)
      return
    }
    const m = Math.floor(diff / 60000)
    const s = Math.floor((diff % 60000) / 1000)
    remainingTime.value = `${m}分${s}秒`
  }, 1000)
}

const fetchCheckedList = async () => {
  if (!sessionId.value) return
  loading.value = true
  try {
    const res = await getCheckinList(sessionId.value)
    checkedList.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleEnd = () => {
  ElMessageBox.confirm('确定结束本次签到吗？', '提示', { type: 'warning' })
    .then(() => {
      clearInterval(timer)
      checkinCode.value = ''
      remainingTime.value = ''
      ElMessage.success('签到已结束')
    })
    .catch(() => {})
}

const handleExport = () => {
  exportAttendance({ sessionId: sessionId.value })
  ElMessage.success('导出成功')
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.code-display {
  text-align: center;
  padding: 30px 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 12px;
  color: #fff;
  margin-top: 20px;
}

.code-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 12px;
}

.code-number {
  font-size: 56px;
  font-weight: 700;
  letter-spacing: 12px;
  margin-bottom: 16px;
  font-family: 'Courier New', monospace;
}

.countdown {
  margin-bottom: 20px;
}
</style>
