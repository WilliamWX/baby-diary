<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFriendList, removeFriend, getFriendRequests, acceptFriend, rejectFriend } from '../api/friend'
import { BASE_URL } from '../utils/config'

const router = useRouter()
const baseUrl = BASE_URL
const friends = ref([])
const requests = ref([])
const loading = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const [fr, fl] = await Promise.all([getFriendRequests(), getFriendList()])
    requests.value = fr.data || []
    friends.value = fl.data || []
  } catch (e) {
    requests.value = []
    friends.value = []
  } finally {
    loading.value = false
  }
}

async function handleAccept(req) {
  try {
    await acceptFriend(req.requestId)
    ElMessage.success('已接受好友申请')
    fetchData()
  } catch (e) { /* ignored */ }
}

async function handleReject(req) {
  try {
    await rejectFriend(req.requestId)
    ElMessage.success('已拒绝')
    requests.value = requests.value.filter(r => r.requestId !== req.requestId)
  } catch (e) { /* ignored */ }
}

async function handleRemove(friend) {
  try {
    await ElMessageBox.confirm(
      `确定要删除好友「${friend.username}」吗？`,
      '删除好友',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await removeFriend(friend.id)
    friends.value = friends.value.filter(f => f.id !== friend.id)
    ElMessage.success('已删除好友')
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function goToProfile(user) {
  router.push('/user/' + user.id || user.userId)
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="friends-page">
    <h2 class="page-title">我的好友</h2>

    <div v-loading="loading">
      <!-- Friend Requests -->
      <div v-if="requests.length > 0" class="requests-section">
        <h3 class="section-title">好友申请</h3>
        <div class="request-list">
          <el-card v-for="req in requests" :key="req.requestId" class="request-card" shadow="hover">
            <div class="request-info" @click="goToProfile(req)">
              <el-avatar
                v-if="req.avatar"
                :size="48"
                :src="baseUrl + req.avatar"
              />
              <el-avatar v-else :size="48" icon="UserFilled" />
              <span class="request-name">{{ req.username }}</span>
            </div>
            <div class="request-actions">
              <el-button type="primary" size="small" @click="handleAccept(req)">同意</el-button>
              <el-button size="small" @click="handleReject(req)">拒绝</el-button>
            </div>
          </el-card>
        </div>
      </div>

      <!-- Friend List -->
      <div v-if="friends.length > 0" class="friend-list">
        <h3 v-if="requests.length > 0" class="section-title">已添加的好友</h3>
        <el-card
          v-for="friend in friends"
          :key="friend.id"
          class="friend-card"
          shadow="hover"
        >
          <div class="friend-info" @click="goToProfile(friend)">
            <el-avatar
              v-if="friend.avatar"
              :size="56"
              :src="baseUrl + friend.avatar"
            />
            <el-avatar v-else :size="56" icon="UserFilled" />
            <div class="friend-meta">
              <span class="friend-name">{{ friend.username }}</span>
              <span class="friend-bio" v-if="friend.bio">{{ friend.bio }}</span>
            </div>
          </div>
          <el-button type="danger" plain size="small" @click="handleRemove(friend)">
            删除好友
          </el-button>
        </el-card>
      </div>

      <el-empty v-else-if="!loading && requests.length === 0" description="还没有好友，去探索一下吧" :image-size="80" />
    </div>
  </div>
</template>

<style scoped>
.friends-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px 0;
}

.page-title {
  font-size: 22px;
  margin-bottom: 20px;
  color: #333;
}

.section-title {
  font-size: 15px;
  color: #ff6b81;
  margin-bottom: 12px;
  padding-left: 4px;
}

.requests-section {
  margin-bottom: 24px;
}

.request-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.request-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.request-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 12px 16px;
}

.request-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.request-info:hover .request-name {
  color: #ff6b81;
}

.request-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  transition: color 0.2s;
}

.request-actions {
  display: flex;
  gap: 8px;
}

.friend-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.friend-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.friend-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 16px 20px;
}

.friend-info {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  flex: 1;
  min-width: 0;
}

.friend-info:hover .friend-name {
  color: #ff6b81;
}

.friend-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.friend-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  transition: color 0.2s;
}

.friend-bio {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
