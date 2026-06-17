<template>
  <div class="page-container">
    <a-card :bordered="false" class="page-card">
      <template #title>
        <div class="card-title">
          <icon-bar-chart class="title-icon" />
          <span>成本分析</span>
        </div>
      </template>

      <div class="filter-section">
        <a-form layout="inline" :model="filterForm">
          <a-form-item label="时间范围">
            <a-range-picker
              v-model="dateRange"
              mode="month"
              style="width: 280px"
              :placeholder="['开始月份', '结束月份']"
              @change="handleDateChange"
            />
          </a-form-item>
          <a-form-item label="项目">
            <a-select
              v-model="filterForm.projectId"
              placeholder="选择项目（可选）"
              style="width: 240px"
              allow-clear
            >
              <a-option
                v-for="project in projectList"
                :key="project.id"
                :value="project.id"
              >
                {{ project.projectName }}
              </a-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="loadData">
              <template #icon><icon-search /></template>
              查询
            </a-button>
            <a-button style="margin-left: 8px" @click="resetFilter">
              重置
            </a-button>
          </a-form-item>
        </a-form>
      </div>

      <div class="summary-cards">
        <a-card :bordered="false" class="summary-card summary-card-total" :loading="loading">
          <div class="summary-content">
            <div class="summary-label">期间总营收</div>
            <div class="summary-value">¥{{ formatMoney(totalSettlement) }}</div>
          </div>
        </a-card>
        <a-card :bordered="false" class="summary-card summary-card-cost" :loading="loading">
          <div class="summary-content">
            <div class="summary-label">期间总成本</div>
            <div class="summary-value">¥{{ formatMoney(totalSalaryCost) }}</div>
          </div>
        </a-card>
        <a-card :bordered="false" class="summary-card summary-card-profit" :loading="loading">
          <div class="summary-content">
            <div class="summary-label">期间总毛利</div>
            <div class="summary-value" :class="{ 'text-negative': totalProfit < 0 }">
              ¥{{ formatMoney(totalProfit) }}
            </div>
          </div>
        </a-card>
        <a-card :bordered="false" class="summary-card summary-card-rate" :loading="loading">
          <div class="summary-content">
            <div class="summary-label">平均毛利率</div>
            <div class="summary-value" :class="{ 'text-negative': profitRate < 0 }">
              {{ profitRate.toFixed(1) }}%
            </div>
          </div>
        </a-card>
      </div>

      <a-card :bordered="false" class="chart-card" :loading="loading">
        <template #title>
          <div class="chart-title">
            <icon-bar-chart />
            <span>月度毛利走势</span>
          </div>
        </template>
        <div ref="trendChartRef" class="chart-container"></div>
      </a-card>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import {
  IconBarChart,
  IconSearch
} from '@arco-design/web-vue/es/icon'
import * as echarts from 'echarts'
import { getMonthlyProfit, getProjectList } from '@/api/dashboard'

const dateRange = ref([])
const filterForm = reactive({
  projectId: undefined
})
const projectList = ref([])
const chartData = ref([])
const loading = ref(false)
const trendChartRef = ref(null)
let trendChart = null

const totalSettlement = computed(() => {
  return chartData.value.reduce((sum, item) => sum + (item.totalSettlement || 0), 0)
})

const totalSalaryCost = computed(() => {
  return chartData.value.reduce((sum, item) => sum + (item.totalSalaryCost || 0), 0)
})

const totalProfit = computed(() => {
  return totalSettlement.value - totalSalaryCost.value
})

const profitRate = computed(() => {
  if (totalSettlement.value === 0) return 0
  return (totalProfit.value / totalSettlement.value) * 100
})

