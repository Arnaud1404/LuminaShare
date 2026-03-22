<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { loadAllPublicImages } from './http-api';
import { type ImageGallery } from './images.ts';
import UserImages from './UserImages.vue';
import { useRouter } from 'vue-router';

const publicImages = ref<ImageGallery[]>([]);
const loading = ref(true);
const router = useRouter();
const error = ref<string | null>(null);

async function loadPublicImages() {
  try {
    loading.value = true;
    console.log('Fetching public images...');
    const images = await loadAllPublicImages();

    publicImages.value = images.sort((a, b) => (b.likes || 0) - (a.likes || 0));

    console.log('Loaded public images:', publicImages.value.length);

    if (publicImages.value.length === 0) {
      console.log(
        'No public images found. Check if any images are marked as public in the database.'
      );
    }
  } catch (err) {
    console.error('Error loading public images:', err);
    error.value = 'Failed to load images. Please try again later.';
  } finally {
    loading.value = false;
  }
}

function handleImageSelect(image: ImageGallery) {
  router.push(`/edit?imageId=${image.id}`);
}

function handleImageUpdate(updatedImage: ImageGallery) {
  const index = publicImages.value.findIndex((img) => img.id === updatedImage.id);
  if (index !== -1) {
    publicImages.value[index] = updatedImage;
  }
}

onMounted(() => {
  loadPublicImages();
});
</script>

<template>
  <div class="home-container">
    <div class="compact-header">
      <h1>Image Gallery</h1>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Loading amazing images...</p>
    </div>

    <div v-else-if="error" class="error-container">
      <p>{{ error }}</p>
      <button @click="loadPublicImages" class="retry-button">Try Again</button>
    </div>

    <div v-else-if="publicImages.length === 0" class="empty-state">
      <p>No public images available yet.</p>
    </div>

    <div v-else class="images-container">
      <UserImages
        :images="publicImages"
        :showPrivacyToggle="false"
        @select="handleImageSelect"
        @imageUpdated="handleImageUpdate"
      />
    </div>
  </div>
</template>

<style scoped>
.home-container {
  max-width: 100%;
  margin: 0 auto;
  padding: 0.5rem;
}

.compact-header {
  text-align: center;
  margin-bottom: 1rem;
  padding: 0.5rem;
}

.compact-header h1 {
  font-size: 1.8rem;
  margin: 0;
}

.loading-container,
.error-container,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  text-align: center;
}

.loading-spinner {
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top: 4px solid var(--color_button, #4caf50);
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.retry-button {
  background-color: var(--color_button, #4caf50);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 1rem;
}

.images-container {
  width: 100%;
}
</style>
