<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getUserImages, getUserProfile, setImageLikes } from './http-api';
import { currentUser } from './users';
import { type ImageGallery } from './images.ts';
import UserImages from './UserImages.vue';

const route = useRoute();
const router = useRouter();
const userExists = ref(true);
const loading = ref(true);
const loadingImages = ref(true);
const userImages = ref<ImageGallery[]>([]);
const username = ref<string>('');
const userBio = ref<string>('');
const selectedImage = ref<ImageGallery | null>(null);

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
  const images = await getUserImages(userid.value, includePrivate);
  
  userImages.value = images.sort((a, b) => (b.likes || 0) - (a.likes || 0));
  
  loadingImages.value = false;
}

async function loadUserData() {
  if (isOwnProfile.value && currentUser.value) {
    username.value = currentUser.value.name;
    userBio.value = currentUser.value.bio || '';
  } else {
    const userProfile = await getUserProfile(userid.value);
    if (userProfile) {
      username.value = userProfile.name;
      userBio.value = userProfile.bio || '';
    } else {
      username.value = userid.value;
      userBio.value = '';
    }
  }
}

function handleImageSelect(image: ImageGallery) {
  selectedImage.value = image;
  router.push(`/edit?imageId=${image.id}`);
}

function handleImageUpdate(updatedImage: ImageGallery) {
  const index = userImages.value.findIndex(img => img.id === updatedImage.id);
  if (index !== -1) {
    userImages.value[index] = updatedImage;
  }
  
  if (selectedImage.value?.id === updatedImage.id) {
    selectedImage.value = updatedImage;
  }
}

async function loadUserProfile() {
  try {
    loading.value = true;
    selectedImage.value = null;

    if (!userid.value) {
      userExists.value = false;
      return;
    }
    
    await setImageLikes(1, 5);
    await setImageLikes(2, 15);

    await Promise.all([loadUserImages(), loadUserData()]);

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


onMounted(() => {
  loadUserProfile();
});
</script>

<template>
  <div class="profile panel">
    <div v-if="loading" class="loading">Loading profile...</div>

    <div v-else-if="!userExists" class="error-message">User not found</div>

    <div v-else-if="userImages.length === 0" class="no-images">
      <p>{{ isOwnProfile ? "You haven't" : "This user hasn't" }} uploaded any photos yet.</p>
      <button v-if="isOwnProfile" @click="navigateToUpload" class="upload-button">
        Upload Photos
      </button>
    </div>

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

        <div v-else class="gallery-wrapper">
          <UserImages 
        :images="userImages" 
        :showPrivacyToggle="isOwnProfile" 
        @select="handleImageSelect"
        @imageUpdated="handleImageUpdate"
      />




         
          
        </div>
      </div>
    </div>
  </div>
</template>

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

.gallery-wrapper {
  position: relative;
}

.image-details {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
}

.detail-card {
  background-color: #222;
  padding: 2rem;
  border-radius: 8px;
  max-width: 80%;
  text-align: center;
}

.image-name {
  font-weight: bold;
  margin-bottom: 1rem;
  font-size: 1.2rem;
}

.image-privacy {
  font-size: 0.9rem;
  opacity: 0.7;
  margin-bottom: 1rem;
}

.image-likes {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.like-button {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.5rem;
  transition: transform 0.2s;
}

.like-button:hover {
  transform: scale(1.2);
}

.close-button {
  background-color: #333;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}
</style>
