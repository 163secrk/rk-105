<template>
  <div class="page-container">
    <a-card :bordered="false" class="page-card">
      <template #title>
        <div class="card-header">
          <div class="card-title">
            <icon-calendar class="title-icon" />
            <span>工时填报</span>
          </div>
          <div class="header-actions">
            <a-button type="text" @click="prevMonth">
              <template #icon><icon-left /></template>
            </a-button>
            <span class="month-label">{{ currentMonthLabel }}</span>
            <a-button type="text" @click="nextMonth">
              <template #icon><icon-right /></template>
            </a-button>
            <a-button
              type="primary"
              :loading="submitting"
              :disabled="!canSubmit"
              @click="handleSubmit"
            >
              <template #icon><icon-send /></template>
              提交当月工时单
            </a-button>
          </div>
        </div>
      </template>

      <div class="calendar-wrapper" v-if="!loading">
        <div class="legend-bar">
          <span class="legend-item"><span class="dot dot-normal"></span>正常出勤</span>
          <span class="legend-item"><span class="dot dot-overtime"></span>加班</span>
          <span class="legend-item"><span class="dot dot-leave"></span>请假</span>
          <span v-if="timesheet" class="status-tag">
            <a-tag :color="statusColor(timesheet.status)">{{ statusText(timesheet.status) }}</a-tag>
          </span>
        </div>

        <div class="calendar-grid">
          <div class="calendar-header">
            <div v-for="w in weekLabels" :key="w" class="header-cell">{{ w }}</div>
          </div>
          <div class="calendar-body">
            <div
              v-for="(cell, idx) in calendarCells"
              :key="idx"
              class="day-cell"
              :class="{
                'empty': !cell.day,
                'today': cell.isToday,
                'readonly': isReadonly,
                'selected': selectedDay === cell.dateStr
              }"
              @click="cell.day && handleDayClick(cell)"
            >
              <div v-if="cell.day" class="day-content">
                <span class="day-number">{{ cell.day }}</span>
                <span
                  v-if="cell.status"
                  class="dot"
                  :class="dotClass(cell.status)"
                ></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="loading-wrapper">
        <a-spin />
      </div>
    </a-card>

    <a-modal
      v-model:visible="statusModalVisible"
      :title="`设置 ${selectedDateLabel} 状态`"
      :footer="false"
      width="360px"
    >
      <div class="status-options">
        <div
          class="status-option"
          :class="{ active: currentDayStatus === 'NORMAL' }"
          @click="selectStatus('NORMAL')"
        >
          <span class="dot dot-normal"></span>
          <span>正常出勤</span>
        </div>
        <div
          class="status-option"
          :class="{ active: currentDayStatus === 'OVERTIME' }"
          @click="selectStatus('OVERTIME')"
        >
          <span class="dot dot-overtime"></span>
          <span>加班</span>
        </div>
        <div
          class="status-option"
          :class="{ active: currentDayStatus === 'LEAVE' }"
          @click="selectStatus('LEAVE')"
        >
          <span class="dot dot-leave"></span>
          <span>请假</span>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import {
  IconCalendar,
  IconLeft,
  IconRight,
  IconSend
} from '@arco-design/web-vue/es/icon'
import {
  getMonthTimesheetApi,
  initMonthTimesheetApi,
  updateDayStatusApi,
  submitTimesheetApi
} from '@/api/timesheet'

const weekLabels = ['一', '二', '三', '四', '五', '六', '日']

const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)
const loading = ref(false)
const submitting = ref(false)
const timesheet = ref(null)
const days = ref([])
const statusModalVisible = ref(false)
const selectedDay = ref('')
const currentDayStatus = ref('NORMAL')

const currentMonthLabel = computed(() => {
  return `${currentYear.value}年${String(currentMonth.value).padStart(2, '0')}月`
})

const currentMonthStr = computed(() => {
  return `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}`
})

const isReadonly = computed(() => {
  return timesheet.value && timesheet.value.status === 'APPROVED'
})

const canSubmit = computed(() => {
  return timesheet.value
    && timesheet.value.status === 'DRAFT'
    && days.value.length > 0
})

const selectedDateLabel = computed(() => {
  if (!selectedDay.value) return ''
  const parts = selectedDay.value.split('-')
  return `${parts[0]}年${parts[1]}月${parts[2]}日`
})

const calendarCells = computed(() => {
  const y = currentYear.value
  const m = currentMonth.value
  const firstDay = new Date(y, m - 1, 1)
  let startWeekday = firstDay.getDay()
  if (startWeekday === 0) startWeekday = 7
  startWeekday -= 1

  const daysInMonth = new Date(y, m, 0).getDate()
  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

  const dayMap = {}
  days.value.forEach(d => {
    if (d.dayDate) {
      const dateStr = typeof d.dayDate === 'string'
        ? d.dayDate
        : `${d.dayDate.getFullYear()}-${String(d.dayDate.getMonth() + 1).padStart(2, '0')}-${String(d.dayDate.getDate()).padStart(2, '0')}`
      dayMap[dateStr] = d.status
    }
  })

  const cells = []
  for (let i = 0; i < startWeekday; i++) {
    cells.push({ day: null, dateStr: '', status: null, isToday: false })
  }
  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    cells.push({
      day: d,
      dateStr,
      status: dayMap[dateStr] || null,
      isToday: dateStr === todayStr
    })
  }
  return cells
})

