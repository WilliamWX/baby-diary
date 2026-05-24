<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPostList, toggleBookmark, toggleLike } from '../api/post'
import { BASE_URL } from '../utils/config'

const router = useRouter()
const baseUrl = BASE_URL
const posts = ref([])
const loading = ref(false)
const category = ref('')
const sort = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const bookmarkedSet = ref(new Set())
const likedSet = ref(new Set())

async function handleLike(p, e) {
  e.stopPropagation()
  try {
    await toggleLike('post', p.id)
    const s = new Set(likedSet.value)
    if (s.has(p.id)) {
      s.delete(p.id)
      p.likeCount = Math.max(0, (p.likeCount || 1) - 1)
    } else {
      s.add(p.id)
      p.likeCount = (p.likeCount || 0) + 1
    }
    likedSet.value = s
  } catch (err) { /* ignored */ }
}

async function handleBookmark(p, e) {
  e.stopPropagation()
  try {
    await toggleBookmark('post', p.id)
    const s = new Set(bookmarkedSet.value)
    if (s.has(p.id)) {
      s.delete(p.id)
      ElMessage.success('已取消收藏')
    } else {
      s.add(p.id)
      ElMessage.success('已收藏')
    }
    bookmarkedSet.value = s
  } catch (err) { /* ignored */ }
}

const categories = [
  { label: '全部', value: '' },
  { label: '喂养', value: '喂养' },
  { label: '睡眠', value: '睡眠' },
  { label: '早教', value: '早教' },
  { label: '健康', value: '健康' },
  { label: '其他', value: '其他' }
]

async function fetchPosts() {
  loading.value = true
  try {
    const res = await getPostList(currentPage.value, pageSize.value, category.value, sort.value)
    posts.value = res.data.records || []
    total.value = res.data.total || 0
    const liked = new Set()
    const bookmarked = new Set()
    posts.value.forEach(p => {
      if (p.liked) liked.add(p.id)
      if (p.bookmarked) bookmarked.add(p.id)
    })
    likedSet.value = liked
    bookmarkedSet.value = bookmarked
  } catch (e) { /* ignored */ }
  finally { loading.value = false }
}

function selectCategory(cat) {
  category.value = cat
  currentPage.value = 1
  fetchPosts()
}

function changeSort(s) {
  sort.value = s
  currentPage.value = 1
  fetchPosts()
}

function onPageChange(page) {
  currentPage.value = page
  fetchPosts()
}

onMounted(fetchPosts)
</script>

<template>
  <div class="posts-page">
    <div class="page-header">
      <h2>经验分享广场</h2>
      <el-button type="primary" @click="router.push('/post/create')">发布经验</el-button>
    </div>
    <div class="categories">
      <el-button
        v-for="c in categories"
        :key="c.value"
        :type="category === c.value ? 'primary' : 'default'"
        size="small"
        @click="selectCategory(c.value)">{{ c.label }}</el-button>
    </div>
    <div class="sort-bar">
      <span class="sort-label">排序：</span>
      <el-button :type="sort === '' ? 'primary' : 'default'" size="small" @click="changeSort('')">最新</el-button>
      <el-button :type="sort === 'popular' ? 'primary' : 'default'" size="small" @click="changeSort('popular')">最热</el-button>
    </div>
    <div v-if="loading" v-loading="loading" style="min-height:200px" />
    <div v-else-if="posts.length === 0" class="empty">
      <el-empty description="暂无帖子" />
    </div>
    <div v-else>
      <el-card v-for="p in posts" :key="p.id" class="post-card" shadow="hover" @click="router.push(`/post/${p.id}`)">
        <div class="card-header">
          <div class="author">
            <el-avatar :size="36" :src="p.authorAvatar ? baseUrl + p.authorAvatar : undefined" icon="UserFilled" />
            <span class="author-name">{{ p.authorName }}</span>
          </div>
          <el-tag size="small">{{ p.category }}</el-tag>
        </div>
        <h3>{{ p.title }}</h3>
        <p class="preview">{{ p.content?.slice(0, 150) }}</p>
        <div class="card-footer">
          <span>{{ p.createdAt?.slice(0, 10) }}</span>
          <span><el-icon><View /></el-icon> {{ p.viewCount }}</span>
          <span class="like-btn" :class="{ active: likedSet.has(p.id) }" @click="handleLike(p, $event)"><svg viewBox="0 0 24 24" width="1em" height="1em" :fill="likedSet.has(p.id) ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg> {{ p.likeCount || 0 }}</span>
          <span><el-icon><ChatLineSquare /></el-icon> {{ p.commentCount || 0 }}</span>
          <span class="bookmark-btn" :class="{ active: bookmarkedSet.has(p.id) }" @click="handleBookmark(p, $event)">
            <svg viewBox="0 0 24 24" width="14" height="14" :fill="bookmarkedSet.has(p.id) ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
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
    </div>
  </div>
</template>

<style scoped>
.posts-page { max-width: 800px; margin: 0 auto; padding: 20px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { color: #ff6b81; font-size: 22px; }
.categories { margin-bottom: 12px; display: flex; gap: 8px; flex-wrap: wrap; }
.sort-bar { margin-bottom: 16px; display: flex; align-items: center; gap: 8px; }
.sort-label { font-size: 13px; color: #999; }
.post-card { margin-bottom: 12px; cursor: pointer; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.author { display: flex; align-items: center; gap: 10px; }
.author-name { font-size: 14px; color: #333; font-weight: 500; }
.post-card h3 { font-size: 17px; margin-bottom: 6px; color: #222; }
.preview { font-size: 14px; color: #666; line-height: 1.6; margin-bottom: 10px; }
.card-footer { display: flex; gap: 16px; color: #aaa; font-size: 13px; padding-top: 8px; border-top: 1px solid #f0f0f0; }
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
