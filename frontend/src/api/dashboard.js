import request from '@/utils/request'

export function getDashboardStats() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}

export function getProjectProfit() {
  return request({
    url: '/dashboard/project-profit',
    method: 'get'
  })
}

export function getMonthlyProfit(params) {
  return request({
    url: '/dashboard/monthly-profit',
    method: 'get',
    params
  })
}

export function getProjectList() {
  return request({
    url: '/project/list',
    method: 'get'
  })
}
