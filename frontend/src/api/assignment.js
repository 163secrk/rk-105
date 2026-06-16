import request from '@/utils/request'

export function getAssignmentListApi() {
  return request({
    url: '/assignment/list',
    method: 'get'
  })
}

export function getAssignmentApi(id) {
  return request({
    url: `/assignment/${id}`,
    method: 'get'
  })
}

export function getActiveAssignmentsByTalentApi(talentId) {
  return request({
    url: `/assignment/talent/${talentId}/active`,
    method: 'get'
  })
}

export function checkConflictApi(params) {
  return request({
    url: '/assignment/check-conflict',
    method: 'get',
    params
  })
}

export function suggestPriceApi(params) {
  return request({
    url: '/assignment/suggest-price',
    method: 'get',
    params
  })
}

export function addAssignmentApi(data) {
  return request({
    url: '/assignment',
    method: 'post',
    data
  })
}

export function updateAssignmentApi(data) {
  return request({
    url: '/assignment',
    method: 'put',
    data
  })
}

export function deleteAssignmentApi(id) {
  return request({
    url: `/assignment/${id}`,
    method: 'delete'
  })
}
