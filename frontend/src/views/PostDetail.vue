<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostDetail, updatePost, deletePost, toggleLike, getComments, addComment } from '../api/post'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const post = ref(null)
const comments = ref([])
const commentText = ref('')
const loading = ref(true)
const isEditing = ref(false)
const editForm = ref({ title: '', content: '', category: '' })

const isOwner = computed(() => post.value && auth.user && post.value.userId === auth.user.id)

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
    post.value.likeCount = (post.value.likeCount || 0) + 1
    ElMessage.success('点赞成功')
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

function cancelEdit() {
  isEditing.value = false
}

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
    await ElMessageBox.confirm('确定要删除这篇经验分享吗？此操作不可恢复。', '确认删除', {
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
    <el-card v-if="post && !isEditing">
      <h1 class="title">{{ post.title }}</h1>
      <div class="meta">
        <span>{{ post.authorName }}</span>
        <span>{{ post.createdAt?.slice(0, 10) }}</span>
        <el-tag size="small">{{ post.category }}</el-tag>
      </div>
      <div class="content">{{ post.content }}</div>
      <div class="actions">
        <el-button @click="handleLike"><el-icon><Star /></el-icon> 点赞 {{ post.likeCount || 0 }}</el-button>
        <template v-if="isOwner">
          <el-button type="warning" @click="startEdit">编辑</el-button>
          <el-button type="danger" @click="handleDelete">删除</el-button>
        </template>
      </div>
    </el-card>

    <el-card v-if="isEditing">
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

    <el-card v-if="post && !isEditing" style="margin-top:16px">
      <h3>评论 ({{ comments.length }})</h3>
      <div class="comment-input">
        <el-input v-model="commentText" type="textarea" :rows="2" placeholder="写下你的评论..." />
        <el-button type="primary" size="small" style="margin-top:8px" @click="submitComment">发表</el-button>
      </div>
      <div v-for="c in comments" :key="c.id" class="comment-item">
        <div class="comment-header">
          <span class="comment-author">{{ c.username }}</span>
          <span class="comment-time">{{ c.createdAt?.slice(0, 10) }}</span>
        </div>
        <p class="comment-content">{{ c.content }}</p>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.post-detail { max-width: 800px; margin: 0 auto; padding: 20px 0; }
.title { font-size: 24px; margin-bottom: 12px; }
.meta { display: flex; gap: 16px; color: #999; font-size: 14px; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #eee; }
.content { font-size: 16px; line-height: 1.8; color: #333; white-space: pre-wrap; margin-bottom: 16px; }
.actions { margin-bottom: 8px; display: flex; gap: 8px; flex-wrap: wrap; }
.comment-input { margin-bottom: 16px; }
.comment-item { padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.comment-header { margin-bottom: 4px; }
.comment-author { font-size: 14px; color: #333; font-weight: bold; }
.comment-time { font-size: 12px; color: #ccc; margin-left: 12px; }
.comment-content { font-size: 14px; color: #555; }
</style>
