<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDiaryList } from '../api/diary'
import { getMomentList } from '../api/moment'
import { getPostList } from '../api/post'
import { getTopChat } from '../api/aidoctor'
import { useAuthStore } from '../stores/auth'
import Logo from '../components/Logo.vue'
import { BASE_URL } from '../utils/config'

const router = useRouter()
const auth = useAuthStore()

const featuredDiary = ref(null)
const featuredPost = ref(null)
const featuredMoment = ref(null)
const featuredAi = ref(null)

const stats = ref({ diaryCount: 0, followerCount: 0, followingCount: 0 })
const baseUrl = BASE_URL

async function fetchStats() {
  try {
    await auth.fetchProfile()
    if (auth.user) {
      stats.value = {
        diaryCount: auth.user.diaryCount || 0,
        followerCount: auth.user.followerCount || 0,
        followingCount: auth.user.followingCount || 0
      }
    }
  } catch (e) { /* ignored */ }
}

async function fetchFeatured() {
  try {
    const [dr, pr, mr, ar] = await Promise.all([
      getDiaryList(1, 1, null, 'popular'),
      getPostList(1, 1, '', 'popular'),
      getMomentList(1, 1, null, 'popular'),
      getTopChat()
    ])
    featuredDiary.value = dr.data.records?.[0] || null
    featuredPost.value = pr.data.records?.[0] || null
    featuredMoment.value = mr.data.records?.[0] || null
    featuredAi.value = ar.data || null
  } catch (e) { /* ignored */ }
}

onMounted(() => {
  fetchStats()
  fetchFeatured()
})
</script>

<template>
  <div class="home">
    <!-- Stats Banner -->
    <div class="welcome-banner">
      <div class="banner-left">
        <Logo :size="48" />
        <div class="banner-text">
          <h1>养娃宝</h1>
          <p>记录宝宝每一个成长瞬间</p>
        </div>
      </div>
      <div class="stats-row">
        <div class="stat-item" @click="router.push('/profile')">
          <span class="stat-num">{{ stats.diaryCount }}</span>
          <span class="stat-label">日记</span>
        </div>
        <div class="stat-item" @click="router.push('/profile')">
          <span class="stat-num">{{ stats.followerCount }}</span>
          <span class="stat-label">粉丝</span>
        </div>
        <div class="stat-item" @click="router.push('/profile')">
          <span class="stat-num">{{ stats.followingCount }}</span>
          <span class="stat-label">关注</span>
        </div>
      </div>
    </div>

    <!-- Featured: top liked items -->
    <div class="featured" v-if="featuredDiary || featuredPost || featuredMoment || featuredAi">
      <div class="section-title">热门推荐</div>
      <div class="featured-grid">
        <!-- Featured Moment -->
        <el-card v-if="featuredMoment" class="featured-card" shadow="hover" @click="router.push(`/moment/${featuredMoment.id}`)">
          <div class="featured-cover" v-if="featuredMoment.coverUrl">
            <el-image :src="baseUrl + featuredMoment.coverUrl" fit="cover" class="featured-img" />
            <div class="featured-play"><el-icon :size="22"><VideoPlay /></el-icon></div>
          </div>
          <div class="featured-tag"><el-tag size="small" type="danger">精彩时刻</el-tag></div>
          <p class="featured-text">{{ featuredMoment.description?.slice(0, 80) || '无描述' }}</p>
          <div class="featured-meta">
            <span><svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg> {{ featuredMoment.likeCount || 0 }}</span>
            <span><svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg> {{ featuredMoment.commentCount || 0 }}</span>
            <span>{{ featuredMoment.authorName }}</span>
          </div>
        </el-card>

        <!-- Featured Diary -->
        <el-card v-if="featuredDiary" class="featured-card" shadow="hover" @click="router.push(`/diary/${featuredDiary.id}`)">
          <div class="featured-cover" v-if="featuredDiary.images?.length">
            <el-image :src="baseUrl + featuredDiary.images[0]" fit="cover" class="featured-img" />
          </div>
          <div class="featured-tag"><el-tag size="small" type="warning">日记</el-tag></div>
          <p class="featured-text">{{ featuredDiary.content?.slice(0, 80) }}</p>
          <div class="featured-meta">
            <span><svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg> {{ featuredDiary.likeCount || 0 }}</span>
            <span><svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg> {{ featuredDiary.commentCount || 0 }}</span>
            <span>{{ featuredDiary.authorName }}</span>
          </div>
        </el-card>

        <!-- Featured Post -->
        <el-card v-if="featuredPost" class="featured-card" shadow="hover" @click="router.push(`/post/${featuredPost.id}`)">
          <div class="featured-tag"><el-tag size="small" type="primary">经验</el-tag></div>
          <p class="featured-title">{{ featuredPost.title }}</p>
          <p class="featured-text">{{ featuredPost.content?.slice(0, 80) }}</p>
          <div class="featured-meta">
            <span><svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg> {{ featuredPost.likeCount || 0 }}</span>
            <span><svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg> {{ featuredPost.commentCount || 0 }}</span>
            <span>{{ featuredPost.authorName }}</span>
          </div>
        </el-card>

        <!-- Featured AI -->
        <el-card v-if="featuredAi" class="featured-card" shadow="hover" @click="router.push('/ai-doctor')">
          <div class="featured-tag"><el-tag size="small" type="success">AI 医生</el-tag></div>
          <p class="featured-text">Q: {{ featuredAi.question?.slice(0, 60) }}</p>
          <p class="featured-answer">{{ featuredAi.answer?.slice(0, 60) }}</p>
          <div class="featured-meta">
            <span><svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg> {{ featuredAi.likeCount || 0 }}</span>
            <span>{{ featuredAi.username }}</span>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home {
  max-width: 680px;
  margin: 0 auto;
  padding: 20px 0;
}

