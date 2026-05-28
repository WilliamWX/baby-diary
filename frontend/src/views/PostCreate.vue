<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createPost } from '../api/post'
import FriendPicker from '../components/FriendPicker.vue'

const router = useRouter()
const form = ref({ title: '', content: '', category: '其他', visibility: 1, visibleTo: '' })
const submitting = ref(false)
const categories = ['喂养', '睡眠', '早教', '健康', '其他']
const friendPickerVisible = ref(false)
const selectedFriendIds = ref([])

function onVisibilityChange(val) {
  if (val === 3) {
    friendPickerVisible.value = true
  } else {
    selectedFriendIds.value = []
    form.value.visibleTo = ''
  }
}

function onFriendConfirm(ids) {
  selectedFriendIds.value = ids
  form.value.visibleTo = ids.join(',')
}

async function submit() {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  submitting.value = true
  try {
    await createPost(form.value)
    ElMessage.success('发布成功')
    router.push('/posts')
  } catch (e) { /* ignored */ }
  finally { submitting.value = false }
}
</script>

<template>
  <div class="post-create">
    <el-card>
      <h2>发布经验</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="form.category">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="标题" maxlength="200" />
        </el-form-item>
        <el-form-item label="可见性">
          <el-radio-group v-model="form.visibility" @change="onVisibilityChange">
            <el-radio :value="1">所有人</el-radio>
            <el-radio :value="2">全部好友</el-radio>
            <el-radio :value="3">部分好友</el-radio>
            <el-radio :value="0">个人私密</el-radio>
          </el-radio-group>
          <div v-if="form.visibility === 3" class="friend-tags">
            <el-button size="small" @click="friendPickerVisible = true">
              选择好友 (已选{{ selectedFriendIds.length }}人)
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="分享你的养娃经验..." />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">发布</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <FriendPicker v-model="selectedFriendIds" v-model:visible="friendPickerVisible" @update:model-value="onFriendConfirm" />
  </div>
</template>

<style scoped>
.post-create { max-width: 800px; margin: 0 auto; padding: 20px 0; }
.post-create h2 { color: #ff6b81; margin-bottom: 20px; font-size: 22px; }
.friend-tags { margin-top: 8px; }
</style>
