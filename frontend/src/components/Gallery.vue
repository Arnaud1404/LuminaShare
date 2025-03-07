<script setup lang="ts">
import {images} from './images';

</script>

<template>
    <div class="image-grid">
    <div v-for="img in images" :key="img.id" class="image-container">
     <img v-if="img.dataUrl" :src="img.dataUrl" :alt="img.name">
      <p>{{ img.name }}</p>
    </div>
  </div>
</template>

<style scoped>
img:hover {
  transform: scale(1.02);
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
