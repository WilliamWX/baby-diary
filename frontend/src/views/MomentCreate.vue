<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createMoment, uploadVideo, uploadCover } from '../api/moment'
import { getBabyList } from '../api/baby'
import { BASE_URL } from '../utils/config'

const router = useRouter()
const baseUrl = BASE_URL

const form = ref({
  description: '',
  babyId: null,
  videoUrl: '',
  coverUrl: ''
})

const videoFile = ref(null)
const videoPreviewUrl = ref('')
const uploadingVideo = ref(false)
const uploadingCover = ref(false)
const submitting = ref(false)
const babies = ref([])

const MAX_VIDEO_SIZE = 100 * 1024 * 1024

async function loadBabies() {
  try {
    const res = await getBabyList()
    babies.value = res.data || []
  } catch (e) { /* ignored */ }
}

function beforeVideoUpload(file) {
  const isVideo = file.type.startsWith('video/')
  if (!isVideo) {
    ElMessage.error('请选择视频文件')
    return false
  }
  if (file.size > MAX_VIDEO_SIZE) {
    ElMessage.error('视频大小不能超过100MB')
    return false
  }
  return true
}

async function handleVideoChange(file) {
  if (!beforeVideoUpload(file.raw)) return
  uploadingVideo.value = true
  try {
    videoFile.value = file.raw
    videoPreviewUrl.value = URL.createObjectURL(file.raw)
    const res = await uploadVideo(file.raw)
    form.value.videoUrl = res.data.url
    ElMessage.success('视频上传成功')
  } catch (e) {
    ElMessage.error('视频上传失败')
  } finally {
    uploadingVideo.value = false
  }
}

async function handleCoverChange(file) {
  uploadingCover.value = true
  try {
    const res = await uploadCover(file.raw)
    form.value.coverUrl = res.data.url
    ElMessage.success('封面上传成功')
  } catch (e) {
    ElMessage.error('封面上传失败')
  } finally {
    uploadingCover.value = false
  }
}

async function handleSubmit() {
  if (!form.value.videoUrl) {
    ElMessage.warning('请先上传视频')
    return
  }
  submitting.value = true
  try {
    await createMoment({ ...form.value })
    ElMessage.success('发布成功')
    router.push('/moments')
  } catch (e) {
    ElMessage.error('发布失败')
  } finally {
    submitting.value = false
  }
}

loadBabies()
</script>

<template>
  <div class="moment-create">
    <el-card>
      <h2 class="page-title">发布精彩时刻</h2>
      <el-form :model="form" label-width="80px">

        <!-- Video Upload -->
        <el-form-item label="视频">
          <div class="video-upload-area">
            <div v-if="videoPreviewUrl" class="video-preview">
              <video :src="videoPreviewUrl" controls class="preview-video" />
              <el-button type="danger" size="small" class="change-video-btn" @click="videoPreviewUrl='';form.videoUrl='';videoFile=null">
                更换视频
              </el-button>
            </div>
            <el-upload
              v-else
              :auto-upload="false"
              :show-file-list="false"
              accept="video/mp4,video/mov,video/webm"
              @change="handleVideoChange"
              drag>
              <div class="upload-placeholder">
                <el-icon :size="48" v-if="!uploadingVideo"><VideoCamera /></el-icon>
                <p v-if="!uploadingVideo">点击或拖拽上传视频</p>
                <p v-else>上传中...</p>
                <span class="upload-hint">支持 mp4、mov、webm，最大 100MB</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>

        <!-- Cover Upload -->
        <el-form-item label="封面图">
          <div class="cover-upload">
            <div v-if="form.coverUrl" class="cover-preview">
              <el-image :src="baseUrl + form.coverUrl" fit="cover" class="cover-img" />
              <el-button type="danger" circle size="small" class="remove-cover" @click="form.coverUrl = ''">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-upload
              v-else
              :auto-upload="false"
              :show-file-list="false"
              accept="image/*"
              @change="handleCoverChange">
              <div class="cover-btn">
                <el-icon :size="24"><Plus /></el-icon>
              </div>
            </el-upload>
          </div>
        </el-form-item>

        <!-- Description -->
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="记录这精彩的一刻..."
            maxlength="500"
            show-word-limit />
        </el-form-item>

        <!-- Baby -->
        <el-form-item label="宝宝">
          <el-select v-model="form.babyId" placeholder="选择宝宝（可选）" clearable style="width:100%">
            <el-option v-for="b in babies" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">发布</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.moment-create { max-width: 720px; margin: 0 auto; padding: 20px 0; }
.page-title { color: #ff6b81; margin-bottom: 24px; font-size: 22px; }

.video-upload-area { width: 100%; }
.video-preview { position: relative; }
.preview-video { width: 100%; max-height: 360px; border-radius: 8px; background: #000; }
.change-video-btn { position: absolute; top: 8px; right: 8px; }

.upload-placeholder {
  padding: 40px 20px;
  text-align: center;
  color: #bbb;
}
.upload-placeholder p { margin: 8px 0 4px; font-size: 14px; color: #999; }
.upload-hint { font-size: 12px; color: #ccc; }

.cover-upload { display: flex; align-items: center; }
.cover-preview { position: relative; width: 120px; height: 90px; }
.cover-img { width: 120px; height: 90px; border-radius: 6px; }
.remove-cover { position: absolute; top: -8px; right: -8px; }
.cover-btn {
  width: 120px;
  height: 90px;
  border: 2px dashed #ddd;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #ccc;
  transition: border-color 0.2s;
}
.cover-btn:hover { border-color: #ff6b81; color: #ff6b81; }
</style>