.welcome-banner {
  background: linear-gradient(135deg, #ff9a9e 0%, #ff6b81 50%, #fad0c4 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
}
.banner-left { display: flex; align-items: center; gap: 16px; }
.banner-text h1 { font-size: 22px; margin: 0; }
.banner-text p { font-size: 13px; margin: 4px 0 0; opacity: 0.9; }
.stats-row { display: flex; gap: 24px; }
.stat-item { text-align: center; cursor: pointer; }
.stat-num { display: block; font-size: 24px; font-weight: 700; }
.stat-label { font-size: 12px; opacity: 0.85; }

.featured { margin-bottom: 24px; }
.section-title {
  font-size: 16px; font-weight: 600; color: #333;
  margin-bottom: 12px; padding-left: 4px;
}
.featured-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.featured-card { cursor: pointer; }
.featured-card:hover { transform: translateY(-2px); transition: transform 0.2s; }
.featured-cover {
  position: relative; aspect-ratio: 16/9; overflow: hidden;
  border-radius: 6px; margin-bottom: 8px; background: #f0f0f0;
}
.featured-img { width: 100%; height: 100%; }
.featured-play {
  position: absolute; top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  color: #fff; background: rgba(0,0,0,0.4);
  width: 36px; height: 36px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.featured-tag { margin-bottom: 6px; }
.featured-title { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 4px; }
.featured-text {
  font-size: 13px; color: #777; line-height: 1.5;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden; margin-bottom: 4px;
}
.featured-answer {
  font-size: 12px; color: #aaa; line-height: 1.5;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden; margin-bottom: 8px;
}
.featured-meta {
  display: flex; gap: 12px; font-size: 12px; color: #bbb; align-items: center;
}
.featured-meta span { display: flex; align-items: center; gap: 2px; }

@media (max-width: 640px) {
  .featured-grid { grid-template-columns: 1fr; }
}
</style>
