<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMomentDetail, updateMoment, deleteMoment, uploadVideo } from '../api/moment'
import { toggleLike, getComments, addComment, toggleBookmark, getInteractStatus } from '../api/post'
import { useAuthStore } from '../stores/auth'
import { BASE_URL } from '../utils/config'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const moment = ref(null)
const comments = ref([])
const commentText = ref('')
const loading = ref(true)
const isEditing = ref(false)
const editForm = ref({ description: '', babyId: null })
const liked = ref(false)
const bookmarked = ref(false)
const uploadingVideo = ref(false)
const videoRef = ref(null)
const isFullscreen = ref(false)

const isOwner = computed(() => moment.value && auth.user && moment.value.userId === auth.user.id)
const baseUrl = BASE_URL

onMounted(async () => {
  try {
    const res = await getMomentDetail(route.params.id)
    moment.value = res.data
    loadComments()
    loadInteractStatus()
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
  document.addEventListener('fullscreenchange', onFullscreenChange)
  document.addEventListener('webkitfullscreenchange', onFullscreenChange)
})

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', onFullscreenChange)
  document.removeEventListener('webkitfullscreenchange', onFullscreenChange)
})

function onFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement
}

async function toggleFullscreen() {
  const el = videoRef.value
  if (!el) return
  try {
    if (!document.fullscreenElement) {
      await el.requestFullscreen()
    } else {
      await document.exitFullscreen()
    }
  } catch (e) { /* ignored */ }
}

async function loadComments() {
  const res = await getComments('moment', moment.value.id)
  comments.value = res.data || []
}

async function loadInteractStatus() {
  try {
    const res = await getInteractStatus('moment', moment.value.id)
    liked.value = res.data.liked || false
    bookmarked.value = res.data.bookmarked || false
  } catch (e) { /* ignored */ }
}

async function handleLike() {
  try {
    await toggleLike('moment', moment.value.id)
    if (liked.value) {
      moment.value.likeCount = Math.max(0, (moment.value.likeCount || 1) - 1)
    } else {
      moment.value.likeCount = (moment.value.likeCount || 0) + 1
    }
    liked.value = !liked.value
  } catch (e) { /* ignored */ }
}

async function handleBookmark() {
  try {
    await toggleBookmark('moment', moment.value.id)
    bookmarked.value = !bookmarked.value
    ElMessage.success(bookmarked.value ? '已收藏' : '已取消收藏')
  } catch (e) { /* ignored */ }
}

async function submitComment() {
  if (!commentText.value) return
  try {
    await addComment('moment', moment.value.id, commentText.value)
    ElMessage.success('评论成功')
    commentText.value = ''
    loadComments()
  } catch (e) { /* ignored */ }
}

function startEdit() {
  editForm.value = {
    description: moment.value.description,
    babyId: moment.value.babyId,
    videoUrl: moment.value.videoUrl,
    coverUrl: moment.value.coverUrl
  }
  isEditing.value = true
}

function cancelEdit() { isEditing.value = false }

async function handleSave() {
  try {
    await updateMoment(moment.value.id, editForm.value)
    moment.value.description = editForm.value.description
    moment.value.babyId = editForm.value.babyId
    moment.value.videoUrl = editForm.value.videoUrl
    moment.value.coverUrl = editForm.value.coverUrl
    isEditing.value = false
    ElMessage.success('更新成功')
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除这个精彩时刻吗？', '确认删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteMoment(moment.value.id)
    ElMessage.success('删除成功')
    router.push('/moments')
  } catch (e) { /* cancelled */ }
}

async function handleVideoChange(file) {
  uploadingVideo.value = true
  try {
    const res = await uploadVideo(file.raw)
    editForm.value.videoUrl = res.data.url
    editForm.value.coverUrl = res.data.coverUrl || ''
    moment.value.videoUrl = res.data.url
    moment.value.coverUrl = res.data.coverUrl || ''
    ElMessage.success('视频更新成功')
  } catch (e) {
    ElMessage.error('视频上传失败')
  } finally {
    uploadingVideo.value = false
  }
}

