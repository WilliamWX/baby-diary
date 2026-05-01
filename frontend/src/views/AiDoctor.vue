<script setup>
import { ref, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { askDoctor } from '../api/aidoctor'

const question = ref('')
const loading = ref(false)
const chatList = ref([])
const chatEnd = ref(null)

function scrollBottom() {
  nextTick(() => chatEnd.value?.scrollIntoView({ behavior: 'smooth' }))
}

async function handleAsk(anonymous) {
  const q = question.value.trim()
  if (!q) {
    ElMessage.warning('请输入问题')
    return
  }
  loading.value = true
  const displayQuestion = q
  question.value = ''
  try {
    const res = await askDoctor({ question: displayQuestion, anonymous })
    chatList.value.push(res.data)
    scrollBottom()
  } catch (e) {
    ElMessage.error('请求失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="ai-doctor">
    <div class="chat-header">
      <h2>AI 医生</h2>
      <p class="desc">儿科育儿问题随时问</p>
    </div>

    <div class="chat-body" v-if="chatList.length">
      <div v-for="(item, idx) in chatList" :key="idx" class="qa-item">
        <div class="question-bubble">
          <div class="q-label">
            {{ item.anonymous ? '匿名用户' : '我' }}
          </div>
          <div class="q-text">{{ item.question }}</div>
        </div>
        <div class="answer-bubble">
          <div class="a-label">AI 医生</div>
          <div class="a-text">{{ item.answer }}</div>
        </div>
      </div>
      <div ref="chatEnd" />
    </div>

    <div class="chat-input">
      <el-input
        v-model="question"
        type="textarea"
        :rows="3"
        placeholder="输入宝宝的健康、喂养、睡眠等问题..."
        :disabled="loading"
        @keyup.enter.exact.prevent="handleAsk(false)" />
      <div class="btn-row">
        <el-button type="primary" :loading="loading" @click="handleAsk(false)">
          提交
        </el-button>
        <el-button :loading="loading" @click="handleAsk(true)">
          匿名提交
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ai-doctor {
  max-width: 720px;
  margin: 0 auto;
  padding: 20px 0;
}
.chat-header {
  text-align: center;
  margin-bottom: 24px;
}
.chat-header h2 {
  font-size: 24px;
  color: #d81b60;
  margin-bottom: 4px;
}
.chat-header .desc {
  font-size: 14px;
  color: #999;
}
.chat-body {
  margin-bottom: 20px;
}
.qa-item {
  margin-bottom: 20px;
}
.question-bubble {
  text-align: right;
  margin-bottom: 12px;
}
.q-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
.q-text {
  display: inline-block;
  max-width: 80%;
  background: #ff6b81;
  color: #fff;
  padding: 10px 16px;
  border-radius: 16px 4px 16px 16px;
  font-size: 15px;
  line-height: 1.6;
  text-align: left;
}
.answer-bubble {
  text-align: left;
}
.a-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  padding-left: 4px;
}
.a-text {
  display: inline-block;
  max-width: 90%;
  background: #f5f5f5;
  color: #333;
  padding: 12px 18px;
  border-radius: 4px 16px 16px 16px;
  font-size: 15px;
  line-height: 1.8;
  white-space: pre-wrap;
}
.chat-input {
  position: sticky;
  bottom: 0;
  background: #fff;
  padding: 16px 0;
  border-top: 1px solid #eee;
}
.btn-row {
  display: flex;
  gap: 12px;
  margin-top: 10px;
  justify-content: flex-end;
}
</style>
