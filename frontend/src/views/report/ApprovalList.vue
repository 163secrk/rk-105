<template>
  <div class="page-container">
    <a-card :bordered="false" class="page-card">
      <template #title>
        <div class="card-header">
          <div class="card-title">
            <icon-file class="title-icon" />
            <span>工时审批</span>
          </div>
          <div class="header-actions">
            <a-radio-group v-model="statusFilter" type="button" @change="fetchList">
              <a-radio value="SUBMITTED">待审批</a-radio              >
              <a-radio value="ALL">全部</a-radio>
            </a-radio-group>
          </div>
        </div>
      </template>

      <a-table
        :data="filteredList"
        :loading="listLoading"
        :pagination="false"
        bordered
        row-key="id"
      >
        <template #columns>
          <a-table-column title="员工姓名" data-index="userName" :width="120" />
          <a-table-column title="项目名称" data-index="projectName" :width="200" />
          <a-table-column title="月份" :width="100">
            <template #cell="{ record }">
              {{ formatMonth(record.month) }}
            </template>
          </a-table-column>
          <a-table-column title="状态" :width="100">
            <template #cell="{ record }">
              <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="提交时间" :width="170">
            <template #cell="{ record }">
              {{ formatTime(record.updateTime) }}
            </template>
          </a-table-column>
          <a-table-column title="审批人" :width="100">
            <template #cell="{ record }">
              {{ record.approverName || '-' }}
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="220" fixed="right">
            <template #cell="{ record }">
              <a-space>
                <a-button type="text" size="small" @click="openDetail(record)">
                  查看详情
                </a-button>
                <a-button
                  v-if="record.status === 'SUBMITTED'"
                  type="text"
                  size="small"
                  status="success"
                  @click="handleApprove(record)"
                >
                  通过
                </a-button>
                <a-button
                  v-if="record.status === 'SUBMITTED'"
                  type="text"
                  size="small"
                  status="danger"
                  @click="openRejectModal(record)"
                >
                  驳回
                </a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="detailVisible"
      :title="`${detailData?.userName || ''} - ${formatMonth(detailData?.month)} 工时详情`"
      :footer="false"
      width="700px"
    >
      <div v-if="detailLoading" style="text-align: center; padding: 40px 0;">
        <a-spin />
      </div>
      <div v-else>
        <div class="detail-info">
          <a-descriptions :column="2" size="small" bordered>
            <a-descriptions-item label="员工">{{ detailData?.userName }}</a-descriptions-item>
            <a-descriptions-item label="项目">{{ detailData?.projectName }}</a-descriptions-item>
            <a-descriptions-item label="月份">{{ formatMonth(detailData?.month) }}</a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="statusColor(detailData?.status)">{{ statusText(detailData?.status) }}</a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <div class="detail-legend">
          <span class="legend-item"><span class="dot dot-normal"></span>正常出勤</span>
          <span class="legend-item"><span class="dot dot-overtime"></span>加班</span>
          <span class="legend-item"><span class="dot dot-leave"></span>请假</span>
        </div>

        <div class="mini-calendar">
          <div class="mini-header">
            <div v-for="w in weekLabels" :key="w" class="mini-header-cell">{{ w }}</div>
          </div>
          <div class="mini-body">
            <div
              v-for="(cell, idx) in detailCalendarCells"
              :key="idx"
              class="mini-day-cell"
              :class="{ empty: !cell.day }"
            >
              <div v-if="cell.day" class="mini-day-content">
                <span class="mini-day-num">{{ cell.day }}</span>
                <span v-if="cell.status" class="dot" :class="dotClass(cell.status)"></span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="detailData?.status === 'SUBMITTED'" class="detail-actions">
          <a-space>
            <a-button type="primary" status="success" @click="handleApprove(detailData); detailVisible = false">
              通过
            </a-button>
            <a-button status="danger" @click="detailVisible = false; openRejectModal(detailData)">
              驳回
            </a-button>
          </a-space>
        </div>

        <div v-if="detailData?.status === 'REJECTED' && detailData?.rejectReason" class="reject-reason">
          <a-alert type="error">
            驳回原因：{{ detailData.rejectReason }}
          </a-alert>
        </div>
      </div>
    </a-modal>

    <a-modal
      v-model:visible="rejectVisible"
      title="驳回工时单"
      @ok="handleReject"
      :ok-loading="rejectLoading"
      ok-text="确认驳回"
    >
      <a-form layout="vertical">
        <a-form-item label="驳回原因">
          <a-textarea
            v-model="rejectReason"
            :auto-size="{ minRows: 3, maxRows: 5 }"
            placeholder="请输入驳回原因"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { IconFile } from '@arco-design/web-vue/es/icon'
