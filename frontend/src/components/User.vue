<template>
  <div class="profile panel">
    <div v-if="loading" class="loading">Loading profile...</div>

    <div v-else-if="!userExists" class="error-message">User not found</div>

    <div v-else>
      <div class="profile-header">
        <h1>{{ isOwnProfile ? 'Your Profile' : `${username}'s Profile` }}</h1>
      </div>

      <div class="user-info">
        <h2>{{ username }}</h2>
        <p v-if="userBio">{{ userBio }}</p>
        <p v-else class="no-bio">No bio provided</p>
      </div>

      <div class="images-section">
        <h2>{{ isOwnProfile ? 'Your Photos' : `${username}'s Photos` }}</h2>

        <div v-if="loadingImages" class="loading">Loading images...</div>

        <div v-else-if="userImages.length === 0" class="no-images">
          <p>{{ isOwnProfile ? "You haven't" : "This user hasn't" }} uploaded any photos yet.</p>
          <button v-if="isOwnProfile" @click="navigateToUpload" class="upload-button">
            Upload Photos
          </button>
        </div>

        <div v-else class="image-grid">
          <div v-for="image in userImages" :key="image.id" class="image-card">
            <img :src="image.dataUrl" :alt="image.name" />
            <div class="image-info">
              <div class="image-name">{{ image.name }}</div>
              <div v-if="isOwnProfile" class="image-privacy">
                {{ image.ispublic ? 'Public' : 'Private' }}
              </div>
              <div class="image-likes">
                <span>{{ image.likes || 0 }} likes</span>
                <button @click="likeImage(image.id)" class="like-button">❤️</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getUserImages, likeImage as likeImageApi } from './http-api';
import { currentUser } from './users';
import { type ImageGallery } from './images.ts';

const route = useRoute();
const router = useRouter();
const userExists = ref(true);
const loading = ref(true);
const loadingImages = ref(true);
const userImages = ref<ImageGallery[]>([]);
const username = ref<string>('');
const userBio = ref<string>('');

const userid = ref<string>((route.params.userid as string) || currentUser.value?.userid || '');
watch(
  () => route.params.userid,
  (newUserid) => {
    if (newUserid) {
      userid.value = newUserid as string;
      loadUserProfile();
    } else if (currentUser.value) {
      userid.value = currentUser.value.userid;
      loadUserProfile();
    }
  }
);

const isOwnProfile = computed(() => {
  return currentUser.value?.userid === userid.value;
});

async function loadUserImages() {
  loadingImages.value = true;
  const includePrivate = isOwnProfile.value;
  userImages.value = await getUserImages(userid.value, includePrivate);
  loadingImages.value = false;
}

async function loadUserProfile() {
  try {
    loading.value = true;

    if (!userid.value) {
      userExists.value = false;
      return;
    }

    await loadUserImages();

    if (isOwnProfile.value && currentUser.value) {
      username.value = currentUser.value.name;
      userBio.value = currentUser.value.bio || '';
    } else {
      username.value = userid.value;
    }

    userExists.value = true;
  } catch (error) {
    console.error('Failed to load user profile:', error);
    userExists.value = false;
  } finally {
    loading.value = false;
  }
}

function navigateToUpload() {
  router.push('/edit');
}

async function likeImage(imageId: number): Promise<void> {
  await likeImageApi(imageId);
  const image = userImages.value.find((img) => img.id === imageId);
  if (image) {
    image.likes = (image.likes || 0) + 1;
  }
}
onMounted(() => {
  loadUserProfile();
});
</script>

<style scoped>
.profile {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.loading,
.error-message {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
}

.profile-header {
  margin-bottom: 2rem;
  border-bottom: 1px solid #333;
  padding-bottom: 1rem;
}

.user-info {
  margin-bottom: 2rem;
}

.no-bio {
  font-style: italic;
  opacity: 0.7;
}

.images-section {
  margin-top: 2rem;
}

.no-images {
  text-align: center;
  padding: 2rem;
}

.upload-button {
  background-color: var(--color_button, #4caf50);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 1rem;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1.5rem;
}

.image-card {
  border-radius: 8px;
  overflow: hidden;
  background-color: rgba(255, 255, 255, 0.1);
  transition: transform 0.2s;
}

.image-card:hover {
  transform: scale(1.02);
}

.image-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}

.image-info {
  padding: 1rem;
}

.image-name {
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.image-privacy {
  font-size: 0.8rem;
  opacity: 0.7;
  margin-bottom: 0.5rem;
}

.image-likes {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.like-button {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.2rem;
  transition: transform 0.2s;
}

.like-button:hover {
  transform: scale(1.2);
}
</style>
