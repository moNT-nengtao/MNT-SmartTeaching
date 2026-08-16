import request from '@/utils/request'

// 学院列表
export function getCollegeList() {
  return request({
    url: '/org/college/list',
    method: 'get'
  })
}

// 专业列表（按学院）
export function getMajorList(collegeId) {
  return request({
    url: '/org/major/list',
    method: 'get',
    params: { collegeId }
  })
}

// 班级列表（按专业）
export function getClassList(majorId) {
  return request({
    url: '/org/class/list',
    method: 'get',
    params: { majorId }
  })
}

// 三级级联数据
export function getOrgTree() {
  return request({
    url: '/org/tree',
    method: 'get'
  })
}

// 新增学院/专业/班级
export function addOrg(data) {
  return request({
    url: '/org',
    method: 'post',
    data
  })
}

// 编辑学院/专业/班级
export function updateOrg(data) {
  return request({
    url: '/org',
    method: 'put',
    data
  })
}

// 删除学院/专业/班级（校验下级关联）
export function deleteOrg(id, type) {
  return request({
    url: `/org/${id}`,
    method: 'delete',
    params: { type }
  })
}

// 批量导入
export function batchImportOrg(data) {
  return request({
    url: '/org/batchImport',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 批量导出
export function batchExportOrg(params) {
  return request({
    url: '/org/batchExport',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
