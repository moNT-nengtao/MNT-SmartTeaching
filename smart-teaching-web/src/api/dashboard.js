import request from '@/utils/request'

// 管理员全局统计
export function getAdminDashboard() {
  return request({
    url: '/dashboard/admin',
    method: 'get'
  })
}

// 教师授课数据
export function getTeacherDashboard() {
  return request({
    url: '/dashboard/teacher',
    method: 'get'
  })
}

// 学生学业数据（雷达图/绩点趋势）
export function getStudentDashboard() {
  return request({
    url: '/dashboard/student',
    method: 'get'
  })
}
