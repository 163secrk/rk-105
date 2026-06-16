import request from '@/utils/request'

export function getTalentListApi() {
  return request({
    url: '/talent/list',
    method: 'get'
  })
}

export function getTalentApi(id) {
  return request({
    url: `/talent/${id}`,
    method: 'get'
  })
}

export function addTalentApi(data) {
  return request({
    url: '/talent',
    method: 'post',
    data
  })
}

export function updateTalentApi(data) {
  return request({
    url: '/talent',
    method: 'put',
    data
  })
}

export function updateTalentStatusApi(id, status) {
  return request({
    url: `/talent/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function deleteTalentApi(id) {
  return request({
    url: `/talent/${id}`,
    method: 'delete'
  })
}
