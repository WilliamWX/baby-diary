<script setup>
import { ref, onMounted } from 'vue'
import request from '../api'

const diaryList = ref([])
const loading = ref(false)

async function fetchDiaries() {
  loading.value = true
  try {
    const res = await request.get('/diary/?page=1&size=10')
    diaryList.value = res.data.records || []
  } catch (e) {
    // ignored
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDiaries()
})
</script>

<template>
  <div class="home">
    <div class="feed">
      <div v-if="loading" v-loading="loading" style="min-height:200px" />
      <div v-else-if="diaryList.length === 0" class="empty">
        <el-empty description="还没有日记，快去写第一篇吧" />
      </div>
      <div v-else class="diary-list">
        <el-card v-for="item in diaryList" :key="item.id" class="diary-card" @click="$router.push(`/diary/${item.id}`)">
          <div class="card-header">
            <div class="author">
              <el-avatar :size="36" :src="item.authorAvatar ? 'http://localhost:9000' + item.authorAvatar : undefined" icon="UserFilled" />
              <span>{{ item.authorName }}</span>
            </div>
            <span class="time">{{ item.createdAt?.slice(0, 10) }}</span>
          </div>
          <p class="card-preview">{{ item.content?.replace(/<[^>]+>/g, '').slice(0, 150) }}</p>
          <div class="card-footer">
            <span><el-icon><View /></el-icon> {{ item.viewCount }}</span>
            <span><el-icon><ChatLineSquare /></el-icon> {{ item.commentCount || 0 }}</span>
            <span><el-icon><Star /></el-icon> {{ item.likeCount || 0 }}</span>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home {
  max-width: 680px;
  margin: 0 auto;
  padding: 20px 0;
}
.diary-card {
  margin-bottom: 16px;
  cursor: pointer;
}
.diary-card:hover {
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.author {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #333;
}
.time {
  font-size: 12px;
  color: #999;
}
.card-title {
  font-size: 18px;
  margin-bottom: 8px;
  color: #222;
}
.card-preview {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}
.card-footer {
  display: flex;
  gap: 20px;
  color: #999;
  font-size: 13px;
}
.card-footer span {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
