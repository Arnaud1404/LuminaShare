<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import {
  loadAllImages,
  loadImageData,
  uploadImage,
  deleteImage,
  getSimilarImages
} from "./http-api";
import Gallery from "./Gallery.vue";
import Similar from "./Similar.vue";
import { images, type ImageGallery } from "./images.ts";
import Notification, { type NotificationType } from "./Notification.vue";

type NotificationRef = {
  showNotification: (message: string, type: NotificationType) => void;
};

const notification = ref<NotificationRef | null>(null);
const selectedImage = ref<ImageGallery | null>(null);
const file = ref<File | null>(null);
const allowedFileTypes = ["image/jpeg", "image/png"];
const descriptor = ref("rgbcube");
const similarCount = ref(3);
const similarImages = ref<ImageGallery[]>([]);
const isFileValid = ref(false);
const isFullscreen = ref(false);
const showMetadata = ref(false);
const isUploading = ref(false);


const fetchSimilarImages = async () => {
  if (!selectedImage.value) return;
  try {
    similarImages.value = await getSimilarImages(
      selectedImage.value.id,
      similarCount.value,
      descriptor.value
    );
    let n = 
      similarImages.value.length;
    if (n == 0) {
      notification.value?.showNotification(
      `Aucune image similaire trouvée`,
      "error"
    );
    }
    else {
      notification.value?.showNotification(
      `${n} images similaires trouvées`,
      "success"
    );
    }
    
  } catch (error: any) {
    console.error("Failed to get similar images:", error);
    const errorMessage = formatErrorMessage(error);
    notification.value?.showNotification(
      `Échec de recherche: ${errorMessage}`,
      "error"
    );
  }
};

function toggleMetadata() {
  showMetadata.value = !showMetadata.value;
}
function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value;
}
function formatErrorMessage(error: any): string {
  if (!error) return "Erreur inconnue";

  // Erreurs Axios
  if (error.code === "ERR_BAD_RESPONSE" || error.code === "ERR_BAD_REQUEST") {
    if (error.response && error.response.status) {
      const status = error.response.status;
      let message = error.response.statusText || '';
      
      if (error.response.data) {
        if (typeof error.response.data === 'string') {
          message = error.response.data;
        } else if (error.response.data.message) {
          message = error.response.data.message;
        }
      }
      
      return `${status}: ${message || error.message}`;
    }
  }

  // Erreurs HTTP basiques
  if (error.status && error.message) {
    return `${error.status}: ${error.message}`;
  }

  return error.message || "Erreur inconnue";
}

watchEffect(() => {
  if (file.value) {
    if (allowedFileTypes.includes(file.value.type)) {
      isFileValid.value = true;
    } else {
      isFileValid.value = false;
      notification.value?.showNotification(
        "Type de fichier non valide. Seuls JPEG et PNG sont acceptés.",
        "error",
      );
    }
  } else {
    isFileValid.value = false;
  }
});

onMounted(async () => {
  try {
    await loadAllImages();
    notification.value?.showNotification(
      "Images chargées avec succès",
      "success",
    );
  } catch (error: any) {
    console.error("Failed to load images:", error);
    const errorMessage = formatErrorMessage(error);
    notification.value?.showNotification(
      `Échec du chargement des images: ${errorMessage}`,
      "error",
    );
  } 
});

const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files) {
    file.value = target.files[0];
  }
};

const submitFile = async () => {
  if (!file.value || isUploading.value) return;
  
  isUploading.value = true;
  
  try {
    await uploadImage(file.value);
    file.value = null;
    notification.value?.showNotification(
      "Image téléversée avec succès",
      "success",
    );
  } catch (error: any) {
    console.error("Upload failed:", error);
    const errorMessage = formatErrorMessage(error);
    notification.value?.showNotification(
      `Échec du téléversement: ${errorMessage}`,
      "error",
    );
  } finally {
    file.value = null;
    isFileValid.value = false;
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fileInput) fileInput.value = '';
    isUploading.value = false;
  }
};

