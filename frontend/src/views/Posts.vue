<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPostList } from '../api/post'
import { BASE_URL } from '../utils/config'

const router = useRouter()
const baseUrl = BASE_URL
const posts = ref([])
const loading = ref(false)
const category = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

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
    const res = await getPostList(currentPage.value, pageSize.value, category.value)
    posts.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* ignored */ }
  finally { loading.value = false }
}

function selectCategory(cat) {
  category.value = cat
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
          <span><el-icon><Star /></el-icon> {{ p.likeCount || 0 }}</span>
          <span><el-icon><ChatLineSquare /></el-icon> {{ p.commentCount || 0 }}</span>
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
.categories { margin-bottom: 16px; display: flex; gap: 8px; flex-wrap: wrap; }
.post-card { margin-bottom: 12px; cursor: pointer; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.author { display: flex; align-items: center; gap: 10px; }
.author-name { font-size: 14px; color: #333; font-weight: 500; }
.post-card h3 { font-size: 17px; margin-bottom: 6px; color: #222; }
.preview { font-size: 14px; color: #666; line-height: 1.6; margin-bottom: 10px; }
.card-footer { display: flex; gap: 16px; color: #aaa; font-size: 13px; padding-top: 8px; border-top: 1px solid #f0f0f0; }
.card-footer span { display: flex; align-items: center; gap: 4px; }
.empty { margin-top: 60px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 24px; padding-bottom: 20px; }
</style>