function dotClass(status) {
  const map = {
    NORMAL: 'dot-normal',
    OVERTIME: 'dot-overtime',
    LEAVE: 'dot-leave'
  }
  return map[status] || ''
}

function statusText(status) {
  const map = { DRAFT: '草稿', SUBMITTED: '待审批', APPROVED: '已通过', REJECTED: '已驳回' }
  return map[status] || status || '-'
}

function statusColor(status) {
  const map = { DRAFT: 'gray', SUBMITTED: 'blue', APPROVED: 'green', REJECTED: 'red' }
  return map[status] || 'gray'
}

function prevMonth() {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value--
  } else {
    currentMonth.value--
  }
}

function nextMonth() {
  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value++
  } else {
    currentMonth.value++
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getMonthTimesheetApi(currentMonthStr.value)
    if (res.data.timesheet) {
      timesheet.value = res.data.timesheet
      days.value = res.data.days || []
    } else {
      timesheet.value = null
      days.value = []
    }
  } catch (e) {
    timesheet.value = null
    days.value = []
  } finally {
    loading.value = false
  }
}

async function initMonth() {
  loading.value = true
  try {
    const res = await initMonthTimesheetApi(currentMonthStr.value)
    timesheet.value = res.data.timesheet
    days.value = res.data.days || []
  } catch (e) {
    Message.error('初始化工时单失败')
  } finally {
    loading.value = false
  }
}

function handleDayClick(cell) {
  if (isReadonly.value) return
  if (!timesheet.value) {
    initMonth().then(() => {
      openStatusModal(cell)
    })
    return
  }
  openStatusModal(cell)
}

function openStatusModal(cell) {
  selectedDay.value = cell.dateStr
  currentDayStatus.value = cell.status || 'NORMAL'
  statusModalVisible.value = true
}

async function selectStatus(status) {
  if (!timesheet.value) return
  currentDayStatus.value = status
  try {
    await updateDayStatusApi(timesheet.value.id, selectedDay.value, status)
    const day = days.value.find(d => {
      const dateStr = typeof d.dayDate === 'string'
        ? d.dayDate
        : `${d.dayDate.getFullYear()}-${String(d.dayDate.getMonth() + 1).padStart(2, '0')}-${String(d.dayDate.getDate()).padStart(2, '0')}`
      return dateStr === selectedDay.value
    })
    if (day) {
      day.status = status
    }
    statusModalVisible.value = false
  } catch (e) {
    Message.error('更新状态失败')
  }
}

async function handleSubmit() {
  if (!timesheet.value) return
  Modal.confirm({
    title: '确认提交',
    content: `确定要提交 ${currentMonthLabel.value} 的工时单吗？提交后将等待PM审批。`,
    onOk: async () => {
      submitting.value = true
      try {
        await submitTimesheetApi(timesheet.value.id)
        Message.success('提交成功，等待审批')
        timesheet.value.status = 'SUBMITTED'
      } catch (e) {
        Message.error(e.message || '提交失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

watch(currentMonthStr, () => {
  fetchData()
})

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  width: 100%;
}

.page-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.title-icon {
  color: #165DFF;
  font-size: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.month-label {
  font-size: 16px;
  font-weight: 600;
  min-width: 120px;
  text-align: center;
  color: #1d2129;
}

.legend-bar {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f7f8fa;
  border-radius: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #4e5969;
}

.status-tag {
  margin-left: auto;
}

.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot-normal {
  background-color: #00b42a;
}

.dot-overtime {
  background-color: #165DFF;
}

.dot-leave {
  background-color: #f53f3f;
}

.calendar-wrapper {
  min-height: 500px;
}

.calendar-grid {
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  overflow: hidden;
}

.calendar-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: #f2f3f5;
}

.header-cell {
  padding: 12px 0;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #4e5969;
  border-bottom: 1px solid #e5e6eb;
}

.header-cell:not(:last-child) {
  border-right: 1px solid #e5e6eb;
}

.calendar-body {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.day-cell {
  min-height: 90px;
  padding: 8px;
  border-bottom: 1px solid #e5e6eb;
  border-right: 1px solid #e5e6eb;
  cursor: pointer;
  transition: background 0.15s;
  position: relative;
}

.day-cell:nth-child(7n) {
  border-right: none;
}

.day-cell.empty {
  cursor: default;
  background: #fafafa;
}

.day-cell:not(.empty):hover {
  background: #e8f3ff;
}

.day-cell.today {
  background: #f0f7ff;
}

.day-cell.today .day-number {
  background: #165DFF;
  color: #fff;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.day-cell.readonly {
  cursor: not-allowed;
}

.day-cell.readonly:not(.empty):hover {
  background: transparent;
}

.day-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.day-number {
  font-size: 14px;
  color: #1d2129;
  font-weight: 500;
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}

.status-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px 0;
}

.status-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 2px solid #e5e6eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
  font-size: 14px;
}

.status-option:hover {
  border-color: #165DFF;
  background: #f0f7ff;
}

.status-option.active {
  border-color: #165DFF;
  background: #e8f3ff;
  font-weight: 600;
}
</style>
