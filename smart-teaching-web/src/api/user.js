import request from '@/utils/request'

// 用户列表（分页+搜索）
export function getUserList(params) {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

// 新增用户
export function addUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

// 编辑用户
export function updateUser(data) {
  return request({
    url: '/user',
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

// 禁用/启用用户
export function toggleUserStatus(id, status) {
  return request({
    url: `/user/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 分配角色
export function assignRole(id, role) {
  return request({
    url: `/user/${id}/role`,
    method: 'put',
    params: { role }
  })
}

// 批量导入用户
export function batchImportUser(data) {
  return request({
    url: '/user/batchImport',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
