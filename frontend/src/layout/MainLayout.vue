<template>
  <a-layout class="main-layout">
    <a-layout-sider
      v-model:collapsed="collapsed"
      :width="220"
      :collapsed-width="64"
      collapsible
      breakpoint="lg"
      class="layout-sider"
    >
      <div class="logo-wrapper">
        <div class="logo">
          <span class="logo-icon">T</span>
          <span v-if="!collapsed" class="logo-text">Talent-Bridge</span>
        </div>
      </div>
      <a-menu
        :default-selected-keys="[selectedKey]"
        :open-keys="openKeys"
        @menu-item-click="handleMenuClick"
        @sub-menu-click="handleSubMenuClick"
        theme="dark"
        class="side-menu"
      >
        <template v-for="menu in menuList" :key="menu.key">
          <a-sub-menu v-if="menu.children && menu.children.length" :key="menu.key">
            <template #icon>
              <component :is="getIcon(menu.icon)" v-if="menu.icon" />
            </template>
            <template #title>{{ menu.title }}</template>
            <a-menu-item
              v-for="child in menu.children"
              :key="child.key"
            >
              {{ child.title }}
            </a-menu-item>
          </a-sub-menu>
          <a-menu-item v-else :key="menu.key">
            <template #icon>
              <component :is="getIcon(menu.icon)" v-if="menu.icon" />
            </template>
            {{ menu.title }}
          </a-menu-item>
        </template>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="layout-header">
        <div class="header-left">
          <span class="page-title">{{ currentPageTitle }}</span>
        </div>
        <div class="header-right">
          <a-dropdown>
            <div class="user-info">
              <a-avatar :size="32" style="background-color: #165DFF">
                {{ userInfo ? userInfo.name?.charAt(0) : 'U' }}
              </a-avatar>
              <span class="user-name">{{ userInfo?.name || '用户' }}</span>
            </div>
            <template #content>
              <a-menu @menu-item-click="handleDropdownClick">
                <a-menu-item key="info">
                  <template #icon>
                    <component :is="IconUser" />
                  </template>
                  个人信息
                </a-menu-item>
                <a-menu-item key="logout">
                  <template #icon>
                    <component :is="IconExport" />
                  </template>
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      <a-layout-content class="layout-content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message, Modal } from '@arco-design/web-vue'
import { useUserStore } from '@/store/user'
import {
  IconDashboard,
  IconUserGroup,
  IconFolder,
  IconCalendar,
  IconFile,
  IconSettings,
  IconApps,
  IconUser,
  IconExport
} from '@arco-design/web-vue/es/icon'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const collapsed = ref(false)
const openKeys = ref([])

const menuList = computed(() => userStore.menuList)
const userInfo = computed(() => userStore.userInfo)

const selectedKey = computed(() => {
  return route.path
})

const currentPageTitle = computed(() => {
  const matched = route.matched
  for (let i = matched.length - 1; i >= 0; i--) {
    if (matched[i].meta && matched[i].meta.title) {
      return matched[i].meta.title
    }
  }
  return ''
})

function computeOpenKeys(menus, targetPath, parents = []) {
  for (const menu of menus) {
    if (menu.key === targetPath) {
      return parents
    }
    if (menu.children && menu.children.length) {
      const found = computeOpenKeys(menu.children, targetPath, [...parents, menu.key])
      if (found) return found
    }
  }
  return null
}

const iconMap = {
  Dashboard: IconDashboard,
  User: IconUserGroup,
  Briefcase: IconFolder,
  ClipboardList: IconCalendar,
  FileText: IconFile,
  DollarSign: IconApps,
  Settings: IconSettings
}

function getIcon(iconName) {
  return iconMap[iconName] || null
}

function handleMenuClick(key) {
  router.push(key)
}

function handleSubMenuClick(key, openKeysVal) {
  openKeys.value = openKeysVal
}

function handleLogout() {
  Modal.confirm({
    title: '确认退出',
    content: '确定要退出登录吗？',
    onOk: () => {
      userStore.logout()
      Message.success('已退出登录')
      router.push('/login')
    }
  })
}

function handleDropdownClick(key) {
  if (key === 'logout') {
    handleLogout()
  }
}

onMounted(() => {
  if (!userStore.userInfo) {
    userStore.fetchUserInfo().catch(() => {})
  }
  if (!userStore.menuList || userStore.menuList.length === 0) {
    userStore.fetchMenuList().then(() => {
      const keys = computeOpenKeys(userStore.menuList, route.path)
      if (keys) openKeys.value = keys
    }).catch(() => {})
  } else {
    const keys = computeOpenKeys(userStore.menuList, route.path)
    if (keys) openKeys.value = keys
  }
})

watch(() => route.path, (newPath) => {
  document.title = `${currentPageTitle.value} - Talent-Bridge 人才外派协同平台`
  const keys = computeOpenKeys(userStore.menuList, newPath)
  if (keys) openKeys.value = keys
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.layout-sider {
  background: #001529;
  overflow: hidden;
}

.logo-wrapper {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #165DFF, #0FC6C2);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 20px;
}

.logo-text {
  color: white;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
}

.side-menu {
  border-right: none;
  background: #001529;
}

.layout-header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e5e6eb;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f2f3f5;
}

.user-name {
  color: #1d2129;
  font-size: 14px;
}

.layout-content {
  background: #f7f8fa;
  padding: 20px;
  overflow: auto;
}
</style>
