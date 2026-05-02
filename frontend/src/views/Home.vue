<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDiaryList } from '../api/diary'
import { useAuthStore } from '../stores/auth'
import Logo from '../components/Logo.vue'
import { BASE_URL } from '../utils/config'

const router = useRouter()
const auth = useAuthStore()

const diaryList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const stats = ref({ diaryCount: 0, followerCount: 0, followingCount: 0 })
const baseUrl = BASE_URL

async function fetchStats() {
  try {
    await auth.fetchProfile()
    if (auth.user) {
      stats.value = {
        diaryCount: auth.user.diaryCount || 0,
        followerCount: auth.user.followerCount || 0,
        followingCount: auth.user.followingCount || 0
      }
    }
  } catch (e) { /* ignored */ }
}

async function fetchDiaries() {
  loading.value = true
  try {
    const res = await getDiaryList(currentPage.value, pageSize.value)
    diaryList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* ignored */ }
  finally { loading.value = false }
}

function onPageChange(page) {
  currentPage.value = page
  fetchDiaries()
}

onMounted(() => {
  fetchStats()
  fetchDiaries()
})
</script>

<template>
  <div class="home">
    <!-- Stats Banner -->
    <div class="welcome-banner">
      <div class="banner-left">
        <Logo :size="48" />
        <div class="banner-text">
          <h1>养娃宝</h1>
          <p>记录宝宝每一个成长瞬间</p>
        </div>
      </div>
      <div class="stats-row">
        <div class="stat-item" @click="router.push('/profile')">
          <span class="stat-num">{{ stats.diaryCount }}</span>
          <span class="stat-label">日记</span>
        </div>
        <div class="stat-item" @click="router.push('/profile')">
          <span class="stat-num">{{ stats.followerCount }}</span>
          <span class="stat-label">粉丝</span>
        </div>
        <div class="stat-item" @click="router.push('/profile')">
          <span class="stat-num">{{ stats.followingCount }}</span>
          <span class="stat-label">关注</span>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="quick-actions">
      <el-button type="primary" @click="router.push('/diary/create')">
        <el-icon><Edit /></el-icon> 写日记
      </el-button>
      <el-button @click="router.push('/ai-doctor')">
        <el-icon><FirstAidKit /></el-icon> AI 医生
      </el-button>
      <el-button @click="router.push('/posts')">
        <el-icon><ChatLineSquare /></el-icon> 经验广场
      </el-button>
    </div>

    <!-- Diary Feed -->
    <div class="feed">
      <div v-if="loading" v-loading="loading" style="min-height:200px" />
      <div v-else-if="diaryList.length === 0" class="empty">
        <el-empty description="还没有日记，快去写第一篇吧">
          <el-button type="primary" @click="router.push('/diary/create')">写日记</el-button>
        </el-empty>
      </div>
      <template v-else>
        <div class="section-title">最新日记</div>
        <el-card v-for="item in diaryList" :key="item.id" class="diary-card" shadow="hover" @click="router.push(`/diary/${item.id}`)">
          <div class="card-header">
            <div class="author">
              <el-avatar :size="40" :src="item.authorAvatar ? baseUrl + item.authorAvatar : undefined" icon="UserFilled" />
              <div class="author-info">
                <span class="author-name">{{ item.authorName }}</span>
                <span class="author-time">{{ item.recordDate || item.createdAt?.slice(0, 10) }}</span>
              </div>
            </div>
            <el-tag v-if="item.babyName" size="small" type="warning">{{ item.babyName }}</el-tag>
          </div>
          <p class="card-preview">{{ item.content?.slice(0, 200) }}</p>
          <div v-if="item.images?.length" class="card-images">
            <el-image
              v-for="(img, idx) in item.images.slice(0, 3)"
              :key="idx"
              :src="baseUrl + img"
              fit="cover"
              class="card-thumb"
              :style="{ width: item.images.length === 1 ? '100%' : 'calc(33.33% - 4px)' }" />
          </div>
          <div class="card-footer">
            <span><el-icon><View /></el-icon> {{ item.viewCount }}</span>
            <span><el-icon><Star /></el-icon> {{ item.likeCount || 0 }}</span>
            <span><el-icon><ChatLineSquare /></el-icon> {{ item.commentCount || 0 }}</span>
          </div>
        </el-card>
        <div class="pagination-wrap" v-if="total > pageSize">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="onPageChange" />
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.home {
  max-width: 680px;
  margin: 0 auto;
  padding: 20px 0;
}

.welcome-banner {
  background: linear-gradient(135deg, #ff9a9e 0%, #ff6b81 50%, #fad0c4 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
}
.banner-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.banner-text h1 {
  font-size: 22px;
  margin: 0;
}
.banner-text p {
  font-size: 13px;
  margin: 4px 0 0;
  opacity: 0.9;
}
.stats-row {
  display: flex;
  gap: 24px;
}
.stat-item {
  text-align: center;
  cursor: pointer;
}
.stat-num {
  display: block;
  font-size: 24px;
  font-weight: 700;
}
.stat-label {
  font-size: 12px;
  opacity: 0.85;
}

.quick-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-left: 4px;
}

.diary-card {
  margin-bottom: 16px;
  cursor: pointer;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}
.author {
  display: flex;
  align-items: center;
  gap: 10px;
}
.author-info {
  display: flex;
  flex-direction: column;
}
.author-name {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}
.author-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
.card-preview {
  font-size: 15px;
  color: #444;
  line-height: 1.7;
  margin-bottom: 12px;
  white-space: pre-wrap;
}

.card-images {
  display: flex;
  gap: 4px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.card-thumb {
  border-radius: 6px;
  aspect-ratio: 1;
  object-fit: cover;
}

.card-footer {
  display: flex;
  gap: 20px;
  color: #999;
  font-size: 13px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}
.card-footer span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-bottom: 20px;
}

.empty {
  margin-top: 60px;
}
</style>
