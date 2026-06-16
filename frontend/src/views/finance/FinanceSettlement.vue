<template>
  <div class="page-container">
    <a-card :bordered="false" class="page-card">
      <template #title>
        <div class="card-title">
          <icon-money-circle class="title-icon" />
          <span>财务结算</span>
        </div>
      </template>
      <div class="placeholder-content">
        <div class="placeholder-icon">
          <icon-money-circle />
        </div>
        <h3>财务结算 - 开发中</h3>
        <p>账单管理与财务结算</p>
      </div>
    </a-card>
  </div>
</template>

<script setup>
import { IconMoneyCircle } from '@arco-design/web-vue/es/icon'
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

.placeholder-content {
  padding: 80px 0;
  text-align: center;
  color: #86909c;
}

.placeholder-icon {
  font-size: 64px;
  color: #c9cdd4;
  margin-bottom: 20px;
}

.placeholder-content h3 {
  color: #4e5969;
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 500;
}

.placeholder-content p {
  margin: 0;
  font-size: 14px;
}
</style>
