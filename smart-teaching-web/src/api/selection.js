import request from '@/utils/request'

// 设置选课开放时间
export function setSelectionTime(data) {
  return request({
    url: '/selection/time',
    method: 'post',
    data
  })
}

// 获取选课开放配置
export function getSelectionConfig() {
  return request({
    url: '/selection/config',
    method: 'get'
  })
}

// 选课大厅课程列表
export function getSelectionCourseList(params) {
  return request({
    url: '/selection/course/list',
    method: 'get',
    params
  })
}

// 智能推荐课程
export function getRecommendCourses() {
  return request({
    url: '/selection/recommend',
    method: 'get'
  })
}

// 选课
export function selectCourse(courseId) {
  return request({
    url: `/selection/select/${courseId}`,
    method: 'post'
  })
}

// 退课
export function dropCourse(courseId) {
  return request({
    url: `/selection/drop/${courseId}`,
    method: 'delete'
  })
}

// 我的已选课程
export function getMyCourses() {
  return request({
    url: '/selection/my',
    method: 'get'
  })
}

// 课程选课名单
export function getCourseStudents(courseId) {
  return request({
    url: `/selection/${courseId}/students`,
    method: 'get'
  })
}

// 剩余名额
export function getRemainingQuota(courseId) {
  return request({
    url: `/selection/${courseId}/remaining`,
    method: 'get'
  })
}

// 热门预警
export function getHotWarning() {
  return request({
    url: '/selection/hotWarning',
    method: 'get'
  })
}
