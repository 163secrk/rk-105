import request from '@/utils/request'

export function getMenuListApi() {
  return request({
    url: '/menu/list',
    method: 'get'
  })
}
