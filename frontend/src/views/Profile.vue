<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()

onMounted(async () => {
  await auth.fetchProfile()
})
</script>

<template>
  <div class="page">
    <el-card>
      <div class="profile-header">
        <el-avatar :size="80" icon="UserFilled" />
        <div class="profile-info">
          <h2>{{ auth.user?.username }}</h2>
          <p class="bio">{{ auth.user?.bio || '这个人很懒，什么都没写...' }}</p>
          <p class="email" v-if="auth.user?.email">{{ auth.user?.email }}</p>
          <p class="join-date">加入于 {{ auth.user?.createdAt?.slice(0, 10) }}</p>
        </div>
      </div>
      <div class="stats">
        <div class="stat-item">
          <span class="num">{{ auth.user?.diaryCount || 0 }}</span>
          <span class="label">日记</span>
        </div>
        <div class="stat-item">
          <span class="num">{{ auth.user?.followerCount || 0 }}</span>
          <span class="label">粉丝</span>
        </div>
        <div class="stat-item">
          <span class="num">{{ auth.user?.followingCount || 0 }}</span>
          <span class="label">关注</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;
}
.profile-header {
  display: flex;
  gap: 24px;
  align-items: center;
  margin-bottom: 24px;
}
.profile-info h2 {
  font-size: 22px;
  margin-bottom: 8px;
}
.bio {
  color: #666;
  margin-bottom: 4px;
}
.email, .join-date {
  font-size: 13px;
  color: #999;
}
.stats {
  display: flex;
  gap: 40px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.stat-item .num {
  font-size: 20px;
  font-weight: bold;
  color: #ff6b81;
}
.stat-item .label {
  font-size: 13px;
  color: #999;
}
</style>
