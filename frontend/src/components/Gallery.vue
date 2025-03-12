<script setup lang="ts">
import { images, type ImageGallery } from "./images";

defineProps<{ images: ImageGallery[] }>();
const emit = defineEmits(["select"]);

function selectImage(image: ImageGallery) {
  emit("select", image);
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
      <p>{{ image.name }}</p>
    </div>
  </div>
</template>

<style scoped>
img:hover {
  transform: scale(1.02);
  cursor: pointer;
}
.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
  padding: 1rem;
}

.image-container {
  text-align: center;
}

.image-container img {
  max-width: 200px;
  max-height: 200px;
  object-fit: cover;
  border-radius: 4px;
}

.image-container p {
  margin-top: 0.5rem;
}
</style>
