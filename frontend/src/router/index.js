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
        name: 'ResourceCenter',
        redirect: '/resource/talent',
        component: () => import('@/views/resource/ResourceManage.vue'),
        meta: { title: '资源中心' },
        children: [
          {
            path: 'talent',
            name: 'TalentPool',
            component: () => import('@/views/resource/TalentPool.vue'),
            meta: { title: '人才库' }
          },
          {
            path: 'project',
            name: 'ResourceProject',
            component: () => import('@/views/resource/ProjectPool.vue'),
            meta: { title: '项目池' }
          }
        ]
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
        meta: { title: '报工审批' },
        children: [
          {
            path: 'calendar',
            name: 'EmployeeCalendar',
            component: () => import('@/views/report/EmployeeCalendar.vue'),
            meta: { title: '工时填报' }
          },
          {
            path: 'approval',
            name: 'ApprovalList',
            component: () => import('@/views/report/ApprovalList.vue'),
            meta: { title: '工时审批' }
          }
        ]
      },
      {
        path: 'finance',
        name: 'FinanceSettlement',
        redirect: '/finance/settlement',
        component: () => import('@/views/finance/FinanceSettlement.vue'),
        meta: { title: '财务结算' },
        children: [
          {
            path: 'workday',
            name: 'WorkdaySetting',
            component: () => import('@/views/finance/WorkdaySetting.vue'),
            meta: { title: '工作日设置' }
          },
          {
            path: 'settlement',
            name: 'SettlementList',
            component: () => import('@/views/finance/SettlementList.vue'),
            meta: { title: '对账单管理' }
          },
          {
            path: 'settlement/detail/:id',
            name: 'SettlementDetail',
            component: () => import('@/views/finance/SettlementDetail.vue'),
            meta: { title: '对账单详情' }
          },
          {
            path: 'cost-analysis',
            name: 'CostAnalysis',
            component: () => import('@/views/finance/CostAnalysis.vue'),
            meta: { title: '成本分析' }
          }
        ]
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
