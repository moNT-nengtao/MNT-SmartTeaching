<template>
  <div class="attendance-student-page">
    <el-row :gutter="20">
      <el-col :span="10">
        <div class="page-card">
          <div class="page-header">
            <span class="page-title">课堂签到</span>
          </div>
          <div class="checkin-form">
            <el-input
              v-model="checkinCode"
              placeholder="请输入6位签到码"
              maxlength="6"
              size="large"
              class="code-input"
            />
            <el-button
              type="primary"
              size="large"
              class="checkin-btn"
              :loading="loading"
              @click="handleCheckin"
            >
              立即签到
            </el-button>
          </div>

          <el-result
            v-if="checkinResult"
            :icon="checkinResult.success ? 'success' : 'error'"
            :title="checkinResult.success ? '签到成功' : '签到失败'"
            :sub-title="checkinResult.message"
          />
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
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="checkinTime" label="签到时间" width="170" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'present' ? 'success' : row.status === 'late' ? 'warning' : 'danger'" size="small">
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { submitCheckin, getMyAttendance } from '@/api/attendance'

const loading = ref(false)
const listLoading = ref(false)
const checkinCode = ref('')
const checkinResult = ref(null)
const attendanceList = ref([])

const attendanceRate = computed(() => {
  if (attendanceList.value.length === 0) return 0
  const present = attendanceList.value.filter((a) => a.status === 'present').length
  return Math.round((present / attendanceList.value.length) * 100)
})

const statusText = (status) => ({
  present: '已签到',
  late: '迟到',
  absent: '缺勤'
}[status] || status)

const handleCheckin = async () => {
  if (!checkinCode.value || checkinCode.value.length !== 6) {
    ElMessage.warning('请输入6位签到码')
    return
  }
  loading.value = true
  try {
    const res = await submitCheckin({ code: checkinCode.value })
    checkinResult.value = { success: true, message: `课程「${res.data?.courseName}」签到成功` }
    ElMessage.success('签到成功')
    fetchAttendance()
  } catch (e) {
    checkinResult.value = { success: false, message: e.response?.data?.msg || '签到码错误或已过期' }
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

onMounted(() => {
  fetchAttendance()
})
</script>

<style scoped>
.checkin-form {
  text-align: center;
  padding: 30px 0;
}

.code-input {
  max-width: 280px;
  margin: 0 auto 20px;
  font-size: 24px;
  letter-spacing: 8px;
  text-align: center;
}

.checkin-btn {
  width: 200px;
}
</style>
