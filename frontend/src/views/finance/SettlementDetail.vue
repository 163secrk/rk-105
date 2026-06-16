<template>
  <div class="detail-page">
    <div class="back-bar">
      <a-button type="text" @click="goBack">
        <template #icon><icon-left /></template>
        返回对账单列表
      </a-button>
    </div>

    <div v-if="loading" class="loading-wrapper">
      <a-spin tip="加载中..." />
    </div>

    <div v-else-if="settlement" class="report-container">
      <div class="report-header">
        <div class="report-title">
          <div class="company-name">Talent-Bridge 人才外派协同平台</div>
          <div class="report-name">人 员 结 算 对 账 单</div>
          <div class="report-sub">
            <span>账单编号：<strong>{{ settlement.settlementNo }}</strong></span>
            <span>生成时间：{{ formatDateTime(settlement.createTime) }}</span>
          </div>
        </div>
      </div>

      <div class="report-info">
        <a-descriptions :column="4" bordered size="large">
          <a-descriptions-item label="账单月份">
            <span class="info-highlight">{{ formatMonth(settlement.month) }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="法定工作日">
            <span>{{ settlement.workdayCount }} 天</span>
          </a-descriptions-item>
          <a-descriptions-item label="人员数量">
            <a-tag color="green">{{ settlement.itemCount }} 人</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="账单状态">
            <a-tag :color="statusColor(settlement.status)">{{ statusText(settlement.status) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="创建人">
            <span>{{ settlement.creatorName || '-' }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            <span>{{ formatDateTime(settlement.updateTime) }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="结算总金额" :span="2">
            <span class="total-amount-label">
              人民币（大写）：<strong>{{ amountToChinese(settlement.totalAmount) }}</strong>
              <span class="total-amount-num">¥ {{ formatAmount(settlement.totalAmount) }}</span>
            </span>
          </a-descriptions-item>
        </a-descriptions>
      </div>

      <div class="report-table-section">
        <div class="table-title-bar">
          <div class="table-title">
            <icon-file class="table-title-icon" />
            <span>结算明细清单</span>
          </div>
          <div class="table-meta">单位：元 &nbsp;|&nbsp; 共 {{ items.length }} 条记录</div>
        </div>

        <div class="finance-table-wrapper">
          <table class="finance-table" border="1" cellpadding="0" cellspacing="0">
            <thead>
              <tr class="thead-row">
                <th class="col-index" rowspan="2">序号</th>
                <th class="col-month" rowspan="2">账单月份</th>
                <th class="col-person" rowspan="2">人员姓名</th>
                <th class="col-project" rowspan="2">所属项目</th>
                <th class="col-price" rowspan="2">月单价<br/>(元)</th>
                <th class="col-workday" rowspan="2">工作日<br/>(天)</th>
                <th class="col-attendance" rowspan="2">实际出勤<br/>(天)</th>
                <th class="col-overtime" rowspan="2">加班<br/>(天)</th>
                <th class="col-calc" colspan="3">计算明细</th>
                <th class="col-final" rowspan="2">最终金额<br/>(元)</th>
              </tr>
              <tr class="thead-row-sub">
                <th class="col-calc-base">基础金额</th>
                <th class="col-calc-ot">加班金额</th>
                <th class="col-calc-desc">计算过程</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, idx) in items" :key="item.id" class="tbody-row">
                <td class="col-index">{{ idx + 1 }}</td>
                <td class="col-month">{{ formatMonth(item.month || settlement.month) }}</td>
                <td class="col-person">
                  <div class="person-cell">
                    <a-avatar :size="28" :style="{ backgroundColor: avatarColor(item.talentName || item.userName) }">
                      {{ (item.talentName || item.userName || '?').charAt(0) }}
                    </a-avatar>
                    <span class="person-name">{{ item.talentName || item.userName }}</span>
                  </div>
                </td>
                <td class="col-project">{{ item.projectName || '-' }}</td>
                <td class="col-price col-number">
                  <span class="snapshot-tag">快照</span>
                  {{ formatAmount(item.unitPriceSnapshot) }}
                </td>
                <td class="col-workday col-number">
                  <span class="snapshot-tag">快照</span>
                  {{ item.workdayCountSnapshot }}
                </td>
                <td class="col-attendance col-number">
                  <span class="attendance-badge">{{ item.actualAttendanceDays }}</span>
                </td>
                <td class="col-overtime col-number">
                  <a-tag v-if="item.overtimeDays > 0" color="orangered" size="small">{{ item.overtimeDays }}</a-tag>
                  <span v-else>0</span>
                </td>
                <td class="col-calc-base col-number">
                  {{ formatAmount(item.baseAmount) }}
                </td>
                <td class="col-calc-ot col-number">
                  <span :class="{ 'amount-ot': item.overtimeAmount > 0 }">{{ formatAmount(item.overtimeAmount) }}</span>
                </td>
                <td class="col-calc-desc">
                  <div class="calc-desc" :title="item.calcDetail">{{ item.calcDetail || '-' }}</div>
                </td>
                <td class="col-final col-number final-cell">
                  <strong>¥ {{ formatAmount(item.finalAmount) }}</strong>
                </td>
              </tr>
              <tr v-if="items.length === 0">
                <td colspan="12" class="empty-cell">
                  <a-empty description="暂无明细数据" />
                </td>
              </tr>
            </tbody>
            <tfoot>
              <tr class="tfoot-row">
                <td colspan="5" class="summary-label">
                  <strong>合计</strong>
                </td>
                <td class="col-number">-</td>
                <td class="col-number">
                  <strong>{{ totalAttendanceDays }}</strong>
                </td>
                <td class="col-number">
                  <strong>{{ totalOvertimeDays }}</strong>
                </td>
                <td class="col-number">
                  <strong>{{ formatAmount(totalBaseAmount) }}</strong>
                </td>
                <td class="col-number">
                  <strong>{{ formatAmount(totalOvertimeAmount) }}</strong>
                </td>
                <td class="col-number">-</td>
                <td class="col-number summary-final">
                  <strong>¥ {{ formatAmount(settlement.totalAmount) }}</strong>
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>

      <div class="report-footer">
        <div class="sign-area">
          <div class="sign-item">
            <span class="sign-label">制表人：</span>
            <span class="sign-value">{{ settlement.creatorName || '____________' }}</span>
          </div>
          <div class="sign-item">
            <span class="sign-label">审核人：</span>
            <span class="sign-value">____________</span>
          </div>
          <div class="sign-item">
            <span class="sign-label">财务主管：</span>
            <span class="sign-value">____________</span>
          </div>
          <div class="sign-item">
            <span class="sign-label">打印日期：</span>
            <span class="sign-value">{{ todayStr }}</span>
          </div>
        </div>
      </div>

      <div class="action-bar">
        <a-space size="large">
          <a-button @click="goBack">
            <template #icon><icon-left /></template>
            返回列表
          </a-button>
          <a-button type="primary" @click="window.print()">
            <template #icon><icon-export /></template>
            打印对账单
          </a-button>
        </a-space>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import {
  IconLeft,
  IconFile,
  IconExport
} from '@arco-design/web-vue/es/icon'
import { getSettlementDetailApi } from '@/api/settlement'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const settlement = ref(null)
const items = ref([])

const todayStr = computed(() => {
  const d = new Date()
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}年${pad(d.getMonth()+1)}月${pad(d.getDate())}日`
})

const totalAttendanceDays = computed(() => items.value.reduce((sum, i) => sum + (i.actualAttendanceDays || 0), 0))
const totalOvertimeDays = computed(() => items.value.reduce((sum, i) => sum + (i.overtimeDays || 0), 0))
const totalBaseAmount = computed(() => items.value.reduce((sum, i) => sum + parseFloat(i.baseAmount || 0), 0))
const totalOvertimeAmount = computed(() => items.value.reduce((sum, i) => sum + parseFloat(i.overtimeAmount || 0), 0))

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

function avatarColor(name) {
  const colors = ['#165DFF', '#0FC6C2', '#722ED1', '#14C9C9', '#F7BA1E', '#F53F3F', '#3491FA', '#00B42A']
  let hash = 0
  for (let i = 0; i < (name || '').length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

function amountToChinese(amount) {
  if (amount == null || amount === '') return '零元整'
  const num = parseFloat(amount)
  if (isNaN(num) || num === 0) return '零元整'

  const digits = ['零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖']
  const intUnits = ['', '拾', '佰', '仟', '万', '拾', '佰', '仟', '亿']
  const decUnits = ['角', '分']

  const [intPart, decPart] = num.toFixed(2).split('.')
  let result = ''

  let intStr = intPart
  let zeroFlag = false
  for (let i = 0; i < intStr.length; i++) {
    const d = parseInt(intStr.charAt(i))
    const pos = intStr.length - 1 - i
    if (d === 0) {
      zeroFlag = true
    } else {
      if (zeroFlag) {
        result += '零'
        zeroFlag = false
      }
      result += digits[d] + intUnits[pos]
    }
  }
  result += '元'

  const jiao = parseInt(decPart.charAt(0))
  const fen = parseInt(decPart.charAt(1))
  if (jiao === 0 && fen === 0) {
    result += '整'
  } else {
    if (jiao > 0) {
      result += digits[jiao] + decUnits[0]
    } else if (fen > 0) {
      result += '零'
    }
    if (fen > 0) {
      result += digits[fen] + decUnits[1]
    }
  }
  return result
}

async function fetchDetail() {
  const id = route.params.id
  if (!id) {
    Message.error('缺少账单ID参数')
    return
  }
  loading.value = true
  try {
    const res = await getSettlementDetailApi(id)
    settlement.value = res.data.settlement
    items.value = res.data.items || []
  } catch (e) {
    items.value = []
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/finance/settlement')
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.detail-page {
  width: 100%;
}

.back-bar {
  margin-bottom: 16px;
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  padding: 120px 0;
}

.report-container {
  background: #fff;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  padding: 40px 48px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

.report-header {
  text-align: center;
  border-bottom: 2px double #1d2129;
  padding-bottom: 24px;
  margin-bottom: 24px;
}

.company-name {
  font-size: 14px;
  color: #4e5969;
  letter-spacing: 4px;
  margin-bottom: 8px;
}

.report-name {
  font-size: 28px;
  font-weight: 700;
  color: #1d2129;
  letter-spacing: 12px;
  margin-bottom: 12px;
}

.report-sub {
  display: flex;
  justify-content: center;
  gap: 40px;
  font-size: 13px;
  color: #4e5969;
}

.report-sub strong {
  font-family: 'Consolas', monospace;
  color: #165DFF;
}

.report-info {
  margin-bottom: 24px;
}

.info-highlight {
  font-weight: 600;
  color: #165DFF;
  font-size: 15px;
}

.total-amount-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  font-size: 13px;
  color: #4e5969;
}

.total-amount-label strong {
  color: #1d2129;
  font-size: 14px;
}

.total-amount-num {
  color: #f53f3f;
  font-weight: 700;
  font-size: 20px;
  font-family: 'DIN Alternate', 'Consolas', monospace;
}

.report-table-section {
  margin-bottom: 32px;
}

.table-title-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
}

.table-title-icon {
  color: #165DFF;
}

.table-meta {
  font-size: 12px;
  color: #86909c;
}

.finance-table-wrapper {
  overflow-x: auto;
  border: 1px solid #c9cdd4;
}

.finance-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  min-width: 1400px;
}

.finance-table th,
.finance-table td {
  padding: 10px 12px;
  text-align: center;
  border: 1px solid #e5e6eb;
}

.thead-row {
  background: linear-gradient(180deg, #f7f8fa 0%, #f2f3f5 100%);
}

.thead-row th {
  font-weight: 600;
  color: #1d2129;
  font-size: 13px;
  vertical-align: middle;
}

.thead-row-sub th {
  background: #fafbfc;
  font-weight: 500;
  font-size: 12px;
  color: #4e5969;
}

.tbody-row {
  transition: background 0.15s;
}

.tbody-row:hover {
  background: #f0f7ff;
}

.tbody-row:nth-child(even) {
  background: #fafbfc;
}

.tbody-row:nth-child(even):hover {
  background: #f0f7ff;
}

.col-number {
  font-family: 'DIN Alternate', 'Consolas', 'Monaco', monospace;
  text-align: right;
}

.col-index { width: 56px; }
.col-month { width: 100px; }
.col-person { width: 140px; }
.col-project { width: 180px; }
.col-price { width: 120px; }
.col-workday { width: 90px; }
.col-attendance { width: 90px; }
.col-overtime { width: 80px; }
.col-calc-base { width: 110px; }
.col-calc-ot { width: 100px; }
.col-calc-desc { width: 320px; text-align: left !important; }
.col-final { width: 120px; }

.person-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: flex-start;
  padding-left: 8px;
}

.person-name {
  font-weight: 500;
  color: #1d2129;
}

.snapshot-tag {
  display: inline-block;
  padding: 1px 4px;
  background: #e8f3ff;
  color: #165DFF;
  border-radius: 2px;
  font-size: 10px;
  margin-right: 4px;
  font-family: inherit;
  font-weight: normal;
}

.attendance-badge {
  display: inline-block;
  min-width: 32px;
  padding: 2px 8px;
  background: #e8ffea;
  color: #00b42a;
  border-radius: 10px;
  font-weight: 600;
  font-size: 12px;
}

.amount-ot {
  color: #f77234;
  font-weight: 600;
}

.calc-desc {
  font-size: 11px;
  color: #86909c;
  line-height: 1.5;
  text-align: left;
  padding: 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-all;
}

.final-cell {
  background: #fffbe6;
}

.final-cell strong {
  color: #f53f3f;
  font-size: 14px;
}

.empty-cell {
  padding: 60px 0 !important;
}

.tfoot-row {
  background: linear-gradient(180deg, #f7f8fa 0%, #eef0f3 100%);
  font-weight: 600;
}

.tfoot-row td {
  padding: 14px 12px;
  border-top: 2px solid #c9cdd4;
  font-size: 14px;
}

.summary-label {
  text-align: right !important;
  font-size: 15px;
  color: #1d2129;
}

.summary-final strong {
  color: #f53f3f;
  font-size: 16px;
}

.report-footer {
  border-top: 1px dashed #e5e6eb;
  padding-top: 24px;
}

.sign-area {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
}

.sign-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 14px;
  color: #4e5969;
}

.sign-label {
  white-space: nowrap;
}

.sign-value {
  color: #1d2129;
  font-weight: 500;
  min-width: 100px;
  border-bottom: 1px solid #1d2129;
  padding-bottom: 2px;
}

.action-bar {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  padding: 20px 0;
  border-top: 1px solid #f2f3f5;
}

@media print {
  .back-bar,
  .action-bar {
    display: none !important;
  }
  .report-container {
    border: none;
    box-shadow: none;
    padding: 0;
  }
}
</style>
