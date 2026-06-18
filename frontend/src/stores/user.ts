import { defineStore } from 'pinia'
import { http, type User } from '../api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null as User | null,
  }),
  actions: {
    async login(account: string, password: string) {
      const data = await http.post<{ token: string; user: User }>('/api/auth/login', { account, password })
      this.token = data.token
      this.user = data.user
      localStorage.setItem('token', data.token)
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
    },
    async fetchMe() {
      if (!this.token) return
      this.user = await http.get<User>('/api/users/me')
    },
  },
})
