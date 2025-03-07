<script setup lang="ts">
import { onMounted, ref, watchEffect } from 'vue';
import { loadAllImages, loadImageData, uploadImage, deleteImage } from "./http-api"
import Gallery from './Gallery.vue'

import { images, type ImageGallery } from "./images.ts";


const selectedImage = ref<ImageGallery | null>(null);
const file = ref<File | null>(null);
const isLoading = ref(false);
// for the gallery
onMounted(async () => {
  isLoading.value = true;
  try {
    await loadAllImages();
  } catch (error) {
    console.error('Failed to load images:', error);
  } finally {
    isLoading.value = false;
  }
});

const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files) {
    file.value = target.files[0];
  }
};

const submitFile = async () => {
  if (!file.value) return;
  
  isLoading.value = true;
  try {
    const success = await uploadImage(file.value);
    
    if (success) {
      file.value = null;
    }
  } catch (error) {
    console.error('Upload failed:', error);
  } finally {
    isLoading.value = false;
  }
};

const downloadImage = () => {
  if (selectedImage.value && selectedImage.value.dataUrl) {
    const link = document.createElement('a');
    link.href = selectedImage.value.dataUrl;
    link.download = selectedImage.value.name;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
};

const handleDeleteImage = async () => {
  if (!selectedImage.value) return;
  
  if (confirm(`Êtes-vous sûr de vouloir supprimer  "${selectedImage.value.name}"?`)) {
    isLoading.value = true;
    try {
      const success = await deleteImage(selectedImage.value.id);
      if (success) {
        selectedImage.value = null;
      }
    } catch (error) {
      console.error('Delete failed:', error);
    } finally {
      isLoading.value = false;
    }
  }
};

// used for when the selectedImage changes
watchEffect(async () => {
  if (selectedImage.value) {
    selectedImage.value.dataUrl = await loadImageData(selectedImage.value.id);
  }
});

</script>

<template>
  <h1>LuminaShare - Partage de photos</h1>

  <div class="upload-container">
      <div>
        <h2>Téléverser un fichier</h2>
        <label>Fichier
          <input type="file" @change="handleFileUpload( $event )"/>
        </label>
        <br>
        <button v-on:click="submitFile()">Envoyer</button>
      </div>
  </div>
  <div v-if="isLoading" class="loading-message">Chargement en cours...</div>
  <Gallery :images="images"/>

  <div class="select-image">
    <h3>Choisir une image à afficher</h3>
    <div v-if="selectedImage" class="image-actions">
      <button @click="downloadImage">Télécharger</button>
      <button @click="handleDeleteImage">Supprimer</button>
    </div>
    <p v-if="selectedImage">{{selectedImage.description}}</p>
    <br/>
    
    <select v-model="selectedImage">
      <option v-for="img in images" :key="img.id" :value="img">
        {{ img.name }}
      </option>
    </select>
    <br/>
    <img v-if="selectedImage && selectedImage.dataUrl" :src="selectedImage.dataUrl" :alt="selectedImage.name">
    

  </div>
</template>

<style scoped>
 body{
 background:
        linear-gradient(
          rgba(0, 0, 0, 0.6), 
          rgba(0, 0, 0, 0.6)
        ),
    }
h1, h2, h3 {
  color: #ffffff;
  margin-bottom: 1.5rem;
}

.select-image {
  margin-top: 2rem;
  padding: 1.5rem;
  background-color: #2a2a2a;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

img {
  max-width: 100%;
  border-radius: 8px;
  margin-top: 1.5rem;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s ease;
}

img:hover {
  transform: scale(1.02);
}

button {
  background-color: #4a4a4a;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

button:hover {
  background-color: #666;
}
</style>