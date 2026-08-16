import request from '@/utils/request'

// 成绩录入
export function enterScore(data) {
  return request({
    url: '/score/enter',
    method: 'post',
    data
  })
}

// 成绩修改
export function updateScore(data) {
  return request({
    url: '/score',
    method: 'put',
    data
  })
}

// 批量导入成绩
export function batchImportScore(data) {
  return request({
    url: '/score/batchImport',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 我的成绩（学生）
export function getMyScores(params) {
  return request({
    url: '/score/my',
    method: 'get',
    params
  })
}

// 课程成绩列表（教师录入用）
export function getCourseScoreList(courseId) {
  return request({
    url: `/score/course/${courseId}`,
    method: 'get'
  })
}

// 成绩统计
export function getScoreStats(params) {
  return request({
    url: '/score/stats',
    method: 'get',
    params
  })
}

// 异常成绩筛查
export function getAbnormalScores(params) {
  return request({
    url: '/score/abnormal',
    method: 'get',
    params
  })
}

// 成绩导出
export function exportScore(params) {
  return request({
    url: '/score/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
