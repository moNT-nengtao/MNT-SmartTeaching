import request from '@/utils/request'

// 公告列表
export function getNoticeList(params) {
  return request({
    url: '/notice/list',
    method: 'get',
    params
  })
}

// 发布公告
export function publishNotice(data) {
  return request({
    url: '/notice',
    method: 'post',
    data
  })
}

// 编辑公告
export function updateNotice(data) {
  return request({
    url: '/notice',
    method: 'put',
    data
  })
}

// 撤回公告
export function withdrawNotice(id) {
  return request({
    url: `/notice/${id}/withdraw`,
    method: 'put'
  })
}

// 置顶/取消置顶
export function toggleNoticeTop(id, isTop) {
  return request({
    url: `/notice/${id}/top`,
    method: 'put',
    params: { isTop }
  })
}

// 公告详情
export function getNoticeDetail(id) {
  return request({
    url: `/notice/${id}`,
    method: 'get'
  })
}

// 标记已读
export function markNoticeRead(id) {
  return request({
    url: `/notice/${id}/read`,
    method: 'put'
  })
}

// 未读数量
export function getUnreadCount() {
  return request({
    url: '/notice/unreadCount',
    method: 'get'
  })
}
