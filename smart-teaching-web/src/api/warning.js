import request from '@/utils/request'

// 预警列表
export function getWarningList(params) {
  return request({
    url: '/warning/list',
    method: 'get',
    params
  })
}

// 预警详情
export function getWarningDetail(id) {
  return request({
    url: `/warning/${id}`,
    method: 'get'
  })
}

// 预警报告导出
export function exportWarningReport(params) {
  return request({
    url: '/warning/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 预警统计
export function getWarningStats(params) {
  return request({
    url: '/warning/stats',
    method: 'get',
    params
  })
}
