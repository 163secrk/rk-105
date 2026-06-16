<template>
  <div class="resource-center">
    <a-tabs
      v-model:active-key="activeTab"
      type="rounded"
      @change="handleTabChange"
      class="resource-tabs"
    >
      <a-tab-pane key="/resource/talent" title="人才库" />
      <a-tab-pane key="/resource/project" title="项目池" />
    </a-tabs>
    <div class="tab-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const activeTab = ref('/resource/talent')

function syncTab() {
  const path = route.path
  if (path.startsWith('/resource/project')) {
    activeTab.value = '/resource/project'
  } else {
    activeTab.value = '/resource/talent'
  }
}

function handleTabChange(key) {
  if (route.path !== key) {
    router.push(key)
  }
}

onMounted(() => {
  syncTab()
})

watch(() => route.path, () => {
  syncTab()
})
</script>

<style scoped>
.resource-center {
  width: 100%;
}

.resource-tabs {
  margin-bottom: 16px;
}

.tab-content {
  width: 100%;
}
</style>
