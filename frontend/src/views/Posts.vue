<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPostList } from '../api/post'

const router = useRouter()
const posts = ref([])
const loading = ref(false)
const category = ref('')

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
    const res = await getPostList(1, 20, category.value)
    posts.value = res.data.records || []
  } catch (e) { /* ignored */ }
  finally { loading.value = false }
}

function selectCategory(cat) {
  category.value = cat
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
    <div v-else>
      <el-card v-for="p in posts" :key="p.id" class="post-card" @click="router.push(`/post/${p.id}`)">
        <div class="card-header">
          <span class="author">{{ p.authorName }}</span>
          <el-tag size="small">{{ p.category }}</el-tag>
        </div>
        <h3>{{ p.title }}</h3>
        <p class="preview">{{ p.content?.slice(0, 150) }}</p>
        <div class="card-footer">
          <span>{{ p.createdAt?.slice(0, 10) }}</span>
          <span><el-icon><View /></el-icon> {{ p.viewCount }}</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.posts-page { max-width: 800px; margin: 0 auto; padding: 20px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { color: #ff6b81; font-size: 22px; }
.categories { margin-bottom: 16px; display: flex; gap: 8px; }
.post-card { margin-bottom: 12px; cursor: pointer; }
.post-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.card-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
.author { font-size: 14px; color: #666; }
.post-card h3 { font-size: 17px; margin-bottom: 6px; }
.preview { font-size: 14px; color: #999; margin-bottom: 10px; }
.card-footer { display: flex; gap: 16px; color: #aaa; font-size: 13px; }
</style>
