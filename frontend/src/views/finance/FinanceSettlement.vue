<template>
  <div class="finance-container">
    <a-card :bordered="false" class="finance-card">
      <template #title>
        <div class="card-header">
          <div class="card-title">
            <icon-apps class="title-icon" />
            <span>财务结算</span>
          </div>
        </div>
      </template>

      <a-tabs v-model:active-key="activeTab" @change="handleTabChange">
        <a-tab-pane key="settlement" title="对账单管理">
          <template #title>
            <div class="tab-title">
              <icon-file />
              <span>对账单管理</span>
            </div>
          </template>
        </a-tab-pane>
        <a-tab-pane key="workday" title="工作日设置">
          <template #title>
            <div class="tab-title">
              <icon-calendar />
              <span>工作日设置</span>
            </div>
          </template>
        </a-tab-pane>
      </a-tabs>

      <div class="tab-content">
        <router-view />
      </div>
    </a-card>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  IconApps,
  IconFile,
  IconCalendar
} from '@arco-design/web-vue/es/icon'

const route = useRoute()
const router = useRouter()

const activeTab = ref('settlement')

function detectTabFromRoute() {
  const path = route.path
  if (path.includes('/workday')) {
    activeTab.value = 'workday'
  } else if (path.includes('/settlement/detail')) {
    activeTab.value = 'settlement'
  } else {
    activeTab.value = 'settlement'
  }
}

function handleTabChange(key) {
  if (key === 'workday') {
    router.push('/finance/workday')
  } else {
    router.push('/finance/settlement')
  }
}

watch(() => route.path, () => {
  detectTabFromRoute()
})

onMounted(() => {
  detectTabFromRoute()
})
</script>

<style scoped>
.finance-container {
  width: 100%;
}

.finance-card {
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

.tab-title {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-content {
  padding-top: 4px;
}
</style>
