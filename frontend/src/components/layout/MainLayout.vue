<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { getNotifications, getUnreadCount, markAllRead, markRead } from '../../api/notification'
import { searchUsers, addFriend, getFriendRequests, acceptFriend, rejectFriend } from '../../api/friend'
import { BASE_URL } from '../../utils/config'
import Logo from '../Logo.vue'

const router = useRouter()
const auth = useAuthStore()
const baseUrl = BASE_URL

const unreadCount = ref(0)
const notifList = ref([])
const notifVisible = ref(false)

// Friend search dialog
const friendDialogVisible = ref(false)
const friendSearchKeyword = ref('')
const friendSearchResults = ref([])
const friendSearchLoading = ref(false)
const friendRequests = ref([])
const friendRequestCount = ref(0)

async function fetchFriendRequests() {
  try {
    const res = await getFriendRequests()
    friendRequests.value = res.data || []
    friendRequestCount.value = friendRequests.value.length
  } catch (e) { /* ignored */ }
}

async function handleAcceptRequest(req) {
  try {
    await acceptFriend(req.requestId)
    ElMessage.success('已接受好友申请')
    friendRequests.value = friendRequests.value.filter(r => r.requestId !== req.requestId)
    friendRequestCount.value = friendRequests.value.length
    auth.fetchProfile()
  } catch (e) { /* ignored */ }
}

async function handleRejectRequest(req) {
  try {
    await rejectFriend(req.requestId)
    friendRequests.value = friendRequests.value.filter(r => r.requestId !== req.requestId)
    friendRequestCount.value = friendRequests.value.length
    ElMessage.success('已拒绝')
  } catch (e) { /* ignored */ }
}

async function handleFriendSearch() {
  const kw = friendSearchKeyword.value.trim()
  if (!kw) {
    ElMessage.warning('请输入用户名')
    return
  }
  friendSearchLoading.value = true
  try {
    const res = await searchUsers(kw)
    friendSearchResults.value = res.data || []
  } finally {
    friendSearchLoading.value = false
  }
}

async function handleAddFriend(user) {
  try {
    await addFriend(user.id)
    ElMessage.success('好友申请已发送')
    user.friendStatus = 0
  } catch (e) {
    // error handled by interceptor
  }
}

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

async function handleMarkAllRead() {
  await markAllRead()
  unreadCount.value = 0
  notifList.value = notifList.value.map(n => ({ ...n, isRead: 1 }))
}

function goToTarget(n) {
  notifVisible.value = false
  if (!n.isRead) {
    markRead(n.id)
    n.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  if (n.targetType === 'diary') {
    router.push(`/diary/${n.targetId}`)
  } else if (n.targetType === 'post') {
    router.push(`/post/${n.targetId}`)
  } else if (n.targetType === 'moment') {
    router.push(`/moment/${n.targetId}`)
  } else if (n.type === 'follow') {
    router.push(`/user/${n.actorId}`)
  }
}

function handleLogout() {
  auth.logout()
  router.push('/login')
}

onMounted(() => {
  if (auth.isLoggedIn) {
    fetchUnread()
    fetchFriendRequests()
  }
})
</script>

<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="header-left">
        <router-link to="/" class="logo-link"><Logo :size="36" /></router-link>
        <el-menu mode="horizontal" :default-active="$route.path" router>
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/moments">精彩时刻</el-menu-item>
          <el-menu-item index="/diary">日记</el-menu-item>
          <el-menu-item index="/posts">经验</el-menu-item>
          <el-menu-item index="/ai-doctor">AI 医生问答</el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <!-- Notification Bell -->
        <el-popover
          v-model:visible="notifVisible"
          placement="bottom-end"
          :width="360"
          trigger="click"
          @show="fetchNotifications"
        >
          <template #reference>
            <span class="notif-bell">
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
                @click.stop="goToTarget(n)">
                <el-avatar :size="36" :src="n.actorAvatar ? baseUrl + n.actorAvatar : undefined" icon="UserFilled" />
                <div class="notif-body">
                  <p class="notif-text"><strong>{{ n.actorName }}</strong> {{ n.content }}</p>
                  <span class="notif-time">{{ n.createdAt?.slice(0, 16).replace('T', ' ') }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-popover>

        <el-badge v-if="auth.isLoggedIn" :value="friendRequestCount" :hidden="friendRequestCount === 0">
          <el-button text @click="friendDialogVisible = true">
            <el-icon :size="18"><Plus /></el-icon>
            添加好友
          </el-button>
        </el-badge>

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

    <!-- Friend Search Dialog -->
    <el-dialog v-model="friendDialogVisible" title="添加好友" width="420px" :close-on-click-modal="false" @open="fetchFriendRequests">
      <!-- Pending friend requests -->
      <div v-if="friendRequests.length > 0" class="friend-requests-section">
        <h4 class="friend-requests-title">好友申请 ({{ friendRequests.length }})</h4>
        <div class="friend-requests-list">
          <div v-for="req in friendRequests" :key="req.requestId" class="friend-request-item">
            <div class="search-user-info">
              <el-avatar :size="36" :src="req.avatar ? baseUrl + req.avatar : undefined" icon="UserFilled" />
              <span class="search-user-name">{{ req.username }}</span>
            </div>
            <div class="friend-request-actions">
              <el-button type="primary" size="small" @click="handleAcceptRequest(req)">同意</el-button>
              <el-button size="small" @click="handleRejectRequest(req)">拒绝</el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="friend-search-box">
        <el-input
          v-model="friendSearchKeyword"
          placeholder="输入用户名搜索"
          @keyup.enter="handleFriendSearch"
          clearable
        >
          <template #append>
            <el-button :loading="friendSearchLoading" @click="handleFriendSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
      <div v-loading="friendSearchLoading" class="friend-search-results">
        <div v-if="friendSearchResults.length > 0">
          <div v-for="user in friendSearchResults" :key="user.id" class="search-user-item">
            <div class="search-user-info">
              <el-avatar :size="36" :src="user.avatar ? baseUrl + user.avatar : undefined" icon="UserFilled" />
              <span class="search-user-name">{{ user.username }}</span>
            </div>
            <el-button
              v-if="user.friendStatus === 1"
              size="small"
              disabled
            >已添加</el-button>
            <el-button
              v-else-if="user.friendStatus === 0"
              size="small"
              disabled
            >已申请</el-button>
            <el-button
              v-else
              type="primary"
              size="small"
              @click="handleAddFriend(user)"
            >添加</el-button>
          </div>
        </div>
        <el-empty v-else-if="!friendSearchLoading && friendSearchKeyword" description="未找到用户" :image-size="60" />
      </div>
    </el-dialog>
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

.friend-search-box { margin-bottom: 16px; }
.friend-search-results { min-height: 80px; }
.search-user-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 4px;
  border-bottom: 1px solid #f5f5f5;
}
.search-user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.search-user-name { font-size: 14px; color: #333; }

.friend-requests-section {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}
.friend-requests-title {
  font-size: 14px;
  color: #ff6b81;
  margin: 0 0 10px;
}
.friend-requests-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.friend-request-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 4px;
  border-bottom: 1px solid #fafafa;
}
.friend-request-actions {
  display: flex;
  gap: 6px;
}
</style>
