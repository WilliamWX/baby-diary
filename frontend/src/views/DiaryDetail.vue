<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDiaryDetail, deleteDiary } from '../api/diary'
import { toggleLike, getComments, addComment, toggleBookmark, getInteractStatus } from '../api/post'
import { useAuthStore } from '../stores/auth'
import { BASE_URL } from '../utils/config'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const baseUrl = BASE_URL

const diary = ref(null)
const loading = ref(true)
const comments = ref([])
const commentText = ref('')
const liked = ref(false)
const bookmarked = ref(false)

const previewList = computed(() => diary.value?.images?.map(i => baseUrl + i) || [])

onMounted(async () => {
  try {
    const res = await getDiaryDetail(route.params.id)
    diary.value = res.data
    loadComments()
    loadInteractStatus()
  } catch (e) {
    ElMessage.error('日记加载失败')
  } finally {
    loading.value = false
  }
})

async function loadInteractStatus() {
  try {
    const res = await getInteractStatus('diary', diary.value.id)
    liked.value = res.data.liked || false
    bookmarked.value = res.data.bookmarked || false
  } catch (e) { /* ignored */ }
}

async function loadComments() {
  const res = await getComments('diary', diary.value.id)
  comments.value = res.data || []
}

async function handleLike() {
  try {
    await toggleLike('diary', diary.value.id)
    if (liked.value) {
      diary.value.likeCount = Math.max(0, (diary.value.likeCount || 1) - 1)
    } else {
      diary.value.likeCount = (diary.value.likeCount || 0) + 1
    }
    liked.value = !liked.value
  } catch (e) { /* ignored */ }
}

async function handleBookmark() {
  try {
    await toggleBookmark('diary', diary.value.id)
    bookmarked.value = !bookmarked.value
    ElMessage.success(bookmarked.value ? '已收藏' : '已取消收藏')
  } catch (e) { /* ignored */ }
}

async function submitComment() {
  if (!commentText.value) return
  try {
    await addComment('diary', diary.value.id, commentText.value)
    ElMessage.success('评论成功')
    commentText.value = ''
    loadComments()
  } catch (e) { /* ignored */ }
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除这篇日记吗？', '确认删除', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await deleteDiary(diary.value.id)
    ElMessage.success('已删除')
    router.push('/')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}
</script>

<template>
  <div class="diary-detail" v-loading="loading">
    <el-card v-if="diary" class="diary-card">
      <div class="diary-header">
        <div class="meta">
          <div class="author">
            <el-avatar :size="40" :src="diary.authorAvatar ? baseUrl + diary.authorAvatar : undefined" icon="UserFilled" />
            <div>
              <div class="author-name">{{ diary.authorName }}</div>
              <div class="time">{{ diary.recordDate || diary.createdAt?.slice(0, 10) }} {{ diary.babyName ? '· ' + diary.babyName : '' }}</div>
            </div>
          </div>
          <div class="actions">
            <span class="view-count"><el-icon><View /></el-icon> {{ diary.viewCount }}</span>
            <el-button v-if="auth.user?.id === diary.userId" type="danger" size="small" @click="handleDelete">删除</el-button>
          </div>
        </div>
        <el-tag v-if="diary.visibility === 0" type="warning" size="small">私密</el-tag>
      </div>
      <div class="content">{{ diary.content }}</div>
      <div v-if="diary.images?.length" class="moments-grid" :class="'grid-' + diary.images.length">
        <div v-for="(img, idx) in diary.images" :key="idx" class="grid-item">
          <el-image
            :src="baseUrl + img"
            :preview-src-list="previewList"
            :initial-index="idx"
            fit="cover"
            class="grid-image" />
        </div>
      </div>
      <div class="diary-actions">
        <el-button :type="liked ? 'primary' : 'default'" @click="handleLike" class="like-btn">
          <svg viewBox="0 0 24 24" width="1em" height="1em" :fill="liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg> {{ diary.likeCount || 0 }}
        </el-button>
        <el-button :type="bookmarked ? 'warning' : 'default'" @click="handleBookmark">
          <el-icon><StarFilled v-if="bookmarked" /><Star v-else /></el-icon> {{ bookmarked ? '已收藏' : '收藏' }}
        </el-button>
      </div>
    </el-card>

    <!-- Comments -->
    <el-card v-if="diary" class="comment-card" shadow="hover">
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
  </div>
</template>

<style scoped>
.diary-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;
}
.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}
.author {
  display: flex;
  align-items: center;
  gap: 12px;
}
.author-name {
  font-size: 15px;
  color: #333;
}
.time {
  font-size: 13px;
  color: #999;
  margin-top: 2px;
}
.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.view-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999;
  font-size: 14px;
}
.content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
  margin-bottom: 24px;
}

.diary-card { margin-bottom: 16px; }
.like-btn.el-button--primary { background: #ff6b81; border-color: #ff6b81; }
.like-btn.el-button--primary:hover { background: #e85d72; border-color: #e85d72; }
.diary-actions {
  display: flex;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.comment-card { margin-bottom: 16px; }
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

/* 朋友圈网格 */
.moments-grid {
  display: grid;
  gap: 4px;
  margin-top: 16px;
}
.grid-item {
  overflow: hidden;
  border-radius: 4px;
}
.grid-image {
  width: 100%;
  height: 100%;
  display: block;
}
.grid-image :deep(img) {
  object-fit: cover;
}

/* 1张：单图大图 */
.grid-1 {
  grid-template-columns: 1fr;
  max-width: 400px;
}
.grid-1 .grid-item {
  aspect-ratio: 4 / 3;
}

/* 2张：左右等分 */
.grid-2 {
  grid-template-columns: 1fr 1fr;
  max-width: 400px;
}
.grid-2 .grid-item {
  aspect-ratio: 1 / 1;
}

/* 3张：左1大 右2小 */
.grid-3 {
  grid-template-columns: 1fr 1fr;
  max-width: 400px;
}
.grid-3 .grid-item:first-child {
  grid-row: 1 / 3;
  aspect-ratio: auto;
}
.grid-3 .grid-item {
  aspect-ratio: 1 / 1;
}

/* 4张：2x2 */
.grid-4 {
  grid-template-columns: 1fr 1fr;
  max-width: 400px;
}
.grid-4 .grid-item {
  aspect-ratio: 1 / 1;
}

/* 5-6张：3列 */
.grid-5, .grid-6 {
  grid-template-columns: 1fr 1fr 1fr;
  max-width: 400px;
}
.grid-5 .grid-item, .grid-6 .grid-item {
  aspect-ratio: 1 / 1;
}

/* 7-9张：3列 */
.grid-7, .grid-8, .grid-9 {
  grid-template-columns: 1fr 1fr 1fr;
  max-width: 400px;
}
.grid-7 .grid-item, .grid-8 .grid-item, .grid-9 .grid-item {
  aspect-ratio: 1 / 1;
}
</style>
