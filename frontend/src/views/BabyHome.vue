<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const baby = ref(null)
const loading = ref(true)

onMounted(async () => {
  // Baby data will be loaded from a combined API in Phase 3
  loading.value = false
})
</script>

<template>
  <div class="baby-home">
    <el-card v-if="baby">
      <div class="baby-header">
        <el-avatar :size="80" icon="UserFilled" />
        <div class="baby-info">
          <h2>{{ baby.name }}</h2>
          <p>生日：{{ baby.birthday }}</p>
          <p v-if="baby.age">现在 {{ baby.age }} 大了</p>
        </div>
      </div>
    </el-card>
    <el-card v-else class="empty-card">
      <el-empty description="前往添加你的宝宝">
        <el-button type="primary" @click="$router.push('/profile')">添加宝宝</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<style scoped>
.baby-home {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;
}
.baby-header {
  display: flex;
  gap: 24px;
  align-items: center;
}
.baby-info h2 {
  font-size: 24px;
  margin-bottom: 8px;
}
.baby-info p {
  color: #666;
  line-height: 1.6;
}
</style>
