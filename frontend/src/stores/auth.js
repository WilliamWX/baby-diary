import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, setToken, removeToken, getUser, setUser, removeUser } from '../utils/auth'
import request from '../api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(getUser())
  const isLoggedIn = ref(!!getToken())

  async function login(username, password) {
    const res = await request.post('/user/login', { username, password })
    setToken(res.data.token)
    setUser({ id: res.data.userId, username: res.data.username })
    user.value = res.data
    isLoggedIn.value = true
  }

  async function register(username, password, email) {
    const res = await request.post('/user/register', { username, password, email })
    setToken(res.data.token)
    setUser({ id: res.data.userId, username: res.data.username })
    user.value = res.data
    isLoggedIn.value = true
  }

  function logout() {
    removeToken()
    removeUser()
    user.value = null
    isLoggedIn.value = false
  }

  async function fetchProfile() {
    const res = await request.get('/user/profile')
    user.value = { ...user.value, ...res.data }
    setUser(user.value)
  }

  return { user, isLoggedIn, login, register, logout, fetchProfile }
})
