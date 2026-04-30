<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createDiary, uploadImage } from '../api/diary'

const router = useRouter()

const today = new Date().toISOString().slice(0, 10)

const form = ref({
  content: '',
  visibility: 1,
  recordDate: today
})
const MAX_IMAGES = 9
const images = ref([])
const uploading = ref(false)
const submitting = ref(false)

async function handleUpload(file) {
  if (images.value.length >= MAX_IMAGES) {
    ElMessage.warning('最多上传9张照片')
    return
  }
  uploading.value = true
  try {
    const res = await uploadImage(file.raw)
    images.value.push(res.data.url)
  } catch (e) {
    ElMessage.error('图片上传失败')
  } finally {
    uploading.value = false
  }
}

function removeImage(index) {
  images.value.splice(index, 1)
}

async function handleSubmit() {
  submitting.value = true
  try {
    const data = { ...form.value, images: images.value }
    await createDiary(data)
    ElMessage.success('发布成功')
    router.push('/')
  } catch (e) {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="diary-create">
    <el-card>
      <h2 class="page-title">写日记</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="记录日期">
          <el-date-picker
            v-model="form.recordDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%" />
        </el-form-item>
        <el-form-item label="可见性">
          <el-radio-group v-model="form.visibility">
            <el-radio :value="1">公开</el-radio>
            <el-radio :value="0">私密</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            placeholder="记录宝宝的成长点滴...&#10;支持换行分段，图片会显示在文章末尾"
            class="content-input" />
        </el-form-item>
        <el-form-item label="图片">
          <div class="upload-hint">{{ images.length }}/{{ MAX_IMAGES }}</div>
          <div class="image-section">
            <div v-for="(img, idx) in images" :key="idx" class="image-item">
              <img :src="'http://localhost:9000' + img" class="preview-img" />
              <el-button type="danger" circle size="small" class="remove-btn" @click="removeImage(idx)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-upload
              v-if="images.length < MAX_IMAGES"
              :auto-upload="false"
              :show-file-list="false"
              accept="image/*"
              @change="handleUpload">
              <div class="upload-btn">
                <el-icon :size="28"><Plus /></el-icon>
              </div>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">发布日记</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.diary-create {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;
}
.page-title {
  color: #ff6b81;
  margin-bottom: 24px;
  font-size: 22px;
}
.image-section {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.upload-hint {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}
.image-item {
  position: relative;
  width: 100px;
  height: 100px;
}
.preview-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 6px;
}
.remove-btn {
  position: absolute;
  top: -8px;
  right: -8px;
}
.upload-btn {
  width: 100px;
  height: 100px;
  border: 2px dashed #ddd;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #ccc;
}
.content-input {
  max-width: 480px;
}
.upload-btn:hover {
  border-color: #ff6b81;
  color: #ff6b81;
}
</style>
