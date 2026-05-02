<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostDetail, updatePost, deletePost, toggleLike, getComments, addComment, toggleBookmark } from '../api/post'
import { useAuthStore } from '../stores/auth'
import { BASE_URL } from '../utils/config'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const post = ref(null)
const comments = ref([])
const commentText = ref('')
const loading = ref(true)
const isEditing = ref(false)
const editForm = ref({ title: '', content: '', category: '' })
const liked = ref(false)
const bookmarked = ref(false)

const isOwner = computed(() => post.value && auth.user && post.value.userId === auth.user.id)
const baseUrl = BASE_URL

const categories = ['喂养', '睡眠', '早教', '健康', '其他']

onMounted(async () => {
  try {
    const res = await getPostDetail(route.params.id)
    post.value = res.data
    loadComments()
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
})

async function loadComments() {
  const res = await getComments('post', post.value.id)
  comments.value = res.data || []
}

async function handleLike() {
  try {
    await toggleLike('post', post.value.id)
    if (liked.value) {
      post.value.likeCount = Math.max(0, (post.value.likeCount || 1) - 1)
    } else {
      post.value.likeCount = (post.value.likeCount || 0) + 1
    }
    liked.value = !liked.value
  } catch (e) { /* ignored */ }
}

async function handleBookmark() {
  try {
    await toggleBookmark('post', post.value.id)
    bookmarked.value = !bookmarked.value
    ElMessage.success(bookmarked.value ? '已收藏' : '已取消收藏')
  } catch (e) { /* ignored */ }
}

async function submitComment() {
  if (!commentText.value) return
  try {
    await addComment('post', post.value.id, commentText.value)
    ElMessage.success('评论成功')
    commentText.value = ''
    loadComments()
  } catch (e) { /* ignored */ }
}

function startEdit() {
  editForm.value = {
    title: post.value.title,
    content: post.value.content,
    category: post.value.category
  }
  isEditing.value = true
}

function cancelEdit() { isEditing.value = false }

async function handleSave() {
  try {
    await updatePost(post.value.id, editForm.value)
    post.value.title = editForm.value.title
    post.value.content = editForm.value.content
    post.value.category = editForm.value.category
    isEditing.value = false
    ElMessage.success('更新成功')
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除这篇经验分享吗？', '确认删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePost(post.value.id)
    ElMessage.success('删除成功')
    router.push('/posts')
  } catch (e) { /* cancelled */ }
}
</script>

<template>
  <div class="post-detail" v-loading="loading">
    <template v-if="post && !isEditing">
      <!-- Post Header -->
      <el-card class="post-main" shadow="hover">
        <div class="post-header">
          <div class="author-row">
            <el-avatar :size="44" :src="post.authorAvatar ? baseUrl + post.authorAvatar : undefined" icon="UserFilled" />
            <div class="author-info">
              <div class="author-name">{{ post.authorName }}</div>
              <div class="post-meta">
                <span>{{ post.createdAt?.slice(0, 10) }}</span>
                <span>· {{ post.viewCount }} 阅读</span>
              </div>
            </div>
          </div>
          <el-tag size="small" effect="plain">{{ post.category }}</el-tag>
        </div>
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-content">{{ post.content }}</div>
        <div class="post-actions">
          <el-button :type="liked ? 'primary' : 'default'" @click="handleLike">
            <el-icon><Star /></el-icon> {{ post.likeCount || 0 }}
          </el-button>
          <el-button :type="bookmarked ? 'warning' : 'default'" @click="handleBookmark">
            <el-icon><Collection /></el-icon> {{ bookmarked ? '已收藏' : '收藏' }}
          </el-button>
          <template v-if="isOwner">
            <el-button type="warning" @click="startEdit">编辑</el-button>
            <el-button type="danger" @click="handleDelete">删除</el-button>
          </template>
        </div>
      </el-card>

      <!-- Comments -->
      <el-card class="comment-card" shadow="hover">
        <div class="comment-section-title">
          <h3>评论 ({{ comments.length }})</h3>
        </div>
        <div class="comment-input-wrap">
          <el-avatar :size="32" :src="auth.user?.avatar ? baseUrl + auth.user.avatar : undefined" icon="UserFilled" class="comment-avatar" />
          <div class="comment-input-area">
            <el-input v-model="commentText" type="textarea" :rows="2" placeholder="写下你的评论..." />
            <el-button type="primary" size="small" class="submit-btn" @click="submitComment">发表</el-button>
          </div>
        </div>
        <div v-if="comments.length" class="comment-list">
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <el-avatar :size="32" :src="c.avatar ? baseUrl + c.avatar : undefined" icon="UserFilled" />
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-author">{{ c.username }}</span>
                <span class="comment-time">{{ c.createdAt?.slice(0, 10) }}</span>
              </div>
              <p class="comment-content">{{ c.content }}</p>
            </div>
          </div>
        </div>
      </el-card>
    </template>

    <!-- Edit Mode -->
    <el-card v-if="isEditing" class="edit-card" shadow="hover">
      <h2>编辑经验分享</h2>
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editForm.category">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="editForm.content" type="textarea" :rows="10" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存</el-button>
          <el-button @click="cancelEdit">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.post-detail { max-width: 800px; margin: 0 auto; padding: 20px 0; }

.post-main, .comment-card, .edit-card { margin-bottom: 16px; }

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}
.author-row { display: flex; align-items: center; gap: 12px; }
.author-info { display: flex; flex-direction: column; }
.author-name { font-size: 15px; color: #333; font-weight: 500; }
.post-meta { font-size: 12px; color: #bbb; margin-top: 2px; }

.post-title { font-size: 22px; margin-bottom: 16px; color: #222; line-height: 1.4; }
.post-content { font-size: 16px; line-height: 1.8; color: #333; white-space: pre-wrap; margin-bottom: 20px; }
.post-actions { display: flex; gap: 8px; flex-wrap: wrap; padding-top: 16px; border-top: 1px solid #f0f0f0; }

.comment-section-title { margin-bottom: 16px; }
.comment-section-title h3 { font-size: 16px; color: #333; margin: 0; }

.comment-input-wrap { display: flex; gap: 12px; margin-bottom: 20px; }
.comment-avatar { flex-shrink: 0; }
.comment-input-area { flex: 1; }
.submit-btn { margin-top: 8px; }

.comment-list { border-top: 1px solid #f5f5f5; padding-top: 8px; }
.comment-item { display: flex; gap: 10px; padding: 14px 0; border-bottom: 1px solid #fafafa; }
.comment-body { flex: 1; }
.comment-header { margin-bottom: 4px; }
.comment-author { font-size: 13px; color: #333; font-weight: 600; }
.comment-time { font-size: 11px; color: #ccc; margin-left: 10px; }
.comment-content { font-size: 14px; color: #555; line-height: 1.6; margin: 0; }

.edit-card h2 { font-size: 18px; margin-bottom: 16px; color: #333; }
</style>
