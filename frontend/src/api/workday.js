import request from '@/utils/request'

export function getWorkdayListApi() {
  return request({
    url: '/finance/workday/list',
    method: 'get'
  })
}

export function getWorkdayByMonthApi(month) {
  return request({
    url: `/finance/workday/month/${month}`,
    method: 'get'
  })
}

export function saveWorkdayApi(data) {
  return request({
    url: '/finance/workday/save',
    method: 'post',
    data
  })
}

export function deleteWorkdayApi(id) {
  return request({
    url: `/finance/workday/${id}`,
    method: 'delete'
  })
}

export function generateYearWorkdaysApi(year) {
  return request({
    url: '/finance/workday/generate',
    method: 'post',
    params: { year }
  })
}
