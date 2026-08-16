import request from '@/utils/request'

// 周课表数据
export function getWeekSchedule(params) {
  return request({
    url: '/schedule/week',
    method: 'get',
    params
  })
}

// 课程颜色设置
export function setCourseColor(courseId, color) {
  return request({
    url: `/schedule/color/${courseId}`,
    method: 'put',
    params: { color }
  })
}

// 课程收藏
export function toggleCourseFavorite(courseId) {
  return request({
    url: `/schedule/favorite/${courseId}`,
    method: 'put'
  })
}

// 课程备忘
export function setCourseMemo(courseId, memo) {
  return request({
    url: `/schedule/memo/${courseId}`,
    method: 'put',
    data: { memo }
  })
}

// 下一节课
export function getNextClass() {
  return request({
    url: '/schedule/next',
    method: 'get'
  })
}

// 上课提醒设置
export function setReminderConfig(data) {
  return request({
    url: '/schedule/reminder',
    method: 'put',
    data
  })
}
