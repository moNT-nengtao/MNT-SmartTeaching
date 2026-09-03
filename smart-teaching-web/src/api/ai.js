import request from '@/utils/request'

// AI 知识点答疑（学生模式）
export function aiAnswer(data) {
  return request({
    url: '/ai/answer',
    method: 'post',
    data
  })
}

// AI 作业评语生成（教师/管理员模式）
export function aiGenerateComment(data) {
  return request({
    url: '/ai/comment',
    method: 'post',
    data
  })
}

// AI 学业分析建议（教师/管理员，需选择学生；需组装成绩/考勤/作业数据并调用大模型，耗时较长）
export function aiAnalysis(studentId, message) {
  return request({
    url: `/ai/analysis/${studentId}`,
    method: 'get',
    params: { message },
    timeout: 180000
  })
}

// 通用对话（后端自动识别模式，作为流式失败时的兜底）
export function aiChat(data) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data
  })
}

// 会话列表（本人）
export function getAiSessions() {
  return request({
    url: '/ai/sessions',
    method: 'get'
  })
}

// 历史对话记录（兼容旧接口，返回本人会话列表）
export function getChatHistory(params) {
  return request({
    url: '/ai/history',
    method: 'get',
    params
  })
}

// 会话历史消息（校验归属）
export function getAiSessionMessages(sessionId) {
  return request({
    url: `/ai/session/${sessionId}/messages`,
    method: 'get'
  })
}

// 删除会话（校验归属）
export function deleteAiSession(sessionId) {
  return request({
    url: `/ai/session/${sessionId}`,
    method: 'delete'
  })
}

// 剩余次数查询
export function getRemainingCount() {
  return request({
    url: '/ai/remaining',
    method: 'get'
  })
}

// 学生列表（教师/管理员，供学业分析选人）
export function getAiStudents() {
  return request({
    url: '/ai/students',
    method: 'get'
  })
}
