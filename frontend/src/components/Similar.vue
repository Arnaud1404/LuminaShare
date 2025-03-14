<script setup lang="ts">
import { type ImageGallery } from './images';

defineProps<{ images: ImageGallery[] }>();
const emit = defineEmits(['select']);

function selectImage(image: ImageGallery) {
    emit('select', image);
}

function formatSimilarity(value?: number): string {
  if (value === undefined) return "";
  return `${(value * 100).toFixed(2)}`;
}
</script>

<template>
  <div class="image-grid">
    <div
      v-for="image in images"
      :key="image.id"
      class="image-container"
      @click="selectImage(image)"
    >
      <img v-if="image.dataUrl" :src="image.dataUrl" :alt="image.name" />
       <p>{{ formatSimilarity(image.similarity) }}</p>
    </div>
  </div>
</template>

<style scoped>
img:hover {
  transform: scale(1.02);
  cursor: pointer;
}
.image-grid {
  display: flex;
  overflow-x: auto;
  flex-wrap: nowrap;
  gap: 1rem;
  padding: 1rem;
}

.image-container {
  text-align: center;
}

.image-container img {
  max-width: 160px;
  max-height: 120px;
  object-fit: cover;
  border-radius: 4px;
}

.image-container p {
  margin-top: 0.2rem;
}
</style>
