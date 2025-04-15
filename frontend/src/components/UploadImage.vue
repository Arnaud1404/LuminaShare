<script setup lang="ts">
import { ref, watchEffect } from 'vue';
import { uploadImage } from './http-api';
import Notification, { type NotificationType } from './Notification.vue';

const emit = defineEmits(['uploaded']);

const notification = ref<{
  showNotification: (msg: string, type: NotificationType) => void;
} | null>(null);
const file = ref<File | null>(null);
const isUploading = ref(false);
const isFileValid = ref(false);
const allowedFileTypes = ['image/jpeg', 'image/png'];

watchEffect(() => {
  if (file.value) {
    isFileValid.value = allowedFileTypes.includes(file.value.type);
    if (!isFileValid.value) {
      notification.value?.showNotification(
        'Type de fichier non valide. Seuls JPEG et PNG sont acceptés.',
        'error'
      );
    }
  } else {
    isFileValid.value = false;
  }
});

const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files) {
    file.value = target.files[0];
  }
};

const submitFile = async () => {
  if (!file.value || isUploading.value || !isFileValid.value) return;
  isUploading.value = true;
  try {
    await uploadImage(file.value);
    notification.value?.showNotification('Image téléversée avec succès', 'success');
    emit('uploaded');
  } catch (error: any) {
    notification.value?.showNotification('Échec du téléversement', 'error');
  } finally {
    file.value = null;
    isFileValid.value = false;
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fileInput) fileInput.value = '';
    isUploading.value = false;
  }
};
</script>

<template>
  <Notification ref="notification" />
  <div class="upload-area">
    <input type="file" @change="handleFileUpload" />
    <button @click="submitFile" :disabled="isUploading || !isFileValid">
      {{ isUploading ? 'Téléversement...' : 'Téléverser' }}
    </button>
  </div>
</template>

<style scoped>
.upload-area {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
