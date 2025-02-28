<script setup lang="ts">
import axios from 'axios';
import { onMounted, ref, watchEffect } from 'vue';
import { loadAllImages, loadImageData, getImagesAsJSON } from "./http-api"

import type { ImageGallery } from "./http-api"

import Gallery from './Gallery.vue'


const images = ref<ImageGallery[]>([]);
const selectedImage = ref<ImageGallery | null>(null);
const file = ref<File | null>(null);

// for the gallery
onMounted(() => loadAllImages()
      .then(response => images.value = response)
      .catch(error => console.log(error)))

getImagesAsJSON()
  .then(result => {
    console.log(result)
    images.value = result;
    console.log(images.value)})
  .catch(error => console.log(error));

const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files) {
    file.value = target.files[0];
  }
};

const submitFile = async () => {
  if (!file.value) return;
  
  const formData = new FormData();
  formData.append('file', file.value);
  
  try {
    await axios.post('/images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    // Refresh the images list after a successful upload
    loadAllImages()
      .then(response => images.value = response)
      .catch(error => console.log(error));
    file.value = null;
  } catch (error) {
    console.error('Upload failed:', error);
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


// used for when the selectedImage changes
watchEffect(async () => {
  if (selectedImage.value) {
    selectedImage.value.dataUrl = await loadImageData(selectedImage.value.id);
  }
});

</script>

<template>
  <h1>Gallery</h1>

  <div class="container">
      <div>
        <h2>Upload File</h2>
        <label>File
          <input type="file" @change="handleFileUpload( $event )"/>
        </label>
        <br>
        <button v-on:click="submitFile()">Submit</button>
      </div>
  </div>

  <Gallery :images="images"/>

  <div class="select-image">
    <h3>Choose an image to display</h3>
    <br/>
    <button v-if="selectedImage && selectedImage.dataUrl" @click="downloadImage">Save Image</button>
    <br/>
    
    <select v-model="selectedImage">
      <option v-for="img in images" :key="img.id" :value="img">
        {{ img.name }}
      </option>
    </select>
    <br/>
    <!-- Render only if an image has been chosen -->
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