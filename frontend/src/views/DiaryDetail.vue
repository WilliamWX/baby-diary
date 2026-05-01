<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDiaryDetail, deleteDiary } from '../api/diary'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const baseUrl = 'http://localhost:9000'

const diary = ref(null)
const loading = ref(true)

const previewList = computed(() => diary.value?.images?.map(i => baseUrl + i) || [])

onMounted(async () => {
  try {
    const res = await getDiaryDetail(route.params.id)
    diary.value = res.data
  } catch (e) {
    ElMessage.error('日记加载失败')
  } finally {
    loading.value = false
  }
})

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
    <el-card v-if="diary">
      <div class="diary-header">
        <div class="meta">
          <div class="author">
            <el-avatar :size="40" :src="diary.authorAvatar ? 'http://localhost:9000' + diary.authorAvatar : undefined" icon="UserFilled" />
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
      <div class="content" v-html="diary.content?.replace(/\n/g, '<br>')" />
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