const downloadImage = () => {
  if (selectedImage.value && selectedImage.value.dataUrl) {
    const link = document.createElement("a");
    link.href = selectedImage.value.dataUrl;
    link.download = selectedImage.value.name;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    notification.value?.showNotification(
      `Téléchargement de ${selectedImage.value.name} démarré`,
      "info",
    );
  }
};

const handleDeleteImage = async () => {
  if (!selectedImage.value) return;

  if (
    confirm(
      `Êtes-vous sûr de vouloir supprimer  "${selectedImage.value.name}"?`,
    )
  ) {
    try {
      const success = await deleteImage(selectedImage.value.id);
      if (success) {
        notification.value?.showNotification(
          `${selectedImage.value.name} supprimée avec succès`,
          "success",
        );
        selectedImage.value = null;
        similarImages.value = [];
      } else {
        notification.value?.showNotification(
          "Échec de la suppression",
          "error",
        );
      }
    } catch (error: any) {
      console.error("Failed to load image data:", error);
      const errorMessage = formatErrorMessage(error);
      notification.value?.showNotification(
        `Impossible de charger l'image: ${errorMessage}`,
        "error",
      );
    } 
  }
};

function handleImageSelect(image: ImageGallery) {
  selectedImage.value = image;
}
watchEffect(async () => {
  if (selectedImage.value) {
    try {
      selectedImage.value.dataUrl = await loadImageData(selectedImage.value.id);
    } catch (error) {
      console.error("Failed to load image data:", error);
      const errorMessage = formatErrorMessage(error);
      notification.value?.showNotification(
        `Impossible de charger l'image: ${errorMessage}`,
        "error",
      );
    }
  }
});
</script>

<template>
  <Notification ref="notification" />
  <h1>LuminaShare - Partage de photos</h1>
<div v-if="isFullscreen" class="fullscreen-overlay" @click="toggleFullscreen">
    <img 
      v-if="selectedImage && selectedImage.dataUrl" 
      :src="selectedImage.dataUrl" 
      :alt="selectedImage.name" 
    />
  </div>
  <div class="two-column-layout">
    <!-- LEFT COLUMN - 50% width -->
    <div class="left-column">
      <!-- Selected Image Area - 60% height -->
      <div class="selected-image-container">
        <h2>Image sélectionnée</h2>
        <div v-if="selectedImage" class="image-display">
          <img v-if="selectedImage.dataUrl" :src="selectedImage.dataUrl" :alt="selectedImage.name" @click="toggleFullscreen"
            style="cursor: pointer;"/>
          <p v-else>Chargement de l'image...</p>
        </div>
        <p v-else>Aucune image sélectionnée</p>
      </div>

      <!-- Similar Images - 40% height -->
<div class="similar-section">
  <h3>Images similaires - Plus le score est bas, plus l'image est similaire</h3>
  <div class="similar-filters">
    <select v-model="descriptor">
      <option value="rgbcube">3D RGB</option>
      <option value="huesat">2D Teinte Saturation</option>
    </select>
    <input type="number" v-model="similarCount" min="1" max="10">
    <button @click="fetchSimilarImages">Rechercher</button>
  </div>
  <div class="similar-results">
    <Similar :images="similarImages" @select="handleImageSelect"/>
  </div>
