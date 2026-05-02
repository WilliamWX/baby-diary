<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBaby } from '../api/baby'
import { getDiaryList } from '../api/diary'
import { BASE_URL } from '../utils/config'

const route = useRoute()
const router = useRouter()
const baby = ref(null)
const diaries = ref([])
const loading = ref(true)
const baseUrl = BASE_URL

const genderLabel = computed(() => {
  if (!baby.value) return ''
  return baby.value.gender === 1 ? '男孩' : '女孩'
})

const ageText = computed(() => {
  if (!baby.value?.birthday) return ''
  const birth = new Date(baby.value.birthday)
  const now = new Date()
  const diffMonths = (now.getFullYear() - birth.getFullYear()) * 12 + now.getMonth() - birth.getMonth()
  if (diffMonths < 0) return '即将出生'
  if (diffMonths < 1) {
    const days = Math.floor((now - birth) / (1000 * 60 * 60 * 24))
    return `${days} 天`
  }
  if (diffMonths < 24) return `${diffMonths} 个月`
  const years = Math.floor(diffMonths / 12)
  const months = diffMonths % 12
  return months > 0 ? `${years} 岁 ${months} 个月` : `${years} 岁`
})

onMounted(async () => {
  try {
    const [babyRes, diaryRes] = await Promise.all([
      getBaby(route.params.id),
      getDiaryList(1, 6, route.params.id)
    ])
    baby.value = babyRes.data
    diaries.value = diaryRes.data.records || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="baby-home" v-loading="loading">
    <template v-if="baby">
      <!-- Baby Profile Card -->
      <div class="baby-card">
        <div class="baby-avatar">
          <el-avatar :size="80" :src="baby.avatar ? baseUrl + baby.avatar : undefined" icon="UserFilled" />
        </div>
        <div class="baby-info">
          <h2>{{ baby.name }}</h2>
          <div class="baby-meta">
            <el-tag :type="baby.gender === 1 ? 'primary' : 'danger'" size="small">{{ genderLabel }}</el-tag>
            <span class="baby-birthday">🎂 {{ baby.birthday }}</span>
            <span class="baby-age" v-if="ageText">🎈 {{ ageText }}</span>
          </div>
        </div>
        <div class="baby-actions">
          <el-button type="primary" size="small" @click="router.push(`/growth/${baby.id}`)">成长记录</el-button>
          <el-button size="small" @click="router.push(`/diary/create`)">写日记</el-button>
        </div>
      </div>

      <!-- Diary List -->
      <div class="baby-diaries">
        <div class="section-header">
          <h3>{{ baby.name }}的日记</h3>
          <el-button text type="primary" size="small" @click="router.push('/')">查看全部</el-button>
        </div>
        <div v-if="diaries.length === 0" class="empty-diaries">
          <el-empty description="还没有日记" :image-size="80" />
        </div>
        <div v-else class="diary-grid">
          <el-card
            v-for="d in diaries"
            :key="d.id"
            class="diary-mini"
            shadow="hover"
            @click="router.push(`/diary/${d.id}`)">
            <div v-if="d.images?.length" class="diary-cover">
              <el-image :src="baseUrl + d.images[0]" fit="cover" />
            </div>
            <p class="diary-excerpt">{{ d.content?.slice(0, 80) }}</p>
            <div class="diary-date">{{ d.recordDate || d.createdAt?.slice(0, 10) }}</div>
          </el-card>
        </div>
      </div>
    </template>

    <div v-else-if="!loading" class="empty">
      <el-empty description="宝宝信息加载失败">
        <el-button type="primary" @click="router.push('/profile')">去添加宝宝</el-button>
      </el-empty>
    </div>
  </div>
</template>

<style scoped>
.baby-home {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;
}

.baby-card {
  background: linear-gradient(135deg, #fff5f5 0%, #fff0f3 50%, #fce4ec 100%);
  border-radius: 16px;
  padding: 28px;
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 28px;
}
.baby-avatar :deep(.el-avatar) {
  border: 3px solid #ff6b81;
}
.baby-info h2 {
  font-size: 24px;
  margin: 0 0 8px;
  color: #333;
}
.baby-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 4px;
}
.baby-birthday, .baby-age {
  font-size: 14px;
  color: #888;
}
.baby-age {
  color: #ff6b81;
  font-weight: 500;
}
.baby-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.section-header h3 {
  font-size: 18px;
  color: #333;
  margin: 0;
}

.diary-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.diary-mini {
  cursor: pointer;
}
.diary-cover {
  height: 140px;
  overflow: hidden;
  border-radius: 6px;
  margin-bottom: 8px;
}
.diary-cover :deep(.el-image) {
  width: 100%;
  height: 100%;
}
.diary-cover :deep(img) {
  object-fit: cover;
  width: 100%;
  height: 100%;
}
.diary-excerpt {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.diary-date {
  font-size: 12px;
  color: #bbb;
}

.empty {
  margin-top: 80px;
}
.empty-diaries {
  padding: 30px 0;
}
</style>