import {
  getPendingListApi,
  getPmAllListApi,
  getTimesheetDetailApi,
  approveTimesheetApi,
  rejectTimesheetApi
} from '@/api/timesheet'

const weekLabels = ['一', '二', '三', '四', '五', '六', '日']

const listLoading = ref(false)
const timesheetList = ref([])
const statusFilter = ref('SUBMITTED')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)
const detailDays = ref([])

const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectLoading = ref(false)
const rejectTarget = ref(null)

const filteredList = computed(() => {
  if (statusFilter.value === 'ALL') {
    return timesheetList.value
  }
  return timesheetList.value.filter(t => t.status === statusFilter.value)
})

const detailCalendarCells = computed(() => {
  if (!detailData.value) return []
  const month = detailData.value.month
  if (!month) return []
  const [y, m] = month.split('-').map(Number)
  const firstDay = new Date(y, m - 1, 1)
  let startWeekday = firstDay.getDay()
  if (startWeekday === 0) startWeekday = 7
  startWeekday -= 1

  const daysInMonth = new Date(y, m, 0).getDate()

  const dayMap = {}
  detailDays.value.forEach(d => {
    if (d.dayDate) {
      const dateStr = typeof d.dayDate === 'string'
        ? d.dayDate
        : `${d.dayDate.getFullYear()}-${String(d.dayDate.getMonth() + 1).padStart(2, '0')}-${String(d.dayDate.getDate()).padStart(2, '0')}`
      dayMap[dateStr] = d.status
    }
  })

  const cells = []
  for (let i = 0; i < startWeekday; i++) {
    cells.push({ day: null, status: null })
  }
  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    cells.push({
      day: d,
      status: dayMap[dateStr] || null
    })
  }
  return cells
})

function dotClass(status) {
  const map = { NORMAL: 'dot-normal', OVERTIME: 'dot-overtime', LEAVE: 'dot-leave' }
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

function formatMonth(month) {
  if (!month) return '-'
  const [y, m] = month.split('-')
  return `${y}年${m}月`
}

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

async function fetchList() {
  listLoading.value = true
  try {
    if (statusFilter.value === 'SUBMITTED') {
      const res = await getPendingListApi()
      timesheetList.value = res.data || []
    } else {
      const res = await getPmAllListApi()
      timesheetList.value = res.data || []
    }
  } catch (e) {
    timesheetList.value = []
  } finally {
    listLoading.value = false
  }
}

async function openDetail(record) {
  detailData.value = record
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getTimesheetDetailApi(record.id)
    detailDays.value = res.data.days || []
    detailData.value = res.data.timesheet
  } catch (e) {
    Message.error('获取详情失败')
  } finally {
    detailLoading.value = false
  }
}

function handleApprove(record) {
  Modal.confirm({
    title: '确认通过',
    content: `确定通过 ${record.userName} ${formatMonth(record.month)} 的工时单吗？通过后将锁定，不可再修改。`,
    onOk: async () => {
      try {
        await approveTimesheetApi(record.id)
        Message.success('审批通过')
        fetchList()
      } catch (e) {
        Message.error(e.message || '审批失败')
      }
    }
  })
}

function openRejectModal(record) {
  rejectTarget.value = record
  rejectReason.value = ''
  rejectVisible.value = true
}

async function handleReject() {
  if (!rejectTarget.value) return
  rejectLoading.value = true
  try {
    await rejectTimesheetApi(rejectTarget.value.id, rejectReason.value)
    Message.success('已驳回')
    rejectVisible.value = false
    fetchList()
  } catch (e) {
    Message.error(e.message || '驳回失败')
  } finally {
    rejectLoading.value = false
  }
}

onMounted(() => {
  fetchList()
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
  gap: 12px;
}

.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
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

.detail-info {
  margin-bottom: 16px;
}

.detail-legend {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: #f7f8fa;
  border-radius: 6px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #4e5969;
}

.mini-calendar {
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  overflow: hidden;
}

.mini-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: #f2f3f5;
}

.mini-header-cell {
  padding: 8px 0;
  text-align: center;
  font-size: 12px;
  font-weight: 600;
  color: #4e5969;
  border-bottom: 1px solid #e5e6eb;
}

.mini-body {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.mini-day-cell {
  min-height: 48px;
  padding: 4px;
  border-bottom: 1px solid #e5e6eb;
  border-right: 1px solid #e5e6eb;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mini-day-cell:nth-child(7n) {
  border-right: none;
}

.mini-day-cell.empty {
  background: #fafafa;
}

.mini-day-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.mini-day-num {
  font-size: 12px;
  color: #1d2129;
}

.detail-actions {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e5e6eb;
  display: flex;
  justify-content: flex-end;
}

.reject-reason {
  margin-top: 12px;
}
</style>
