<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import {
  loadAllImages,
  loadImageData,
  uploadImage,
  deleteImage,
} from "./http-api";
import Gallery from "./Gallery.vue";
import { images, type ImageGallery } from "./images.ts";
import Notification, { type NotificationType } from "./Notification.vue";

type NotificationRef = {
  showNotification: (message: string, type: NotificationType) => void;
};

const notification = ref<NotificationRef | null>(null);
const selectedImage = ref<ImageGallery | null>(null);
const file = ref<File | null>(null);
const isLoading = ref(false);
const allowedFileTypes = ["image/jpeg", "image/png"];
const isFileValid = ref(false);

function formatErrorMessage(error: any): string {
  if (!error) return "Erreur inconnue";

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
  isLoading.value = true;
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
      notification.value?.showNotification(
        "Image téléversée avec succès",
        "success",
      );
    }
  } catch (error: any) {
    console.error("Upload failed:", error);
    const errorMessage = formatErrorMessage(error);
    notification.value?.showNotification(
      `Échec du téléversement: ${errorMessage}`,
      "error",
    );
  } finally {
    isLoading.value = false;
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
    isLoading.value = true;
    try {
      const success = await deleteImage(selectedImage.value.id);
      if (success) {
        notification.value?.showNotification(
          `${selectedImage.value.name} supprimée avec succès`,
          "success",
        );
        selectedImage.value = null;
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
    } finally {
      isLoading.value = false;
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

  <div class="upload-container">
    <div>
      <h2>Téléverser un fichier</h2>
      <label
        >Fichier
        <input type="file" @change="handleFileUpload($event)" />
      </label>
      <br />
      <button v-on:click="submitFile()" :disabled="!isFileValid">
        Envoyer
      </button>
    </div>
  </div>
  <div v-if="isLoading" class="loading-message">Chargement en cours...</div>

  <Gallery :images="images" @select="handleImageSelect" />

  <div class="select-image">
    <h3>Image sélectionnée</h3>
    <div v-if="selectedImage" class="image-actions">
      <button @click="downloadImage">Télécharger</button>
      <button @click="handleDeleteImage">Supprimer</button>
      <p>{{ selectedImage.name }}</p>
      <p v-if="selectedImage.description">{{ selectedImage.description }}</p>
      <img
        v-if="selectedImage.dataUrl"
        :src="selectedImage.dataUrl"
        :alt="selectedImage.name"
      />
    </div>
    <p v-else>Cliquez sur une image dans la galerie pour la sélectionner</p>
  </div>
</template>

<style scoped>
body {
  background: linear-gradient(rgba(0, 0, 0, 0.6), rgba(0, 0, 0, 0.6));
}
h1,
h2,
h3 {
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

button:disabled {
  background-color: #2a2a2a;
  color: #666;
  cursor: not-allowed;
}
</style>
