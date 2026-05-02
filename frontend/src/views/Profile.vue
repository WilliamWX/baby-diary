<script setup>
import { onMounted, ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { uploadAvatar, updateProfile } from '../api/user'
import { BASE_URL } from '../utils/config'

const auth = useAuthStore()
const baseUrl = BASE_URL
const uploading = ref(false)
const editingBio = ref(false)
const bioDraft = ref('')
const savingBio = ref(false)
const bioInput = ref(null)

onMounted(async () => {
  await auth.fetchProfile()
})

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
        <div class="stat-item">
          <span class="num">{{ auth.user?.diaryCount || 0 }}</span>
          <span class="label">日记</span>
        </div>
        <div class="stat-item">
          <span class="num">{{ auth.user?.followerCount || 0 }}</span>
          <span class="label">粉丝</span>
        </div>
        <div class="stat-item">
          <span class="num">{{ auth.user?.followingCount || 0 }}</span>
          <span class="label">关注</span>
        </div>
      </div>
    </el-card>
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
.avatar-overlay.uploading {
  opacity: 1;
}
.profile-info h2 {
  font-size: 22px;
  margin-bottom: 8px;
}
.bio-wrap {
  cursor: pointer;
  min-width: 200px;
}
.bio-wrap:hover .bio {
  color: #e91e63;
}
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
  gap: 40px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.stat-item .num {
  font-size: 20px;
  font-weight: bold;
  color: #ff6b81;
}
.stat-item .label {
  font-size: 13px;
  color: #999;
}
</style>
