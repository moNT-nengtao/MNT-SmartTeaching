import request from '@/utils/request'

// 发起签到会话（教师）：courseId + duration(分钟,上限20) + pattern(九宫格序列)
export function generateCheckinCode(data) {
  return request({
    url: '/attendance/generate',
    method: 'post',
    data
  })
}

// 学生签到提交：pattern(九宫格序列)
export function submitCheckin(data) {
  return request({
    url: '/attendance/submit',
    method: 'post',
    data
  })
}

// 签到会话详情（会话信息 + 学生名单 + 统计）
export function getSessionDetail(sessionId) {
  return request({
    url: `/attendance/${sessionId}/list`,
    method: 'get'
  })
}

// 教师当前活跃签到会话（无则返回 null，进入页面恢复展示）
export function getTeacherCurrentSession() {
  return request({
    url: '/attendance/teacher/current',
    method: 'get'
  })
}

// 学生当前待签到会话（无则返回 null，展示当前需签到课程）
export function getStudentCurrentSession() {
  return request({
    url: '/attendance/student/current',
    method: 'get'
  })
}

// 考勤报表导出
export function exportAttendance(sessionId) {
  return request({
    url: `/attendance/${sessionId}/export`,
    method: 'get',
    responseType: 'blob'
  })
}

// 教师修改考勤状态（仅迟到/请假/旷课，不允许改为考勤成功，历史会话不允许修改）
export function updateAttendanceStatus(recordId, status) {
  return request({
    url: `/attendance/record/${recordId}/status`,
    method: 'put',
    data: { status }
  })
}

// 教师手动签到（学生到场但无法自主签到，特殊状态留痕 status=5）
export function manualCheckin(recordId) {
  return request({
    url: `/attendance/record/${recordId}/manual`,
    method: 'put'
  })
}

// 历史考勤会话列表（教师/管理员，用于名单切换）
export function getAttendanceSessions(courseId) {
  return request({
    url: '/attendance/sessions',
    method: 'get',
    params: { courseId }
  })
}

// 结束签到会话（缺勤落定为旷课，联动生成旷课预警）
export function endAttendanceSession(sessionId) {
  return request({
    url: `/attendance/${sessionId}/end`,
    method: 'post'
  })
}

// 我的考勤记录
export function getMyAttendance() {
  return request({
    url: '/attendance/my',
    method: 'get'
  })
}

// 教师可发起签到的课程选项
export function getTeacherCourseOptions() {
  return request({
    url: '/attendance/courses',
    method: 'get'
  })
}
