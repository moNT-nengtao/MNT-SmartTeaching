import request from '@/utils/request'

// AI 知识点答疑
export function aiAnswer(data) {
  return request({
    url: '/ai/answer',
    method: 'post',
    data
  })
}

// AI 作业评语生成
export function aiGenerateComment(data) {
  return request({
    url: '/ai/comment',
    method: 'post',
    data
  })
}

// AI 学业分析建议
export function aiAnalysis(studentId) {
  return request({
    url: `/ai/analysis/${studentId}`,
    method: 'get'
  })
}

// 剩余次数查询
export function getRemainingCount() {
  return request({
    url: '/ai/remaining',
    method: 'get'
  })
}

// 历史对话记录
export function getChatHistory(params) {
  return request({
    url: '/ai/history',
    method: 'get',
    params
  })
}
