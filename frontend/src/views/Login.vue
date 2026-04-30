<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()

const form = ref({ username: '', password: '' })
const loading = ref(false)

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  loading.value = true
  try {
    await auth.login(form.value.username, form.value.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2 class="title">登录养娃宝</h2>
      <el-form :model="form" label-width="0">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width:100%" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="link">
        还没有账号？ <router-link to="/register">立即注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #ffd6e0 0%, #ffe4c4 100%);
}
.auth-card {
  width: 400px;
  padding: 20px;
}
.title {
  text-align: center;
  color: #ff6b81;
  margin-bottom: 24px;
  font-size: 24px;
}
.link {
  text-align: center;
  font-size: 14px;
  color: #999;
}
.link a {
  color: #ff6b81;
}
</style>
