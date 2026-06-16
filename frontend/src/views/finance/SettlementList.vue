<template>
  <div class="settlement-page">
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="section-title">
          <icon-file class="section-icon" />
          <span>对账单管理</span>
        </div>
        <div class="section-desc">按月生成对账单，汇总已审批工时并计算结算金额</div>
      </div>
      <div class="toolbar-right">
        <a-space>
          <a-month-picker
            v-model="generateMonth"
            placeholder="选择生成月份"
            style="width: 200px"
            value-format="YYYY-MM"
          />
          <a-button type="primary" :loading="generating" @click="handleGenerate">
            <template #icon><icon-send /></template>
            生成对账单
          </a-button>
        </a-space>
      </div>
    </div>

    <a-table
      :data="settlementList"
      :loading="loading"
      :pagination="false"
      row-key="id"
      bordered
    >
      <template #columns>
        <a-table-column title="账单编号" data-index="settlementNo" width="200">
          <template #cell="{ record }">
            <span class="settlement-no">{{ record.settlementNo }}</span>
          </template>
        </a-table-column>
        <a-table-column title="账单月份" data-index="month" width="130">
          <template #cell="{ record }">
            <a-tag color="arcoblue">{{ formatMonth(record.month) }}</a-tag>
          </template>
        </a-table-column>
        <a-table-column title="法定工作日" data-index="workdayCount" width="120">
          <template #cell="{ record }">
            <span>{{ record.workdayCount }} 天</span>
          </template>
        </a-table-column>
        <a-table-column title="明细条数" data-index="itemCount" width="110">
          <template #cell="{ record }">
            <a-tag color="green">{{ record.itemCount }} 条</a-tag>
          </template>
        </a-table-column>
        <a-table-column title="结算总金额" width="160">
          <template #cell="{ record }">
            <span class="amount-total">¥ {{ formatAmount(record.totalAmount) }}</span>
          </template>
        </a-table-column>
        <a-table-column title="状态" data-index="status" width="110">
          <template #cell="{ record }">
            <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
          </template>
        </a-table-column>
        <a-table-column title="创建人" data-index="creatorName" width="110">
          <template #cell="{ record }">
            <span>{{ record.creatorName || '-' }}</span>
          </template>
        </a-table-column>
        <a-table-column title="生成时间" data-index="createTime" width="180">
          <template #cell="{ record }">
            <span>{{ formatDateTime(record.createTime) }}</span>
          </template>
        </a-table-column>
        <a-table-column title="操作" width="160" fixed="right">
          <template #cell="{ record }">
            <a-space>
              <a-button type="text" size="small" @click="goDetail(record)">
                <template #icon><icon-eye /></template>
                查看详情
              </a-button>
              <a-button type="text" status="danger" size="small" @click="handleDelete(record)">
                <template #icon><icon-delete /></template>
                删除
              </a-button>
            </a-space>
          </template>
        </a-table-column>
      </template>
      <template #empty>
        <a-empty description="暂无对账单，选择月份后点击生成对账单开始创建" />
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message, Modal } from '@arco-design/web-vue'
import {
  IconFile,
  IconSend,
  IconEye,
  IconDelete
} from '@arco-design/web-vue/es/icon'
import {
  getSettlementListApi,
  generateSettlementApi,
  deleteSettlementApi
} from '@/api/settlement'

const router = useRouter()
const loading = ref(false)
const generating = ref(false)
const settlementList = ref([])
const now = new Date()
const generateMonth = ref(
  `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
)

function formatMonth(month) {
  if (!month) return '-'
  const parts = month.split('-')
  return `${parts[0]}年${parts[1]}月`
}

function formatAmount(amount) {
  if (amount == null) return '0.00'
  const num = parseFloat(amount)
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatDateTime(dt) {
  if (!dt) return '-'
  if (typeof dt === 'string') return dt.replace('T', ' ').substring(0, 19)
  const d = new Date(dt)
  if (isNaN(d.getTime())) return String(dt)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function statusText(status) {
  const map = { GENERATED: '已生成', CONFIRMED: '已确认', PAID: '已支付' }
  return map[status] || status || '-'
}

function statusColor(status) {
  const map = { GENERATED: 'blue', CONFIRMED: 'green', PAID: 'cyan' }
  return map[status] || 'gray'
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getSettlementListApi()
    settlementList.value = res.data || []
  } catch (e) {
    settlementList.value = []
  } finally {
    loading.value = false
  }
}

async function handleGenerate() {
  if (!generateMonth.value) {
    Message.warning('请先选择生成月份')
    return
  }
  Modal.confirm({
    title: '确认生成',
    content: `确定要生成 ${formatMonth(generateMonth.value)} 的对账单吗？\n系统将汇总该月份所有已通过审批的工时单并计算结算金额。`,
    width: 480,
    onOk: async () => {
      generating.value = true
      try {
        const res = await generateSettlementApi(generateMonth.value)
        Message.success(`对账单生成成功！共 ${res.data.itemCount} 条明细，总金额 ¥${formatAmount(res.data.totalAmount)}`)
        fetchList()
      } catch (e) {}
      finally {
        generating.value = false
      }
    }
  })
}

function goDetail(record) {
  router.push(`/finance/settlement/detail/${record.id}`)
}

function handleDelete(record) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除对账单 ${record.settlementNo} 吗？\n删除后不可恢复。`,
    width: 480,
    okButtonProps: { status: 'danger' },
    onOk: async () => {
      try {
        await deleteSettlementApi(record.id)
        Message.success('删除成功')
        fetchList()
      } catch (e) {}
    }
  })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.settlement-page {
  width: 100%;
}

.toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
}

.section-icon {
  color: #165DFF;
  font-size: 18px;
}

.section-desc {
  font-size: 13px;
  color: #86909c;
}

.settlement-no {
  font-family: 'Consolas', 'Monaco', monospace;
  font-weight: 600;
  color: #165DFF;
  font-size: 13px;
}

.amount-total {
  font-weight: 700;
  color: #f53f3f;
  font-size: 15px;
}
</style>
