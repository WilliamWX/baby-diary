<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDiaryList } from '../api/diary'
import { toggleBookmark, toggleLike } from '../api/post'
import { BASE_URL } from '../utils/config'

const router = useRouter()
const baseUrl = BASE_URL

const diaryList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const bookmarkedSet = ref(new Set())
const likedSet = ref(new Set())

async function handleLike(item, e) {
  e.stopPropagation()
  try {
    await toggleLike('diary', item.id)
    const s = new Set(likedSet.value)
    if (s.has(item.id)) {
      s.delete(item.id)
      item.likeCount = Math.max(0, (item.likeCount || 1) - 1)
    } else {
      s.add(item.id)
      item.likeCount = (item.likeCount || 0) + 1
    }
    likedSet.value = s
  } catch (err) { /* ignored */ }
}

async function handleBookmark(item, e) {
  e.stopPropagation()
  try {
    await toggleBookmark('diary', item.id)
    const s = new Set(bookmarkedSet.value)
    if (s.has(item.id)) {
      s.delete(item.id)
      ElMessage.success('已取消收藏')
    } else {
      s.add(item.id)
      ElMessage.success('已收藏')
    }
    bookmarkedSet.value = s
  } catch (err) { /* ignored */ }
}

async function fetchDiaries() {
  loading.value = true
  try {
    const res = await getDiaryList(currentPage.value, pageSize.value)
    diaryList.value = res.data.records || []
    total.value = res.data.total || 0
    const liked = new Set()
    const bookmarked = new Set()
    diaryList.value.forEach(item => {
      if (item.liked) liked.add(item.id)
      if (item.bookmarked) bookmarked.add(item.id)
    })
    likedSet.value = liked
    bookmarkedSet.value = bookmarked
  } catch (e) { /* ignored */ }
  finally { loading.value = false }
}

function onPageChange(page) {
  currentPage.value = page
  fetchDiaries()
}

onMounted(fetchDiaries)
</script>

<template>
  <div class="diary-list-page">
    <div class="page-header">
      <h2>日记</h2>
      <el-button type="primary" @click="router.push('/diary/create')">
        <el-icon><Edit /></el-icon> 写日记
      </el-button>
    </div>

    <div v-if="loading" v-loading="loading" style="min-height:200px" />
    <div v-else-if="diaryList.length === 0" class="empty">
      <el-empty description="还没有日记，快去写第一篇吧">
        <el-button type="primary" @click="router.push('/diary/create')">写日记</el-button>
      </el-empty>
    </div>
    <template v-else>
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
          <span class="like-btn" :class="{ active: likedSet.has(item.id) }" @click="handleLike(item, $event)">
            <svg viewBox="0 0 24 24" width="1em" height="1em" :fill="likedSet.has(item.id) ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg> {{ item.likeCount || 0 }}
          </span>
          <span><el-icon><ChatLineSquare /></el-icon> {{ item.commentCount || 0 }}</span>
          <span class="bookmark-btn" :class="{ active: bookmarkedSet.has(item.id) }" @click="handleBookmark(item, $event)">
            <svg viewBox="0 0 24 24" width="14" height="14" :fill="bookmarkedSet.has(item.id) ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
            收藏
          </span>
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
</template>

<style scoped>
.diary-list-page {
  max-width: 680px;
  margin: 0 auto;
  padding: 20px 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 { color: #ff6b81; font-size: 22px; }

.diary-card { margin-bottom: 16px; cursor: pointer; }
.card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
.author { display: flex; align-items: center; gap: 10px; }
.author-info { display: flex; flex-direction: column; }
.author-name { font-size: 15px; color: #333; font-weight: 500; }
.author-time { font-size: 12px; color: #999; margin-top: 2px; }
.card-preview {
  font-size: 15px; color: #444; line-height: 1.7; margin-bottom: 12px; white-space: pre-wrap;
}
.card-images { display: flex; gap: 4px; margin-bottom: 12px; flex-wrap: wrap; }
.card-thumb { border-radius: 6px; aspect-ratio: 1; object-fit: cover; }
.card-footer {
  display: flex; gap: 20px; color: #999; font-size: 13px;
  padding-top: 8px; border-top: 1px solid #f0f0f0;
}
.card-footer span { display: flex; align-items: center; gap: 4px; }
.like-btn { cursor: pointer; }
.like-btn.active { color: #ff6b81; }
.like-btn:hover { color: #ff6b81; }
.bookmark-btn { cursor: pointer; color: #999; margin-left: auto; font-size: 12px; }
.bookmark-btn.active { color: #f5a623; }
.bookmark-btn:hover { color: #f5a623; }
.empty { margin-top: 60px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 24px; padding-bottom: 20px; }
</style>
