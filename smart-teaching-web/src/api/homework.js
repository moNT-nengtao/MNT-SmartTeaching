import request from '@/utils/request'
import { ElMessage } from 'element-plus'

// 教师/管理员 作业列表
export function getHomeworkList(params) {
  return request({
    url: '/homework/list',
    method: 'get',
    params
  })
}

// 学生 我的作业列表
export function getMyHomework(params) {
  return request({
    url: '/homework/my',
    method: 'get',
    params
  })
}

// 作业详情
export function getHomeworkDetail(id) {
  return request({
    url: `/homework/${id}`,
    method: 'get'
  })
}

// 发布作业（FormData，附件字段名 file）
export function publishHomework(data) {
  return request({
    url: '/homework',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 编辑作业（FormData）
export function updateHomework(data) {
  return request({
    url: '/homework',
    method: 'put',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 删除作业
export function deleteHomework(id) {
  return request({
    url: `/homework/${id}`,
    method: 'delete'
  })
}

// 查看作业提交列表
export function getSubmissionList(homeworkId) {
  return request({
    url: `/homework/${homeworkId}/submissions`,
    method: 'get'
  })
}

// 学生提交作业（FormData）
export function submitHomework(data) {
  return request({
    url: '/homework/submit',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 教师批改作业
export function gradeHomework(data) {
  return request({
    url: '/homework/grade',
    method: 'put',
    data
  })
}

// 管理员作业统计
export function getHomeworkStats() {
  return request({
    url: '/homework/stats',
    method: 'get'
  })
}

/**
 * 下载附件（带404检测）
 * 文件不存在时弹出提示，而非跳转404页面
 * @param {string} url - 附件访问路径，如 /files/homework/xxx.pdf
 * @param {string} filename - 下载时保存的文件名
 */
export function downloadAttachment(url, filename) {
  if (!url) {
    ElMessage.warning('附件不存在')
    return
  }
  fetch(url, { method: 'GET' })
    .then(response => {
      if (!response.ok) {
        ElMessage.error('文件不存在或已被删除')
        return null
      }
      return response.blob()
    })
    .then(blob => {
      if (!blob) return
      const downloadUrl = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = filename || '附件'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(downloadUrl)
    })
    .catch(() => {
      ElMessage.error('下载失败，请稍后重试')
    })
}
