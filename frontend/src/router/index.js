import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import MainLayout from '@/layout/MainLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Workbench',
        component: () => import('@/views/dashboard/Workbench.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'resource',
        name: 'ResourceManage',
        component: () => import('@/views/resource/ResourceManage.vue'),
        meta: { title: '资源管理' }
      },
      {
        path: 'project',
        name: 'ProjectPool',
        component: () => import('@/views/project/ProjectPool.vue'),
        meta: { title: '项目池' }
      },
      {
        path: 'assignment',
        name: 'AssignmentCenter',
        component: () => import('@/views/assignment/AssignmentCenter.vue'),
        meta: { title: '指派中心' }
      },
      {
        path: 'timesheet',
        name: 'ReportApproval',
        component: () => import('@/views/report/ReportApproval.vue'),
        meta: { title: '报工审批' }
      },
      {
        path: 'finance',
        name: 'FinanceSettlement',
        component: () => import('@/views/finance/FinanceSettlement.vue'),
        meta: { title: '财务结算' }
      },
      {
        path: 'settings',
        name: 'SystemSettings',
        component: () => import('@/views/system/SystemSettings.vue'),
        meta: { title: '系统设置' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token

  if (to.meta.public) {
    if (token && to.path === '/login') {
      next('/')
    } else {
      next()
    }
  } else {
    if (!token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
    } else {
      next()
    }
  }
})

export default router
