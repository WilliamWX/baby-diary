<script setup>
import { ref, watch } from 'vue'
import { getFriendList } from '../api/friend'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'update:visible'])

const friends = ref([])
const checkedIds = ref([])
const loading = ref(false)

watch(() => props.visible, async (val) => {
  if (val) {
    checkedIds.value = [...props.modelValue]
    loading.value = true
    try {
      const res = await getFriendList()
      friends.value = res.data || []
    } catch (e) {
      friends.value = []
    } finally {
      loading.value = false
    }
  }
})

function confirm() {
  emit('update:modelValue', checkedIds.value)
  emit('update:visible', false)
}

function cancel() {
  emit('update:visible', false)
}
</script>

<template>
  <el-dialog
    title="选择好友"
    :model-value="visible"
    @update:model-value="emit('update:visible', $event)"
    width="400px"
    :close-on-click-modal="false"
  >
    <div v-loading="loading">
      <el-checkbox-group v-model="checkedIds">
        <div
          v-for="friend in friends"
          :key="friend.id"
          style="padding: 8px 0; border-bottom: 1px solid #f0f0f0;"
        >
          <el-checkbox :label="friend.id" :value="friend.id">
            {{ friend.username }}
          </el-checkbox>
        </div>
      </el-checkbox-group>
      <el-empty v-if="!loading && friends.length === 0" description="暂无好友" :image-size="60" />
    </div>

    <template #footer>
      <el-button @click="cancel">取消</el-button>
      <el-button type="primary" @click="confirm">确定</el-button>
    </template>
  </el-dialog>
</template>
