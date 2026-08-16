import { defineStore } from 'pinia'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: null,
    role: '',
    menus: []
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.role === 'admin',
    isTeacher: (state) => state.role === 'teacher',
    isStudent: (state) => state.role === 'student'
  },

  actions: {
    // 登录
    async login(loginForm) {
      const res = await loginApi(loginForm)
      this.token = res.data.token
      setToken(res.data.token)
      await this.fetchUserInfo()
      return res
    },

    // 初始化登录态：从本地缓存恢复，失效则立即清理
    async initializeFromStorage() {
      const savedToken = getToken()
      if (!savedToken) {
        this.resetState()
        return
      }

      this.token = savedToken
      try {
        await this.fetchUserInfo()
      } catch (error) {
        this.resetState()
      }
    },

    // 获取用户信息
    async fetchUserInfo() {
      try {
        const res = await getUserInfo()
        this.userInfo = res.data
        this.role = res.data.role
        this.menus = res.data.menus || []
        return res
      } catch (error) {
        this.resetState()
        throw error
      }
    },

    // 登出
    async logout() {
      try {
        await logoutApi()
      } finally {
        this.resetState()
      }
    },

    // 重置状态
    resetState() {
      this.token = ''
      this.userInfo = null
      this.role = ''
      this.menus = []
      removeToken()
    }
  }
})
