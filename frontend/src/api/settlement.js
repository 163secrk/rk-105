import request from '@/utils/request'

export function getSettlementListApi() {
  return request({
    url: '/finance/settlement/list',
    method: 'get'
  })
}

export function getSettlementDetailApi(id) {
  return request({
    url: `/finance/settlement/detail/${id}`,
    method: 'get'
  })
}

export function generateSettlementApi(month) {
  return request({
    url: '/finance/settlement/generate',
    method: 'post',
    params: { month }
  })
}

export function deleteSettlementApi(id) {
  return request({
    url: `/finance/settlement/${id}`,
    method: 'delete'
  })
}
