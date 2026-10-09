<template>
  <span :class="['iot-badge', badgeClass]">
    <span class="dot" :style="{ backgroundColor: dotColor }"></span>
    <span class="badge-text">{{ text }}</span>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: Number, default: 0 },
  text: { type: String, default: '未知' }
})

const badgeClass = computed(() => {
  if (props.status === 1) return 'iot-badge-success'
  if (props.status === 2 || props.status === 0) return 'iot-badge-error'
  return 'iot-badge-warn'
})

const dotColor = computed(() => {
  if (props.status === 1) return '#10b981'
  if (props.status === 2 || props.status === 0) return '#ef4444'
  return '#f59e0b'
})
</script>

<style scoped>
.iot-badge {
  display: inline-flex;
  align-items: center;
  vertical-align: middle;
  gap: 5px;
  line-height: 1;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
  display: inline-block;
}

.badge-text {
  display: inline-flex;
  align-items: center;
  line-height: 1;
}
</style>
