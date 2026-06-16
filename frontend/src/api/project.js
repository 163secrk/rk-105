import request from '@/utils/request'

export function getProjectListApi() {
  return request({
    url: '/project/list',
    method: 'get'
  })
}

export function getProjectApi(id) {
  return request({
    url: `/project/${id}`,
    method: 'get'
  })
}

export function addProjectApi(data) {
  return request({
    url: '/project',
    method: 'post',
    data
  })
}

export function updateProjectApi(data) {
  return request({
    url: '/project',
    method: 'put',
    data
  })
}

export function deleteProjectApi(id) {
  return request({
    url: `/project/${id}`,
    method: 'delete'
  })
}
