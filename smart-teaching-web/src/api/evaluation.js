import request from '@/utils/request'

// 提交评价
export function submitEvaluation(data) {
  return request({
    url: '/evaluation',
    method: 'post',
    data
  })
}

// 查看评价（教师看自己课程的）
export function getEvaluationList(params) {
  return request({
    url: '/evaluation/list',
    method: 'get',
    params
  })
}

// 教师评分榜单
export function getTeacherRanking(params) {
  return request({
    url: '/evaluation/ranking',
    method: 'get',
    params
  })
}

// 评价统计
export function getEvaluationStats(courseId) {
  return request({
    url: `/evaluation/stats/${courseId}`,
    method: 'get'
  })
}

// 可评价课程列表（学生）
export function getEvaluableCourses() {
  return request({
    url: '/evaluation/evaluable',
    method: 'get'
  })
}

// 课程评价预览（选课大厅用）
export function getCourseEvaluationPreview(courseId) {
  return request({
    url: `/evaluation/preview/${courseId}`,
    method: 'get'
  })
}
