import request from '@/utils/request'

// 课程列表
export function getCourseList(params) {
  return request({
    url: '/course/list',
    method: 'get',
    params
  })
}

// 新增课程
export function addCourse(data) {
  return request({
    url: '/course',
    method: 'post',
    data
  })
}

// 编辑课程
export function updateCourse(data) {
  return request({
    url: '/course',
    method: 'put',
    data
  })
}

// 删除课程
export function deleteCourse(id) {
  return request({
    url: `/course/${id}`,
    method: 'delete'
  })
}

// 排课列表
export function getScheduleList(params) {
  return request({
    url: '/course/schedule/list',
    method: 'get',
    params
  })
}

// 新增排课
export function addSchedule(data) {
  return request({
    url: '/course/schedule',
    method: 'post',
    data
  })
}

// 编辑排课
export function updateSchedule(data) {
  return request({
    url: '/course/schedule',
    method: 'put',
    data
  })
}

// 删除排课
export function deleteSchedule(id) {
  return request({
    url: `/course/schedule/${id}`,
    method: 'delete'
  })
}

// 排课冲突校验
export function checkScheduleConflict(data) {
  return request({
    url: '/course/schedule/checkConflict',
    method: 'post',
    data
  })
}

// 批量排课
export function batchSchedule(data) {
  return request({
    url: '/course/schedule/batch',
    method: 'post',
    data
  })
}

// 课表导出
export function exportSchedule(params) {
  return request({
    url: '/course/schedule/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
