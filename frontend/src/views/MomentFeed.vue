<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMomentList } from '../api/moment'
import { toggleLike, toggleBookmark } from '../api/post'
import { BASE_URL } from '../utils/config'

const router = useRouter()
const moments = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const baseUrl = BASE_URL
const likedSet = ref(new Set())
const bookmarkedSet = ref(new Set())

async function handleLike(m, e) {
  e.stopPropagation()
  try {
    await toggleLike('moment', m.id)
    const s = new Set(likedSet.value)
    if (s.has(m.id)) {
      s.delete(m.id)
      m.likeCount = Math.max(0, (m.likeCount || 1) - 1)
    } else {
      s.add(m.id)
      m.likeCount = (m.likeCount || 0) + 1
    }
    likedSet.value = s
  } catch (err) { /* ignored */ }
}

async function handleBookmark(m, e) {
  e.stopPropagation()
  try {
    await toggleBookmark('moment', m.id)
    const s = new Set(bookmarkedSet.value)
    if (s.has(m.id)) {
      s.delete(m.id)
      ElMessage.success('已取消收藏')
    } else {
      s.add(m.id)
      ElMessage.success('已收藏')
    }
    bookmarkedSet.value = s
  } catch (err) { /* ignored */ }
}

async function fetchMoments() {
  loading.value = true
  try {
    const res = await getMomentList(currentPage.value, pageSize.value)
    moments.value = res.data.records || []
    total.value = res.data.total || 0
    const liked = new Set()
    const bookmarked = new Set()
    moments.value.forEach(m => {
      if (m.liked) liked.add(m.id)
      if (m.bookmarked) bookmarked.add(m.id)
    })
    likedSet.value = liked
    bookmarkedSet.value = bookmarked
  } catch (e) { /* ignored */ }
  finally { loading.value = false }
}

function onPageChange(page) {
  currentPage.value = page
  fetchMoments()
}

onMounted(fetchMoments)
</script>

<template>
  <div class="moment-feed">
    <div class="page-header">
      <h2>精彩时刻</h2>
      <el-button type="primary" @click="router.push('/moment/create')">
        <el-icon><VideoCamera /></el-icon> 发布视频
      </el-button>
    </div>

    <div v-if="loading" v-loading="loading" style="min-height:300px" />
    <div v-else-if="moments.length === 0" class="empty">
      <el-empty description="还没有精彩时刻，快去发布第一个视频吧">
        <el-button type="primary" @click="router.push('/moment/create')">发布视频</el-button>
      </el-empty>
    </div>
    <div v-else class="moment-grid">
      <el-card
        v-for="m in moments"
        :key="m.id"
        class="moment-card"
        shadow="hover"
        @click="router.push(`/moment/${m.id}`)">
        <div class="card-cover">
          <el-image v-if="m.coverUrl" :src="baseUrl + m.coverUrl" fit="cover" class="cover-img" />
          <div v-else class="cover-placeholder">
            <el-icon :size="40"><VideoPlay /></el-icon>
          </div>
          <div class="play-icon-overlay">
            <el-icon :size="28"><VideoPlay /></el-icon>
          </div>
        </div>
        <div class="card-body">
          <p class="card-desc">{{ m.description?.slice(0, 60) || '无描述' }}</p>
          <div class="card-meta">
            <div class="meta-author">
              <el-avatar :size="24" :src="m.authorAvatar ? baseUrl + m.authorAvatar : undefined" icon="UserFilled" />
              <span>{{ m.authorName }}</span>
            </div>
            <div class="meta-stats">
              <span class="like-btn" :class="{ active: likedSet.has(m.id) }" @click="handleLike(m, $event)"><svg viewBox="0 0 24 24" width="1em" height="1em" :fill="likedSet.has(m.id) ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg> {{ m.likeCount || 0 }}</span>
              <span><el-icon><ChatLineSquare /></el-icon> {{ m.commentCount || 0 }}</span>
              <span class="bookmark-btn" :class="{ active: bookmarkedSet.has(m.id) }" @click="handleBookmark(m, $event)">
                <svg viewBox="0 0 24 24" width="14" height="14" :fill="bookmarkedSet.has(m.id) ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                收藏
              </span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="onPageChange" />
    </div>
  </div>
</template>

<style scoped>
.moment-feed { max-width: 960px; margin: 0 auto; padding: 20px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { color: #ff6b81; font-size: 22px; }

.moment-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.moment-card { cursor: pointer; overflow: hidden; }
.moment-card:hover { transform: translateY(-2px); transition: transform 0.2s; }

.card-cover {
  position: relative;
  aspect-ratio: 3 / 4;
  overflow: hidden;
  background: #f0f0f0;
}
.cover-img {
  width: 100%;
  height: 100%;
}
.cover-img :deep(img) { object-fit: cover; }

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
}
.play-icon-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.card-body { padding: 10px 12px; }
.card-desc {
  font-size: 13px;
  color: #555;
  line-height: 1.5;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.meta-author { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #888; }
.meta-stats { display: flex; gap: 10px; font-size: 12px; color: #bbb; }
.meta-stats span { display: flex; align-items: center; gap: 2px; }
.like-btn { cursor: pointer; }
.like-btn.active { color: #ff6b81; }
.like-btn:hover { color: #ff6b81; }
.bookmark-btn { cursor: pointer; font-size: 12px; }
.bookmark-btn.active { color: #f5a623; }
.bookmark-btn:hover { color: #f5a623; }

.empty { margin-top: 80px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 24px; padding-bottom: 20px; }

@media (max-width: 768px) {
  .moment-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 480px) {
  .moment-grid { grid-template-columns: 1fr; }
}
</style>
