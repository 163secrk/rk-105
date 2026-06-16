import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, getUserInfoApi } from '@/api/auth'
import { getMenuListApi } from '@/api/menu'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const menuList = ref([])

  const isLoggedIn = computed(() => !!token.value)

  function setToken(newToken) {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  function setUserInfo(info) {
    userInfo.value = info
  }

  function setMenuList(menus) {
    menuList.value = menus
  }

  async function login(username, password) {
    const res = await loginApi({ username, password })
    setToken(res.data.token)
    const userInfo = {
      ...res.data.user,
      name: res.data.user.realName
    }
    setUserInfo(userInfo)
    return res.data
  }

  async function fetchUserInfo() {
    const res = await getUserInfoApi()
    const info = {
      ...res.data,
      name: res.data.realName
    }
    setUserInfo(info)
    return info
  }

  async function fetchMenuList() {
    const res = await getMenuListApi()
    const menus = res.data.map(item => ({
      key: item.menuPath,
      title: item.menuName,
      icon: item.menuIcon,
      path: item.menuPath
    }))
    setMenuList(menus)
    return menus
  }

  function logout() {
    setToken('')
    setUserInfo(null)
    setMenuList([])
  }

  return {
    token,
    userInfo,
    menuList,
    isLoggedIn,
    setToken,
    setUserInfo,
    setMenuList,
    login,
    fetchUserInfo,
    fetchMenuList,
    logout
  }
})
