<script setup>
import { onMounted, ref, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { uploadAvatar, updateProfile } from '../api/user'
import { getDiaryList } from '../api/diary'
import { getMomentList } from '../api/moment'
import { getPostList, getLikedItems, getBookmarkedItems } from '../api/post'
import { getHistory } from '../api/aidoctor'
import { getFriendList, removeFriend } from '../api/friend'
import { BASE_URL } from '../utils/config'

const router = useRouter()
const auth = useAuthStore()
const baseUrl = BASE_URL
const uploading = ref(false)

// Friend management dialog
const friendMgmtVisible = ref(false)
const friendMgmtList = ref([])
const friendMgmtLoading = ref(false)
const friendSelectedIds = ref([])

async function openFriendMgmt() {
  friendMgmtVisible.value = true
  friendMgmtLoading.value = true
  friendSelectedIds.value = []
  try {
    const res = await getFriendList()
    friendMgmtList.value = res.data || []
  } finally {
    friendMgmtLoading.value = false
  }
}

function toggleSelectAll() {
  if (friendSelectedIds.value.length === friendMgmtList.value.length) {
    friendSelectedIds.value = []
  } else {
    friendSelectedIds.value = friendMgmtList.value.map(f => f.id)
  }
}

async function handleBatchRemove() {
  if (friendSelectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的好友')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${friendSelectedIds.value.length} 位好友吗？`,
      '批量删除',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch { return }
  for (const id of friendSelectedIds.value) {
    try { await removeFriend(id) } catch (e) { /* continue */ }
  }
  friendMgmtList.value = friendMgmtList.value.filter(f => !friendSelectedIds.value.includes(f.id))
  friendSelectedIds.value = []
  ElMessage.success('删除完成')
  auth.fetchProfile()
}

async function handleRemoveSingle(friend) {
  try {
    await ElMessageBox.confirm(
      `确定要删除好友「${friend.username}」吗？`,
      '删除好友',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch { return }
  try {
    await removeFriend(friend.id)
    friendMgmtList.value = friendMgmtList.value.filter(f => f.id !== friend.id)
    friendSelectedIds.value = friendSelectedIds.value.filter(id => id !== friend.id)
    ElMessage.success('已删除好友')
    auth.fetchProfile()
  } catch (e) { ElMessage.error('删除失败') }
}
const editingBio = ref(false)
const bioDraft = ref('')
const savingBio = ref(false)
const bioInput = ref(null)

const activeTab = ref('all')
const feedItems = ref([])
const feedLoading = ref(false)

const tabs = [
  { key: 'moment', label: '精彩时刻', countKey: 'momentCount' },
  { key: 'diary', label: '日记', countKey: 'diaryCount' },
  { key: 'post', label: '经验', countKey: 'postCount' },
  { key: 'aichat', label: 'AI医生问答', countKey: 'aiChatCount' },
  { key: 'friend', label: '好友', countKey: 'friendCount' },
  { key: 'like', label: '点赞', countKey: 'likeCount' },
  { key: 'bookmark', label: '收藏', countKey: 'bookmarkCount' },
]

onMounted(async () => {
  await auth.fetchProfile()
  fetchFeed()
})

function switchTab(key) {
  if (key === 'friend') {
    router.push('/friends')
    return
  }
  activeTab.value = key
  fetchFeed()
}

function getTypeLabel(type) {
  const map = { moment: '精彩时刻', diary: '日记', post: '经验', aichat: 'AI医生问答' }
  return map[type] || ''
}

function getTypeColor(type) {
  const map = { moment: 'danger', diary: 'warning', post: 'primary', aichat: 'success' }
  return map[type] || 'info'
}

function getItemPath(item) {
  if (item.type === 'moment') return `/moment/${item.id}`
  if (item.type === 'diary') return `/diary/${item.id}`
  if (item.type === 'post') return `/post/${item.id}`
  if (item.type === 'aichat') return '/ai-doctor'
  return ''
}

function getItemPreview(item) {
  if (item.type === 'moment') return item.description?.slice(0, 100) || item.content?.slice(0, 100) || '无描述'
  if (item.type === 'diary') return item.content?.slice(0, 100) || ''
  if (item.type === 'post') return item.content?.slice(0, 100) || ''
  if (item.type === 'aichat') return `Q: ${item.question?.slice(0, 80)}`
  return ''
}

function getItemCover(item) {
  if (item.type === 'moment') return item.coverUrl
  if (item.type === 'diary') return item.images?.[0] || item.coverUrl
  return item.coverUrl || null
}

async function fetchFeed() {
  feedLoading.value = true
  try {
    const uid = auth.user.id
    let items = []

    if (activeTab.value === 'like') {
      const res = await getLikedItems()
      items = (res.data || []).map(r => ({ ...r, type: r.type }))
    } else if (activeTab.value === 'bookmark') {
      const res = await getBookmarkedItems()
      items = (res.data || []).map(r => ({ ...r, type: r.type }))
    } else if (activeTab.value === 'moment') {
      const res = await getMomentList(1, 20, null, uid)
      items = (res.data.records || []).map(r => ({ ...r, type: 'moment' }))
    } else if (activeTab.value === 'diary') {
      const res = await getDiaryList(1, 20, null, uid)
      items = (res.data.records || []).map(r => ({ ...r, type: 'diary' }))
    } else if (activeTab.value === 'post') {
      const res = await getPostList(1, 20, '', '', uid)
      items = (res.data.records || []).map(r => ({ ...r, type: 'post' }))
    } else if (activeTab.value === 'aichat') {
      const res = await getHistory(1, 20)
      items = (res.data.records || []).map(r => ({ ...r, type: 'aichat' }))
    } else {
      // 'all'
      const [dr, mr, pr, ar] = await Promise.all([
        getDiaryList(1, 10, null, uid),
        getMomentList(1, 10, null, uid),
        getPostList(1, 10, '', '', uid),
        getHistory(1, 10)
      ])
      items = [
        ...(dr.data.records || []).map(r => ({ ...r, type: 'diary' })),
        ...(mr.data.records || []).map(r => ({ ...r, type: 'moment' })),
        ...(pr.data.records || []).map(r => ({ ...r, type: 'post' })),
        ...(ar.data.records || []).map(r => ({ ...r, type: 'aichat' }))
      ]
    }

    items.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    feedItems.value = items.slice(0, 20)
  } catch (e) { /* ignored */ }
  finally { feedLoading.value = false }
}

function startEditBio() {
  bioDraft.value = auth.user?.bio || ''
  editingBio.value = true
  nextTick(() => bioInput.value?.focus())
}

async function saveBio() {
  savingBio.value = true
  try {
    await updateProfile({ bio: bioDraft.value })
    auth.user.bio = bioDraft.value
    editingBio.value = false
    ElMessage.success('签名已更新')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    savingBio.value = false
  }
}

function triggerUpload() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    uploading.value = true
    try {
      const res = await uploadAvatar(file)
      auth.user.avatar = res.data.url
      ElMessage.success('头像更新成功')
    } catch (e) {
      ElMessage.error('头像上传失败')
    } finally {
      uploading.value = false
    }
  }
  input.click()
}
</script>

<template>
  <div class="page">
    <!-- Profile Card -->
    <el-card>
      <div class="profile-header">
        <div class="avatar-wrap" @click="triggerUpload" title="点击更换头像">
          <el-avatar v-if="auth.user?.avatar" :size="80" :src="baseUrl + auth.user.avatar" />
          <el-avatar v-else :size="80" icon="UserFilled" />
          <div class="avatar-overlay" :class="{ uploading }">
            <span v-if="uploading">上传中...</span>
            <span v-else>换头像</span>
          </div>
        </div>
        <div class="profile-info">
          <h2>{{ auth.user?.username }}</h2>
          <div class="bio-wrap" @click="!editingBio && startEditBio()">
            <el-input
              v-if="editingBio"
              ref="bioInput"
              v-model="bioDraft"
              placeholder="写一句个性签名..."
              maxlength="60"
              show-word-limit
              @blur="saveBio"
              @keyup.enter="saveBio"
              :disabled="savingBio" />
            <p v-else class="bio" :class="{ placeholder: !auth.user?.bio }">
              {{ auth.user?.bio || '点击添加个性签名...' }}
            </p>
          </div>
          <p class="email" v-if="auth.user?.email">{{ auth.user?.email }}</p>
          <p class="join-date">加入于 {{ auth.user?.createdAt?.slice(0, 10) }}</p>
        </div>
      </div>
      <div class="stats">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          class="stat-item"
          :class="{ active: activeTab === tab.key }"
          @click="switchTab(tab.key)"
        >
          <span class="num">{{ auth.user?.[tab.countKey] || 0 }}</span>
          <span class="label">{{ tab.label }}</span>
        </div>
      </div>
      <div style="text-align:right;padding-top:12px;">
        <el-button type="primary" @click="openFriendMgmt">好友管理</el-button>
      </div>
    </el-card>

    <!-- Friend Management Dialog -->
    <el-dialog v-model="friendMgmtVisible" title="好友管理" width="500px" :close-on-click-modal="false">
      <div v-loading="friendMgmtLoading">
        <div v-if="friendMgmtList.length > 0">
          <div class="mgmt-toolbar">
            <el-checkbox
              :model-value="friendSelectedIds.length === friendMgmtList.length"
              :indeterminate="friendSelectedIds.length > 0 && friendSelectedIds.length < friendMgmtList.length"
              @change="toggleSelectAll"
            >全选</el-checkbox>
            <el-button
              type="danger"
              size="small"
              :disabled="friendSelectedIds.length === 0"
              @click="handleBatchRemove"
            >批量删除 ({{ friendSelectedIds.length }})</el-button>
          </div>
          <div class="mgmt-list">
            <el-card v-for="friend in friendMgmtList" :key="friend.id" class="mgmt-card" shadow="hover">
              <div class="mgmt-item">
                <el-checkbox v-model="friendSelectedIds" :value="friend.id" />
                <el-avatar
                  v-if="friend.avatar"
                  :size="40"
                  :src="baseUrl + friend.avatar"
                />
                <el-avatar v-else :size="40" icon="UserFilled" />
                <span class="mgmt-name">{{ friend.username }}</span>
              </div>
              <el-button type="danger" text size="small" @click="handleRemoveSingle(friend)">删除</el-button>
            </el-card>
          </div>
        </div>
        <el-empty v-else-if="!friendMgmtLoading" description="还没有好友" :image-size="60" />
      </div>
    </el-dialog>

    <!-- Content Feed -->
    <div class="feed" v-if="feedItems.length > 0">
      <div class="section-header">
        <span class="section-title" v-if="activeTab === 'all'">最近更新</span>
        <span class="section-title" v-else>{{ tabs.find(t => t.key === activeTab)?.label }}</span>
        <el-button v-if="activeTab !== 'all'" size="small" text @click="switchTab('all')">查看全部</el-button>
      </div>
      <div
        v-for="item in feedItems"
        :key="item.type + '-' + item.id"
        class="feed-item"
        @click="router.push(getItemPath(item))"
      >
        <el-image
          v-if="getItemCover(item)"
          :src="baseUrl + getItemCover(item)"
          fit="cover"
          class="feed-cover"
        />
        <div class="feed-body">
          <div class="feed-top">
            <el-tag :type="getTypeColor(item.type)" size="small">{{ getTypeLabel(item.type) }}</el-tag>
            <span class="feed-time">{{ item.createdAt?.slice(0, 16).replace('T', ' ') }}</span>
          </div>
          <p class="feed-preview">{{ getItemPreview(item) }}</p>
        </div>
      </div>
    </div>
    <div v-else-if="!feedLoading" class="feed-empty">
      <el-empty description="还没有内容" :image-size="60" />
    </div>
  </div>
</template>

<style scoped>
.page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;
}
.profile-header {
  display: flex;
  gap: 24px;
  align-items: center;
  margin-bottom: 24px;
}
.avatar-wrap {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  flex-shrink: 0;
}
.avatar-wrap:hover .avatar-overlay {
  opacity: 1;
}
.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s;
}
.avatar-overlay.uploading { opacity: 1; }
.profile-info h2 {
  font-size: 22px;
  margin-bottom: 8px;
}
.bio-wrap {
  cursor: pointer;
  min-width: 200px;
}
.bio-wrap:hover .bio { color: #e91e63; }
.bio {
  color: #666;
  margin-bottom: 4px;
  min-height: 22px;
}
.bio.placeholder {
  color: #ccc;
  font-style: italic;
}
.email, .join-date {
  font-size: 13px;
  color: #999;
}
.stats {
  display: flex;
  gap: 28px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  flex-wrap: wrap;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}
.stat-item:hover { background: #fff0f3; }
.stat-item.active { background: #fff0f3; }
.stat-item.active .label { color: #ff6b81; font-weight: 600; }
.stat-item .num {
  font-size: 20px;
  font-weight: bold;
  color: #ff6b81;
}
.stat-item .label {
  font-size: 13px;
  color: #999;
}

.feed { margin-top: 20px; }
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-left: 4px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
.feed-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 10px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: background 0.2s;
}
.feed-item:hover { background: #fafafa; }
.feed-cover {
  width: 80px;
  height: 56px;
  border-radius: 6px;
  flex-shrink: 0;
  background: #f0f0f0;
}
.feed-body { flex: 1; min-width: 0; }
.feed-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.feed-time { font-size: 12px; color: #bbb; }
.feed-preview {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.feed-empty {
  margin-top: 20px;
  text-align: center;
}

.mgmt-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}
.mgmt-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 400px;
  overflow-y: auto;
}
.mgmt-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
}
.mgmt-item {
  display: flex;
  align-items: center;
  gap: 10px;
}
.mgmt-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

@media (max-width: 640px) {
  .stats { gap: 16px; }
  .stat-item .num { font-size: 16px; }
}
</style>
