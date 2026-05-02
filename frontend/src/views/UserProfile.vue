<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api'
import { toggleFollow } from '../api/post'
import { BASE_URL } from '../utils/config'

const route = useRoute()
const baseUrl = BASE_URL
const profile = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await request.get(`/user/profile/${route.params.id}`)
    profile.value = res.data
  } catch (e) {
    ElMessage.error('用户不存在')
  } finally {
    loading.value = false
  }
})

async function handleFollow() {
  try {
    await toggleFollow(profile.value.id)
    ElMessage.success('操作成功')
  } catch (e) { /* ignored */ }
}
</script>

<template>
  <div class="user-profile" v-loading="loading">
    <el-card v-if="profile">
      <div class="profile-header">
        <el-avatar :size="80" :src="profile.avatar ? baseUrl + profile.avatar : undefined" icon="UserFilled" />
        <div class="info">
          <h2>{{ profile.username }}</h2>
          <p class="bio">{{ profile.bio || '这个人很懒，什么都没写...' }}</p>
          <span class="join">加入于 {{ profile.createdAt?.slice(0, 10) }}</span>
        </div>
        <el-button type="primary" @click="handleFollow">关注</el-button>
      </div>
      <div class="stats">
        <div class="stat"><span class="num">{{ profile.diaryCount || 0 }}</span><span>日记</span></div>
        <div class="stat"><span class="num">{{ profile.followerCount || 0 }}</span><span>粉丝</span></div>
        <div class="stat"><span class="num">{{ profile.followingCount || 0 }}</span><span>关注</span></div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.user-profile { max-width: 800px; margin: 0 auto; padding: 20px 0; }
.profile-header { display: flex; gap: 24px; align-items: center; margin-bottom: 20px; }
.info h2 { font-size: 22px; margin-bottom: 6px; }
.bio { color: #666; }
.join { font-size: 13px; color: #999; }
.stats { display: flex; gap: 40px; padding-top: 16px; border-top: 1px solid #eee; }
.stat { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.stat .num { font-size: 20px; font-weight: bold; color: #ff6b81; }
.stat span:last-child { font-size: 13px; color: #999; }
</style>