const formatMoney = (value) => {
  if (!value && value !== 0) return '0'
  return Number(value).toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

const formatMonth = (month) => {
  if (!month) return ''
  const parts = month.split('-')
  if (parts.length === 2) {
    return `${parts[0]}年${parts[1]}月`
  }
  return month
}

const handleDateChange = (dates) => {
  if (dates && dates.length === 2) {
    dateRange.value = dates
  }
}

const initTrendChart = (data) => {
  if (!trendChartRef.value) return
  if (trendChart) {
    trendChart.dispose()
  }
  trendChart = echarts.init(trendChartRef.value)

  const months = data.map(item => formatMonth(item.month))
  const settlements = data.map(item => item.totalSettlement || 0)
  const costs = data.map(item => item.totalSalaryCost || 0)
  const profits = data.map(item => item.profit || 0)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      },
      formatter: (params) => {
        const idx = params[0].dataIndex
        const item = data[idx]
        return `
          <div style="font-weight:600;margin-bottom:8px">${formatMonth(item.month)}</div>
          <div style="display:flex;align-items:center;gap:8px">
            <span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:#165DFF"></span>
            <span>营收: ¥${formatMoney(item.totalSettlement)}</span>
          </div>
          <div style="display:flex;align-items:center;gap:8px;margin-top:4px">
            <span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:#FF7D00"></span>
            <span>成本: ¥${formatMoney(item.totalSalaryCost)}</span>
          </div>
          <div style="display:flex;align-items:center;gap:8px;margin-top:4px;font-weight:600">
            <span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:#00B42A"></span>
            <span style="color:${item.profit >= 0 ? '#00B42A' : '#F53F3F'}">
              毛利: ¥${formatMoney(item.profit)}
            </span>
          </div>
        `
      }
    },
    legend: {
      data: ['营收', '成本', '毛利'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months,
      boundaryGap: false,
      axisLabel: {
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value) => '¥' + (value / 10000).toFixed(0) + '万'
      }
    },
    series: [
      {
        name: '营收',
        type: 'line',
        data: settlements,
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#165DFF'
        },
        itemStyle: {
          color: '#165DFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(22, 93, 255, 0.2)' },
            { offset: 1, color: 'rgba(22, 93, 255, 0.02)' }
          ])
        }
      },
      {
        name: '成本',
        type: 'line',
        data: costs,
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#FF7D00'
        },
        itemStyle: {
          color: '#FF7D00'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 125, 0, 0.2)' },
            { offset: 1, color: 'rgba(255, 125, 0, 0.02)' }
          ])
        }
      },
      {
        name: '毛利',
        type: 'line',
        data: profits,
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#00B42A'
        },
        itemStyle: {
          color: '#00B42A'
        },
        markLine: {
          silent: true,
          data: [
            {
              yAxis: 0,
              lineStyle: {
                color: '#F53F3F',
                type: 'dashed',
                width: 1
              }
            }
          ],
          label: {
            show: false
          }
        },
        markPoint: {
          data: [
            { type: 'max', name: '最高', label: { formatter: '{c}' } },
            { type: 'min', name: '最低', label: { formatter: '{c}' } }
          ]
        }
      }
    ]
  }

  trendChart.setOption(option)
}

const loadProjects = async () => {
  try {
    const res = await getProjectList()
    if (res.data) {
      projectList.value = res.data
    }
  } catch (e) {
    console.error('加载项目列表失败', e)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startMonth = dateRange.value[0]
      params.endMonth = dateRange.value[1]
    }
    if (filterForm.projectId) {
      params.projectId = filterForm.projectId
    }
    const res = await getMonthlyProfit(params)
    if (res.data) {
      chartData.value = res.data
      await nextTick()
      initTrendChart(res.data)
    }
  } catch (e) {
    console.error('加载毛利走势数据失败', e)
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  dateRange.value = []
  filterForm.projectId = undefined
  loadData()
}

const handleResize = () => {
  trendChart && trendChart.resize()
}

onMounted(() => {
  loadProjects()
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
})
</script>

<style scoped>
.page-container {
  width: 100%;
}

.page-card {
  border-radius: 8px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.title-icon {
  color: #722ED1;
  font-size: 20px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 16px;
  background: #f7f8fa;
  border-radius: 8px;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.summary-card {
  border-radius: 8px;
}

.summary-content {
  text-align: center;
}

.summary-label {
  font-size: 13px;
  color: #86909c;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 26px;
  font-weight: 700;
  color: #1d2129;
}

.summary-card-total .summary-value {
  color: #165DFF;
}

.summary-card-cost .summary-value {
  color: #FF7D00;
}

.summary-card-profit .summary-value {
  color: #00B42A;
}

.summary-card-rate .summary-value {
  color: #722ED1;
}

.text-negative {
  color: #F53F3F !important;
}

.chart-card {
  border-radius: 8px;
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
}

.chart-container {
  width: 100%;
  height: 450px;
}

@media (max-width: 1200px) {
  .summary-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .summary-cards {
    grid-template-columns: 1fr;
  }

  .filter-section :deep(.arco-form-item) {
    display: block;
    margin-right: 0 !important;
    margin-bottom: 12px;
  }
}
</style>