</script>

<template>
  <div class="moment-detail" v-loading="loading">
    <template v-if="moment && !isEditing">
      <!-- Video Player -->
      <div class="video-player-wrap">
        <video
          ref="videoRef"
          :src="baseUrl + moment.videoUrl"
          :poster="moment.coverUrl ? baseUrl + moment.coverUrl : undefined"
          controls
          playsinline
          class="detail-video"
          @dblclick="toggleFullscreen" />
        <div class="fullscreen-btn" @click="toggleFullscreen">
          <el-icon :size="18"><FullScreen v-if="!isFullscreen" /><Aim v-else /></el-icon>
        </div>
      </div>

      <!-- Info Card -->
      <el-card class="info-card" shadow="hover">
        <div class="moment-header">
          <div class="author-row">
            <el-avatar :size="44" :src="moment.authorAvatar ? baseUrl + moment.authorAvatar : undefined" icon="UserFilled" />
            <div class="author-info">
              <div class="author-name">{{ moment.authorName }}</div>
              <div class="moment-meta">
                <span>{{ moment.createdAt?.slice(0, 10) }}</span>
                <span>· {{ moment.viewCount || 0 }} 次播放</span>
                <span v-if="moment.babyName">· {{ moment.babyName }}</span>
              </div>
            </div>
          </div>
        </div>
        <p class="moment-desc">{{ moment.description || '无描述' }}</p>
        <div class="moment-actions">
          <el-button :type="liked ? 'primary' : 'default'" @click="handleLike">
            <svg viewBox="0 0 24 24" width="1em" height="1em" :fill="liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg> {{ moment.likeCount || 0 }}
          </el-button>
          <el-button :type="bookmarked ? 'warning' : 'default'" @click="handleBookmark">
            <el-icon><StarFilled v-if="bookmarked" /><Star v-else /></el-icon> {{ bookmarked ? '已收藏' : '收藏' }}
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
      <h2>编辑精彩时刻</h2>
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="4"
            placeholder="记录这精彩的一刻..."
            maxlength="500"
            show-word-limit />
        </el-form-item>
        <el-form-item label="更换视频">
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            accept="video/mp4,video/mov,video/webm"
            @change="handleVideoChange"
            drag>
            <div class="upload-placeholder">
              <p v-if="!uploadingVideo">点击或拖拽更换视频</p>
              <p v-else>上传中...</p>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="uploadingVideo" @click="handleSave">保存</el-button>
          <el-button @click="cancelEdit">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.moment-detail { max-width: 800px; margin: 0 auto; padding: 20px 0; }

.video-card, .info-card, .comment-card, .edit-card { margin-bottom: 16px; }

.video-player-wrap {
  position: relative;
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
  background: #000;
}

.detail-video {
  display: block;
  width: 100%;
  max-height: 70vh;
}

.fullscreen-btn {
  position: absolute;
  bottom: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  z-index: 10;
}
.fullscreen-btn:hover { background: rgba(0,0,0,0.7); }

.detail-video:fullscreen {
  max-height: none;
  object-fit: contain;
}
.detail-video::-webkit-media-controls-fullscreen-button { display: none; }

.moment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}
.author-row { display: flex; align-items: center; gap: 12px; }
.author-info { display: flex; flex-direction: column; }
.author-name { font-size: 15px; color: #333; font-weight: 500; }
.moment-meta { font-size: 12px; color: #bbb; margin-top: 2px; }
.moment-meta span + span { margin-left: 0; }

.moment-desc {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
  margin-bottom: 16px;
}
.moment-actions { display: flex; gap: 8px; flex-wrap: wrap; padding-top: 16px; border-top: 1px solid #f0f0f0; }
.moment-actions .el-button--primary { background: #ff6b81; border-color: #ff6b81; }
.moment-actions .el-button--primary:hover { background: #e85d72; border-color: #e85d72; }

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

.upload-placeholder {
  padding: 30px 20px;
  text-align: center;
  color: #999;
}
</style>
