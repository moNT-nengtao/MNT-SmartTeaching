import request from '@/utils/request'

// 问题列表（按课程分区）
export function getQuestionList(params) {
  return request({
    url: '/qa/list',
    method: 'get',
    params
  })
}

// 问题详情
export function getQuestionDetail(id) {
  return request({
    url: `/qa/${id}`,
    method: 'get'
  })
}

// 发布问题
export function publishQuestion(data) {
  return request({
    url: '/qa',
    method: 'post',
    data
  })
}

// 回复问题
export function replyQuestion(data) {
  return request({
    url: '/qa/reply',
    method: 'post',
    data
  })
}

// 点赞
export function likeQuestion(id) {
  return request({
    url: `/qa/${id}/like`,
    method: 'put'
  })
}

// 点赞回复
export function likeReply(replyId) {
  return request({
    url: `/qa/reply/${replyId}/like`,
    method: 'put'
  })
}

// 置顶问题
export function toggleQuestionTop(id, isTop) {
  return request({
    url: `/qa/${id}/top`,
    method: 'put',
    params: { isTop }
  })
}

// 获取标签列表
export function getTagList() {
  return request({
    url: '/qa/tags',
    method: 'get'
  })
}
