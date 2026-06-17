<template>
  <div class="page-container">
    <a-card :bordered="false" class="page-card">
      <template #title>
        <div class="card-title">
          <icon-dashboard class="title-icon" />
          <span>工作台</span>
        </div>
      </template>

      <div class="stats-grid">
        <a-card :bordered="false" class="stat-card stat-card-blue" :loading="loading">
          <div class="stat-content">
            <div class="stat-icon">
              <icon-user-group />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.activeTalentCount || 0 }}</div>
              <div class="stat-label">当前在项人数</div>
            </div>
          </div>
        </a-card>

        <a-card :bordered="false" class="stat-card stat-card-orange" :loading="loading">
          <div class="stat-content">
            <div class="stat-icon">
              <icon-user />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.idleTalentCount || 0 }}</div>
              <div class="stat-label">待岗人数</div>
            </div>
          </div>
        </a-card>

        <a-card :bordered="false" class="stat-card stat-card-green" :loading="loading">
          <div class="stat-content">
            <div class="stat-icon">
              <icon-folder />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.newProjectCount || 0 }}</div>
              <div class="stat-label">本月新签项目数</div>
            </div>
          </div>
        </a-card>

        <a-card :bordered="false" class="stat-card stat-card-purple" :loading="loading">
          <div class="stat-content">
            <div class="stat-icon">
              <icon-gift />
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(stats.monthlyRevenue) }}</div>
              <div class="stat-label">本月总营收</div>
            </div>
          </div>
        </a-card>
      </div>

      <a-card :bordered="false" class="chart-card" :loading="chartLoading">
        <template #title>
          <div class="chart-title">
            <icon-bar-chart />
            <span>各项目毛利分析</span>
          </div>
        </template>
        <div ref="profitChartRef" class="chart-container"></div>
      </a-card>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import {
  IconDashboard,
  IconUserGroup,
  IconUser,
  IconFolder,
  IconGift,
  IconBarChart
} from '@arco-design/web-vue/es/icon'
import * as echarts from 'echarts'
import { getDashboardStats, getProjectProfit } from '@/api/dashboard'

const stats = ref({
  activeTalentCount: 0,
  idleTalentCount: 0,
  newProjectCount: 0,
  monthlyRevenue: 0
})
const loading = ref(false)
const chartLoading = ref(false)
const profitChartRef = ref(null)
let profitChart = null

const formatMoney = (value) => {
  if (!value) return '0'
  return Number(value).toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

const loadStats = async () => {
  loading.value = true
  try {
    const res = await getDashboardStats()
    if (res.data) {
      stats.value = res.data
    }
  } catch (e) {
    console.error('加载统计数据失败', e)
  } finally {
    loading.value = false
  }
}

const initProfitChart = (data) => {
  if (!profitChartRef.value) return
  if (profitChart) {
    profitChart.dispose()
  }
  profitChart = echarts.init(profitChartRef.value)

  const projectNames = data.map(item => item.projectName || '未命名项目')
  const profitData = data.map(item => item.profit || 0)
  const colors = profitData.map(v => v >= 0 ? '#165DFF' : '#F53F3F')

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        const idx = params[0].dataIndex
        const item = data[idx]
        return `
          <div style="font-weight:600;margin-bottom:8px">${item.projectName}</div>
          <div>客户结算总价: ¥${formatMoney(item.totalSettlement)}</div>
          <div>人员月薪成本: ¥${formatMoney(item.totalSalaryCost)}</div>
          <div style="font-weight:600;color:${item.profit >= 0 ? '#165DFF' : '#F53F3F'}">
            毛利: ¥${formatMoney(item.profit)}
          </div>
        `
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: projectNames,
      axisLabel: {
        interval: 0,
        rotate: 30,
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
        name: '毛利',
        type: 'bar',
        data: profitData.map((v, i) => ({
          value: v,
          itemStyle: {
            color: colors[i],
            borderRadius: [4, 4, 0, 0]
          }
        })),
        barWidth: '40%',
        label: {
          show: true,
          position: 'top',
          formatter: (params) => {
            const v = params.value
            if (Math.abs(v) >= 10000) {
              return '¥' + (v / 10000).toFixed(1) + '万'
            }
            return '¥' + v.toFixed(0)
          },
          fontSize: 11
        }
      }
    ]
  }

  profitChart.setOption(option)
}

const loadProjectProfit = async () => {
  chartLoading.value = true
  try {
    const res = await getProjectProfit()
    if (res.data) {
      await nextTick()
      initProfitChart(res.data)
    }
  } catch (e) {
    console.error('加载项目毛利数据失败', e)
  } finally {
    chartLoading.value = false
  }
}

const handleResize = () => {
  profitChart && profitChart.resize()
}

onMounted(() => {
  loadStats()
  loadProjectProfit()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (profitChart) {
    profitChart.dispose()
    profitChart = null
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
  color: #165DFF;
  font-size: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  background: linear-gradient(135deg, #f0f4ff 0%, #ffffff 100%);
}

.stat-card-blue {
  background: linear-gradient(135deg, #e8f3ff 0%, #ffffff 100%);
}

.stat-card-orange {
  background: linear-gradient(135deg, #fff7e8 0%, #ffffff 100%);
}

.stat-card-green {
  background: linear-gradient(135deg, #e8fffa 0%, #ffffff 100%);
}

.stat-card-purple {
  background: linear-gradient(135deg, #f5e8ff 0%, #ffffff 100%);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #165DFF;
  background: rgba(22, 93, 255, 0.1);
}

.stat-card-orange .stat-icon {
  color: #FF7D00;
  background: rgba(255, 125, 0, 0.1);
}

.stat-card-green .stat-icon {
  color: '#00B42A';
  background: rgba(0, 180, 42, 0.1);
}

.stat-card-purple .stat-icon {
  color: #722ED1;
  background: rgba(114, 46, 209, 0.1);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1d2129;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #86909c;
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
  height: 400px;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
