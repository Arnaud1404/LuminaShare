<script setup lang="ts">
import { ref } from 'vue';

export type NotificationType = 'info' | 'error' | 'success';

const show = ref(false);
const message = ref('');
const type = ref<NotificationType>('info');

function showNotification(text: string, notificationType: NotificationType = 'info') {
  message.value = text;
  type.value = notificationType;
  show.value = true;

  setTimeout(() => {
    show.value = false;
  }, 5000);
}

function hideNotification() {
  show.value = false;
}

defineExpose({
  showNotification,
});
</script>

<template>
  <transition name="fade">
    <div v-if="show" :class="['notification', type]">
      <div>{{ message }}</div>
      <button @click="hideNotification">×</button>
    </div>
  </transition>
</template>

<style scoped>
.notification {
  position: fixed;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  padding: 10px;
  border-radius: 4px;
  color: white;
  display: flex;
  justify-content: space-between;
  min-width: 300px;
  z-index: 1000;
}

.info {
  background-color: #2196f3;
}

.success {
  background-color: #4caf50;
}

.error {
  background-color: #f44336;
}

button {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 20px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
