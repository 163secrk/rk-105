<template>
  <div class="page-container">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

onMounted(async () => {
  if (!userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  const roles = userStore.userInfo?.roles || []
  const isPm = roles.some(r => r.roleCode === 'PM')
  const isAdmin = roles.some(r => r.roleCode === 'ADMIN')

  if (isPm || isAdmin) {
    router.replace('/timesheet/approval')
  } else {
    router.replace('/timesheet/calendar')
  }
})
</script>

<style scoped>
.page-container {
  width: 100%;
}
</style>