</div>
</div>

    <!-- RIGHT COLUMN - 40% width -->
    <div class="right-column">
      <!-- Image Actions - 10% height -->
      <div class="image-actions">
        <h3>Actions</h3>
        <div class="action-buttons" v-if="selectedImage">
          <button @click="downloadImage">Télécharger</button>
          <button @click="toggleMetadata">Métadonnées</button>
          <button @click="handleDeleteImage" class="delete-button">Supprimer</button>
        </div>
        <p v-else>Sélectionnez une image pour voir les actions disponibles</p>
      </div>

      <div v-if="showMetadata && selectedImage" class="metadata-popup">
        <div class="metadata-content">
          <h3>Métadonnées</h3>
          <p><strong>Identifiant:</strong> {{ selectedImage.id }}</p>
          <p><strong>Nom:</strong> {{ selectedImage.name }}</p>
          <p><strong>Type:</strong> {{ selectedImage.type }}</p>
          <p><strong>Taille:</strong> {{ selectedImage.size }}</p>
          <button @click="toggleMetadata" class="close-button">Fermer</button>
        </div>
      </div>

      <!-- Upload Area - 10% height -->
      <div class="upload-area">
        <h3>Téléverser une image</h3>
        <div style="display: flex; gap: 10px;">
          <input type="file" @change="handleFileUpload" style="flex: 1;" />
          <button @click="submitFile" :disabled="isUploading">
            {{ isUploading ? 'Téléversement...' : 'Téléverser' }}
          </button>
        </div>
      </div>

      <!-- Gallery - 80% height, scrollable -->
      <div class="gallery-container">
        <h3>Galerie</h3>
        <div class="scrollable-gallery">
          <Gallery :images="images" @select="handleImageSelect" />
        </div>
      </div>
    </div>
  </div>
</template>


<style scoped>
.fullscreen-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: hsla(0, 0%, 0%, 0.9);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.fullscreen-overlay img {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

select {
  background: hsl(0, 0%, 20%);
  color: white;
  border: 1px solid hsl(0, 0%, 33%);
  padding: 5px;
  border-radius: 4px;
  min-width: 120px;
}

input[type="number"] {
  background: hsl(0, 0%, 20%);
  color: white;
  border: 1px solid hsl(0, 0%, 33%);
  padding: 5px;
  border-radius: 4px;
  width: 60px;
}

.two-column-layout {
  display: flex;
  height: 85vh;
  width: 100%;
}

.left-column {
  width: 50%;
  padding: 10px;
  display: flex;
  flex-direction: column;
}

.selected-image-container {
  height: 60%;
  background-color: hsl(0, 0%, 16%);
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 10px;
  overflow: hidden;
}

.image-display {
  height: 85%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-display img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 4px;
}

.metadata-area {
  height: 10%;
  background-color: hsl(0, 0%, 16%);
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 10px;
  overflow: auto;
}

.similar-section {
  height: 40%;
  background-color: hsl(0, 0%, 16%);
  border-radius: 8px;
  padding: 10px;
}

.similar-filters {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
}

.similar-results {
  overflow: hidden;
}

.right-column {
  width: 50%;
  padding: 10px;
  display: flex;
  flex-direction: column;
}

.image-actions {
  height: 10%;
  background-color: hsl(0, 0%, 16%);
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 10px;
}

.metadata-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.metadata-content {
  background-color: hsl(0, 0%, 16%);
  padding: 20px;
  border-radius: 8px;
  max-width: 500px;
  width: 100%;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.upload-area {
  height: 10%;
  background-color: #2a2a2a;
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
}

.upload-area input {
  margin-bottom: 5px;
}

.gallery-container {
  height: 80%;
  background-color: hsl(0, 0%, 16%);
  border-radius: 8px;
  padding: 10px;
}

.scrollable-gallery {
  height: 90%;
  overflow-y: auto;
}

.delete-button {
  background-color: rgb(196, 33, 33);
}

button {
  background-color: hsl(0, 0%, 29%);
  color: white;
  border: none;
  border-radius: 4px;
  padding: 5px 10px;
  cursor: pointer;
}

button:hover {
  background-color: hsl(0, 0%, 35%);
}

button:disabled {
  background-color: hsl(0, 0%, 23%);
  cursor: not-allowed;
}

h1, h2, h3 {
  color: #ffffff;
  text-align: center;
  margin-bottom: 10px;

}

</style>