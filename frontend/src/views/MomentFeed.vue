<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMomentList } from '../api/moment'
import { BASE_URL } from '../utils/config'

const router = useRouter()
const moments = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const baseUrl = BASE_URL

async function fetchMoments() {
  loading.value = true
  try {
    const res = await getMomentList(currentPage.value, pageSize.value)
    moments.value = res.data.records || []
    total.value = res.data.total || 0
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
              <span><el-icon><Star /></el-icon> {{ m.likeCount || 0 }}</span>
              <span><el-icon><ChatLineSquare /></el-icon> {{ m.commentCount || 0 }}</span>
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

.empty { margin-top: 80px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 24px; padding-bottom: 20px; }

@media (max-width: 768px) {
  .moment-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 480px) {
  .moment-grid { grid-template-columns: 1fr; }
}
</style>
