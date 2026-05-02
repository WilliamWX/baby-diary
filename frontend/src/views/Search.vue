<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getDiaryList } from '../api/diary'
import { getPostList } from '../api/post'

import { BASE_URL } from '../utils/config'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const diaryResults = ref([])
const postResults = ref([])
const loading = ref(false)
const baseUrl = BASE_URL

async function doSearch() {
  const q = keyword.value.trim()
  if (!q) return
  loading.value = true
  try {
    const [diaryRes, postRes] = await Promise.all([
      getDiaryList(1, 10, null, q),
      getPostList(1, 10, '', q)
    ])
    diaryResults.value = diaryRes.data.records || []
    postResults.value = postRes.data.records || []
  } catch (e) { /* ignored */ }
  finally { loading.value = false }
}

onMounted(() => {
  keyword.value = route.query.q || ''
  if (keyword.value) doSearch()
})

watch(() => route.query.q, (val) => {
  if (val) {
    keyword.value = val
    doSearch()
  }
})
</script>

<template>
  <div class="search-page">
    <div class="search-header">
      <h2>搜索"{{ keyword }}"</h2>
      <span class="result-count">{{ diaryResults.length + postResults.length }} 条结果</span>
    </div>

    <div v-loading="loading" style="min-height:100px" />

    <template v-if="!loading">
      <!-- Diary Results -->
      <div v-if="diaryResults.length" class="result-section">
        <h3 class="section-title">日记 ({{ diaryResults.length }})</h3>
        <el-card v-for="d in diaryResults" :key="'d'+d.id" class="result-card" shadow="hover" @click="router.push(`/diary/${d.id}`)">
          <div class="card-header">
            <div class="author">
              <el-avatar :size="32" :src="d.authorAvatar ? baseUrl + d.authorAvatar : undefined" icon="UserFilled" />
              <span>{{ d.authorName }}</span>
            </div>
            <span class="date">{{ d.recordDate || d.createdAt?.slice(0, 10) }}</span>
          </div>
          <p class="excerpt">{{ d.content?.slice(0, 180) }}</p>
        </el-card>
      </div>

      <!-- Post Results -->
      <div v-if="postResults.length" class="result-section">
        <h3 class="section-title">经验帖 ({{ postResults.length }})</h3>
        <el-card v-for="p in postResults" :key="'p'+p.id" class="result-card" shadow="hover" @click="router.push(`/post/${p.id}`)">
          <div class="card-header">
            <div class="author">
              <el-avatar :size="32" :src="p.authorAvatar ? baseUrl + p.authorAvatar : undefined" icon="UserFilled" />
              <span>{{ p.authorName }}</span>
            </div>
            <el-tag size="small">{{ p.category }}</el-tag>
          </div>
          <h4 class="post-title">{{ p.title }}</h4>
          <p class="excerpt">{{ p.content?.slice(0, 150) }}</p>
        </el-card>
      </div>

      <div v-if="!diaryResults.length && !postResults.length" class="empty">
        <el-empty description="未找到相关内容" />
      </div>
    </template>
  </div>
</template>

<style scoped>
.search-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 20px 0;
}
.search-header {
  margin-bottom: 20px;
}
.search-header h2 {
  font-size: 22px;
  color: #333;
  margin-bottom: 4px;
}
.result-count {
  font-size: 13px;
  color: #999;
}

.result-section {
  margin-bottom: 28px;
}
.section-title {
  font-size: 16px;
  color: #ff6b81;
  margin-bottom: 12px;
  padding-left: 4px;
}

.result-card {
  margin-bottom: 10px;
  cursor: pointer;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.author {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #333;
}
.date {
  font-size: 12px;
  color: #bbb;
}
.post-title {
  font-size: 15px;
  margin: 0 0 6px;
  color: #222;
}
.excerpt {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0;
}
.empty {
  margin-top: 60px;
}
</style>
