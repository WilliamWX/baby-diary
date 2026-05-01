<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'
import Logo from '../components/Logo.vue'

const router = useRouter()
const auth = useAuthStore()

const form = ref({ username: '', password: '', email: '' })
const loading = ref(false)

const footprintSteps = [
  { x: 5, y: 82, scale: 0.25, rot: -15 },
  { x: 16, y: 68, scale: 0.35, rot: 12 },
  { x: 28, y: 55, scale: 0.48, rot: -10 },
  { x: 42, y: 44, scale: 0.62, rot: 14 },
  { x: 58, y: 35, scale: 0.76, rot: -8 },
  { x: 76, y: 28, scale: 0.92, rot: 10 },
]
function footprintStyle(i) {
  const s = footprintSteps[i]
  return {
    left: s.x + '%',
    top: s.y + '%',
    transform: `rotate(${s.rot}deg) scale(${s.scale})`,
    animationDelay: (i * 0.8) + 's'
  }
}

async function handleRegister() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  if (form.value.username.length < 2 || form.value.password.length < 6) {
    ElMessage.warning('用户名至少2位，密码至少6位')
    return
  }
  loading.value = true
  try {
    await auth.register(form.value.username, form.value.password, form.value.email)
    ElMessage.success('注册成功')
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
    <!-- baby footprints: small→large = healthy growth -->
    <svg v-for="i in 6" :key="'fp'+i" class="footprint" :style="footprintStyle(i - 1)" viewBox="0 0 58 84">
      <path d="M30 78
        C18 78, 10 68, 10 58
        C10 50, 8 44, 6 36
        C4 28, 6 22, 12 16
        C18 10, 24 8, 30 8
        C36 8, 42 10, 46 16
        C50 22, 52 30, 50 40
        C48 50, 46 56, 42 64
        C38 72, 34 78, 30 78Z" fill="rgba(255,255,255,0.13)"/>
      <ellipse cx="10" cy="4" rx="5.5" ry="6" fill="rgba(255,255,255,0.13)" transform="rotate(-15 10 4)"/>
      <ellipse cx="21" cy="0" rx="4.5" ry="6" fill="rgba(255,255,255,0.13)"/>
      <ellipse cx="30" cy="-1" rx="4.5" ry="5.5" fill="rgba(255,255,255,0.13)"/>
      <ellipse cx="39" cy="2" rx="4" ry="5" fill="rgba(255,255,255,0.13)"/>
      <ellipse cx="47" cy="6" rx="3.5" ry="4.5" fill="rgba(255,255,255,0.13)" transform="rotate(12 47 6)"/>
    </svg>

    <!-- hearts -->
    <span v-for="i in 8" :key="'h'+i" class="heart" :style="{
      left: [12, 78, 8, 85, 70, 18, 90, 25][i-1] + '%',
      top: [8, 70, 38, 15, 55, 82, 35, 65][i-1] + '%',
      transform: 'scale(' + [0.5, 0.8, 0.35, 0.6, 0.45, 0.7, 0.55, 0.4][i-1] + ') rotate(' + [10, -15, 25, -8, 0, 20, -30, 5][i-1] + 'deg)',
      animationDelay: (i * 0.7) + 's'
    }" />

    <!-- bubbles -->
    <span v-for="i in 6" :key="'b'+i" class="bubble" :style="{
      left: [30, 65, 40, 55, 72, 20][i-1] + '%',
      top: [20, 50, 72, 30, 60, 85][i-1] + '%',
      width: [28, 45, 18, 35, 50, 22][i-1] + 'px',
      height: [28, 45, 18, 35, 50, 22][i-1] + 'px',
      animationDelay: (i * 0.8) + 's',
      opacity: [0.06, 0.04, 0.07, 0.05, 0.04, 0.06][i-1]
    }" />

    <div class="auth-content">
      <Logo :show-text="false" />
      <h2 class="title">养娃宝</h2>
      <p class="subtitle">记录 分享 成长</p>
      <el-card class="auth-card">
        <el-form :model="form" label-width="0">
          <el-form-item>
            <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" placeholder="密码（至少6位）" prefix-icon="Lock" size="large" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.email" placeholder="邮箱（选填）" prefix-icon="Message" size="large" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" style="width:100%" size="large" @click="handleRegister">注册</el-button>
          </el-form-item>
        </el-form>
        <div class="link">
          已有账号？ <router-link to="/login">去登录</router-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(160deg, #fce4ec 0%, #f8bbd0 25%, #f48fb1 50%, #ffccbc 75%, #ffe0b2 100%);
  overflow: hidden;
}

/* --- footprints --- */
.footprint {
  position: absolute;
  width: 50px;
  height: 60px;
  pointer-events: none;
  animation: sway 10s ease-in-out infinite;
}
@keyframes sway {
  0%, 100% { transform: translateX(0) var(--r); }
  25% { transform: translateX(6px) var(--r); }
  75% { transform: translateX(-6px) var(--r); }
}

/* --- hearts --- */
.heart {
  position: absolute;
  width: 30px;
  height: 30px;
  pointer-events: none;
  animation: heartBeat 5s ease-in-out infinite;
}
.heart::before,
.heart::after {
  content: '';
  position: absolute;
  width: 15px;
  height: 24px;
  background: rgba(255,255,255,0.15);
  border-radius: 15px 15px 0 0;
}
.heart::before {
  left: 15px;
  transform: rotate(45deg);
  transform-origin: 0 100%;
}
.heart::after {
  left: 0;
  transform: rotate(-45deg);
  transform-origin: 100% 100%;
}
@keyframes heartBeat {
  0%, 100% { transform: translateY(0) scale(1); }
  15% { transform: translateY(-8px) scale(1.12); }
  30% { transform: translateY(0) scale(1); }
}

/* --- bubbles --- */
.bubble {
  position: absolute;
  border-radius: 50%;
  background: white;
  pointer-events: none;
  animation: float 9s ease-in-out infinite;
}
@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-20px) scale(1.06); }
}

.auth-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.title {
  text-align: center;
  color: #d81b60;
  margin: 16px 0 4px;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 4px;
}
.subtitle {
  text-align: center;
  color: #ec407a;
  font-size: 14px;
  margin: 0 0 24px;
  letter-spacing: 2px;
}
.auth-card {
  width: 400px;
  padding: 28px 24px 16px;
  border-radius: 16px;
  border: none;
  box-shadow: 0 8px 40px rgba(245, 108, 150, 0.15);
}
.link {
  text-align: center;
  font-size: 14px;
  color: #999;
}
.link a {
  color: #e91e63;
  font-weight: 500;
}
</style>
