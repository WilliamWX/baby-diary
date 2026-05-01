<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import Logo from '../Logo.vue'

const router = useRouter()
const auth = useAuthStore()

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="header-left">
        <router-link to="/" class="logo-link"><Logo :size="36" /></router-link>
        <el-menu mode="horizontal" :default-active="$route.path" router>
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/posts">经验分享</el-menu-item>
          <el-menu-item index="/diary/create">写日记</el-menu-item>
          <el-menu-item index="/ai-doctor">AI 医生</el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <el-dropdown v-if="auth.isLoggedIn">
          <span class="user-dropdown">
            <el-avatar :size="32" :src="auth.user?.avatar ? 'http://localhost:9000' + auth.user.avatar : undefined" icon="UserFilled" />
            <span class="username">{{ auth.user?.username }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <div v-else>
          <el-button type="primary" @click="router.push('/login')">登录</el-button>
          <el-button @click="router.push('/register')">注册</el-button>
        </div>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.layout {
  min-height: 100vh;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #eee;
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 30px;
}
.logo-link {
  text-decoration: none;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.username {
  font-size: 14px;
}
.el-header {
  height: 60px;
}
</style>
