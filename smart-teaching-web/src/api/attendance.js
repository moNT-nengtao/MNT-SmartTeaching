import request from '@/utils/request'

// 生成动态签到码
export function generateCheckinCode(data) {
  return request({
    url: '/attendance/generate',
    method: 'post',
    data
  })
}

// 学生签到提交
export function submitCheckin(data) {
  return request({
    url: '/attendance/submit',
    method: 'post',
    data
  })
}

// 实时签到名单
export function getCheckinList(sessionId) {
  return request({
    url: `/attendance/${sessionId}/list`,
    method: 'get'
  })
}

// 考勤统计
export function getAttendanceStats(params) {
  return request({
    url: '/attendance/stats',
    method: 'get',
    params
  })
}

// 考勤报表导出
export function exportAttendance(params) {
  return request({
    url: '/attendance/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 我的考勤记录
export function getMyAttendance(params) {
  return request({
    url: '/attendance/my',
    method: 'get',
    params
  })
}
