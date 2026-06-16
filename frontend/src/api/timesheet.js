import request from '@/utils/request'

export function getMyTimesheetListApi() {
  return request({
    url: '/timesheet/my-list',
    method: 'get'
  })
}

export function getMonthTimesheetApi(month) {
  return request({
    url: '/timesheet/month',
    method: 'get',
    params: { month }
  })
}

export function initMonthTimesheetApi(month) {
  return request({
    url: '/timesheet/init',
    method: 'post',
    params: { month }
  })
}

export function updateDayStatusApi(timesheetId, dayDate, status) {
  return request({
    url: '/timesheet/day-status',
    method: 'put',
    params: { timesheetId, dayDate, status }
  })
}

export function submitTimesheetApi(id) {
  return request({
    url: `/timesheet/submit/${id}`,
    method: 'post'
  })
}

export function getPendingListApi() {
  return request({
    url: '/timesheet/pending-list',
    method: 'get'
  })
}

export function getPmAllListApi() {
  return request({
    url: '/timesheet/pm-list',
    method: 'get'
  })
}

export function getTimesheetDetailApi(id) {
  return request({
    url: `/timesheet/detail/${id}`,
    method: 'get'
  })
}

export function approveTimesheetApi(id) {
  return request({
    url: `/timesheet/approve/${id}`,
    method: 'post'
  })
}

export function rejectTimesheetApi(id, reason) {
  return request({
    url: `/timesheet/reject/${id}`,
    method: 'post',
    data: { reason }
  })
}
