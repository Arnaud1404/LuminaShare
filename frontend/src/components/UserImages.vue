<script setup lang="ts">
import { type ImageGallery } from './images';
import { toggleLike, checkLikeStatus, toggleImagePrivacy } from './http-api';
import { currentUser } from './users';
import { onMounted } from 'vue';

const props = defineProps<{
  images: ImageGallery[],
  showPrivacyToggle?: boolean
}>();
const emit = defineEmits(['select', 'imageUpdated']);

onMounted(async () => {
  if (currentUser.value?.userid) {
    for (const image of props.images) {
      try {
        image.isLiked = await checkLikeStatus(image.id);
      } catch (error) {
        console.error(`Failed to check like status for image ${image.id}:`, error);
      }
    }
  }
});

async function selectImage(image: ImageGallery) {
  emit('select', image);
}

async function handleLike(event: Event, image: ImageGallery) {
  event.stopPropagation(); 

  if (!currentUser.value?.userid) {
    alert('Please log in to like images');
    return;
  }

  try {
    const result = await toggleLike(image.id);

    const updatedImage = { 
      ...image, 
      likes: result.likes,
      isLiked: result.isLiked 
    };
    emit('imageUpdated', updatedImage);
  } catch (error) {
    console.error('Failed to like image:', error);
  }
}

async function togglePrivacy(event: Event, image: ImageGallery) {
  event.stopPropagation();
  try {
    await toggleImagePrivacy(image.id);
    const updatedImage = { ...image, ispublic: !image.ispublic };
    emit('imageUpdated', updatedImage);
  } catch (error) {
    console.error('Failed to toggle image privacy:', error);
  }
}
</script>

<template>
  <div class="image-grid">
    <div
      v-for="image in images"
      :key="image.id"
      class="image-card"
      @click="selectImage(image)"
    >
      <div class="image-container">
        <img v-if="image.dataUrl" :src="image.dataUrl" :alt="image.name" />
      </div>
      <div class="card-footer">
        <div class="image-name">{{ image.name }}</div>
        <div class="card-actions">
          <div class="likes-container">
            <span class="like-count">{{ image.likes || 0 }}</span>
            <button @click="(e) => handleLike(e, image)" 
              class="like-button"
              :class="{ 'liked': image.isLiked }">
              {{ image.isLiked ? '❤️' : '🤍' }}
            </button>
          </div>
          <button 
            v-if="showPrivacyToggle"
            @click="(e) => togglePrivacy(e, image)" 
            class="privacy-toggle"
            :title="image.ispublic ? 'Public' : 'Private'">
            {{ image.ispublic ? '🌎' : '🔒' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 1.5rem;
  padding: 1rem;
}

.image-card {
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--card_background, #2a2a2a);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  cursor: pointer;
}

.image-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.image-container {
  height: 200px;
  overflow: hidden;
}

.image-container img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-card:hover .image-container img {
  transform: scale(1.05);
}

.card-footer {
  padding: 0.8rem;
}

.image-name {
  font-weight: 500;
  margin-bottom: 0.5rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.likes-container {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.like-count {
  font-size: 0.9rem;
}

.like-button, .privacy-toggle {
  background: none;
  border: none;
  font-size: 1.1rem;
  cursor: pointer;
  padding: 0.2rem;
  transition: transform 0.2s;
}

.like-button:hover, .privacy-toggle:hover {
  transform: scale(1.2);
}

.like-button.liked {
  color: red;
  animation: pulse 0.3s ease-in-out;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

.privacy-toggle {
  opacity: 0.8;
}

.privacy-toggle:hover {
  opacity: 1;
  transform: scale(1.2);
}
</style>