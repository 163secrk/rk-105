import request from '@/utils/request'

export function getDashboardStats() {
  return request({
    url: '/api/dashboard/stats',
    method: 'get'
  })
}

export function getProjectProfit() {
  return request({
    url: '/api/dashboard/project-profit',
    method: 'get'
  })
}

export function getMonthlyProfit(params) {
  return request({
    url: '/api/dashboard/monthly-profit',
    method: 'get',
    params
  })
}

export function getProjectList() {
  return request({
    url: '/api/project/list',
    method: 'get'
  })
}
