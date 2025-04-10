<script setup lang="ts">
import { type ImageGallery } from './images';
import {ref } from 'vue';


defineProps<{ image: ImageGallery|null }>();

const isFullscreen = ref(false);

function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value;
}

</script>

<template>

<div class="selected-image-container">
        <div v-if="image" class="image-display">
          <img
            v-if="image.dataUrl"
            :src="image.dataUrl"
            :alt="image.name"
            @click="toggleFullscreen"
            style="cursor: pointer"
          />
          <p v-else>Chargement de l'image...</p>
        </div>
        <p v-else>Aucune image sélectionnée</p>
</div>
</template>