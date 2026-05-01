<script setup>
defineProps({
  showText: { type: Boolean, default: true },
  size: { type: Number, default: 48 }
})
</script>

<template>
  <div class="logo-wrap">
    <svg class="logo-icon" viewBox="0 0 48 48" :width="size" :height="size">
      <defs>
        <linearGradient id="bgGrad" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="#ff9a9e"/>
          <stop offset="100%" stop-color="#ff6b81"/>
        </linearGradient>
      </defs>
      <circle cx="24" cy="24" r="24" fill="url(#bgGrad)"/>
      <rect x="14" y="18" width="20" height="22" rx="6" fill="white" opacity="0.95"/>
      <rect x="16" y="26" width="16" height="12" rx="3" fill="#ffe0e5"/>
      <rect x="19" y="12" width="10" height="8" rx="3" fill="white" opacity="0.95"/>
      <ellipse cx="24" cy="10" rx="6" ry="4" fill="white" opacity="0.95"/>
      <path d="M24 30 C24 28 21 27 20 29 C19 31 24 34 24 34 C24 34 29 31 28 29 C27 27 24 28 24 30Z" fill="#ff6b81"/>
    </svg>
    <span v-if="showText" class="logo-text" :style="{ fontSize: size * 0.42 + 'px' }">养娃宝</span>
  </div>
</template>

<style scoped>
.logo-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}
.logo-text {
  font-weight: bold;
  color: #ff6b81;
  white-space: nowrap;
}
</style>
