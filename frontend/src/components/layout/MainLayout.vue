<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { getNotifications, getUnreadCount, markAllRead } from '../../api/notification'
import { BASE_URL } from '../../utils/config'
import Logo from '../Logo.vue'

const router = useRouter()
const auth = useAuthStore()
const searchKeyword = ref('')
const baseUrl = BASE_URL

const unreadCount = ref(0)
const notifList = ref([])
const notifVisible = ref(false)

async function fetchUnread() {
  if (!auth.isLoggedIn) return
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (e) { /* ignored */ }
}

async function fetchNotifications() {
  try {
    const res = await getNotifications(1, 10)
    notifList.value = res.data.records || []
    unreadCount.value = res.data.unreadCount || 0
  } catch (e) { /* ignored */ }
}

function handleNotifClick() {
  notifVisible.value = !notifVisible.value
  if (notifVisible.value) {
    fetchNotifications()
  }
}

async function handleMarkAllRead() {
  await markAllRead()
  unreadCount.value = 0
  notifList.value = notifList.value.map(n => ({ ...n, isRead: 1 }))
}

function goToTarget(n) {
  notifVisible.value = false
  if (n.targetType === 'diary') {
    router.push(`/diary/${n.targetId}`)
  } else if (n.targetType === 'post') {
    router.push(`/post/${n.targetId}`)
  } else if (n.type === 'follow') {
    router.push(`/user/${n.actorId}`)
  }
}

function handleSearch() {
  const q = searchKeyword.value.trim()
  if (q) {
    router.push(`/search?q=${encodeURIComponent(q)}`)
  }
}

function handleLogout() {
  auth.logout()
  router.push('/login')
}

onMounted(() => {
  if (auth.isLoggedIn) fetchUnread()
})
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
      <div class="header-center">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索日记、帖子..."
          class="search-input"
          @keyup.enter="handleSearch"
          clearable
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
      <div class="header-right">
        <!-- Notification Bell -->
        <el-popover
          v-model:visible="notifVisible"
          placement="bottom-end"
          :width="360"
          trigger="click"
        >
          <template #reference>
            <span class="notif-bell" @click="handleNotifClick">
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
                <el-icon :size="22"><Bell /></el-icon>
              </el-badge>
            </span>
          </template>
          <div class="notif-panel">
            <div class="notif-header">
              <span>消息通知</span>
              <el-button v-if="unreadCount > 0" text size="small" type="primary" @click="handleMarkAllRead">全部已读</el-button>
            </div>
            <div v-if="notifList.length === 0" class="notif-empty">
              <el-empty description="暂无通知" :image-size="60" />
            </div>
            <div v-else class="notif-list">
              <div
                v-for="n in notifList"
                :key="n.id"
                class="notif-item"
                :class="{ unread: !n.isRead }"
                @click="goToTarget(n)">
                <el-avatar :size="36" :src="n.actorAvatar ? baseUrl + n.actorAvatar : undefined" icon="UserFilled" />
                <div class="notif-body">
                  <p class="notif-text"><strong>{{ n.actorName }}</strong> {{ n.content }}</p>
                  <span class="notif-time">{{ n.createdAt?.slice(0, 16).replace('T', ' ') }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-popover>

        <el-dropdown v-if="auth.isLoggedIn">
          <span class="user-dropdown">
            <el-avatar :size="32" :src="auth.user?.avatar ? baseUrl + auth.user.avatar : undefined" icon="UserFilled" />
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
.layout { min-height: 100vh; }
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #eee;
  padding: 0 20px;
}
.header-left { display: flex; align-items: center; gap: 30px; }
.header-center { flex: 1; max-width: 320px; margin: 0 20px; }
.search-input { width: 100%; }
.logo-link { text-decoration: none; }
.header-right { display: flex; align-items: center; gap: 10px; }
.user-dropdown { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.username { font-size: 14px; }
.el-header { height: 60px; }

.notif-bell {
  cursor: pointer;
  padding: 6px;
  color: #666;
  font-size: 20px;
}
.notif-bell:hover { color: #ff6b81; }

.notif-panel { max-height: 420px; }
.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 8px;
  font-weight: 600;
  font-size: 14px;
}
.notif-empty { padding: 20px 0; }

.notif-list { max-height: 340px; overflow-y: auto; }
.notif-item {
  display: flex;
  gap: 10px;
  padding: 10px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}
.notif-item:hover { background: #fafafa; }
.notif-item.unread { background: #fff5f7; }
.notif-item.unread:hover { background: #ffeef2; }
.notif-body { flex: 1; min-width: 0; }
.notif-text { font-size: 13px; color: #333; margin: 0 0 4px; line-height: 1.4; }
.notif-time { font-size: 11px; color: #bbb; }
</style>
