<script setup lang="ts">
import { onMounted, ref, watchEffect } from 'vue';
import {
  loadAllImages,
  loadImageData,
  deleteImage,
  getSimilarImages,
  getImageFilter,
  uploadImage,
} from './http-api';
import Gallery from './Gallery.vue';
import Similar from './Similar.vue';
import DisplayImage from './DisplayImage.vue';

import { images, type ImageGallery } from './images.ts';
import Notification, { type NotificationType } from './Notification.vue';
import { useRoute } from 'vue-router';

type NotificationRef = {
  showNotification: (message: string, type: NotificationType) => void;
};
const route = useRoute();
const notification = ref<NotificationRef | null>(null);
const selectedImage = ref<ImageGallery | null>(null);
const filtrerImage = ref<ImageGallery | null>(null);
const infoFilter = ref('gradienImage');
const FilterPourcent = ref(1);

const file = ref<File | null>(null);
const allowedFileTypes = ['image/jpeg', 'image/png'];
const descriptor = ref('rgbcube');
const similarCount = ref(3);
const similarImages = ref<ImageGallery[]>([]);
const isFileValid = ref(false);
const isUploading = ref(false);
const showMetadata = ref(false);
const resizeWidth = ref(300);
const resizeHeight = ref(300);
const mirrorDirection = ref('mirrorh');

const fetchSimilarImages = async () => {
  if (!selectedImage.value) return;
  try {
    similarImages.value = await getSimilarImages(
      selectedImage.value.id,
      similarCount.value,
      descriptor.value
    );
    let n = similarImages.value.length;
    if (n == 0) {
      notification.value?.showNotification(`Aucune image similaire trouvée`, 'error');
    } else {
      notification.value?.showNotification(`${n} images similaires trouvées`, 'success');
    }
  } catch (error: any) {
    console.error('Failed to get similar images:', error);
    const errorMessage = formatErrorMessage(error);
    notification.value?.showNotification(`Échec de recherche: ${errorMessage}`, 'error');
  }
};

function toggleMetadata() {
  showMetadata.value = !showMetadata.value;
}

function formatErrorMessage(error: any): string {
  if (!error) return 'Erreur inconnue';

  // Erreurs Axios
  if (error.code === 'ERR_BAD_RESPONSE' || error.code === 'ERR_BAD_REQUEST') {
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

  return error.message || 'Erreur inconnue';
}

watchEffect(() => {
  if (file.value) {
    if (allowedFileTypes.includes(file.value.type)) {
      isFileValid.value = true;
    } else {
      isFileValid.value = false;
      notification.value?.showNotification(
        'Type de fichier non valide. Seuls JPEG et PNG sont acceptés.',
        'error'
      );
    }
  } else {
    isFileValid.value = false;
  }
});

onMounted(async () => {
  try {
    await loadAllImages();
    notification.value?.showNotification('Images chargées avec succès', 'success');

    const imageId = route.query.imageid ? Number(route.query.imageid) : null;
    if (imageId) {
      const imageToSelect = images.value.find((img) => img.id === imageId);
      if (imageToSelect) {
        selectedImage.value = imageToSelect;
      }
    }
  } catch (error: any) {
    console.error('Failed to load images:', error);
    const errorMessage = formatErrorMessage(error);
    notification.value?.showNotification(
      `Échec du chargement des images: ${errorMessage}`,
      'error'
    );
  }
});

const downloadImage = () => {
  if (filtrerImage.value && filtrerImage.value.dataUrl) {
    const link = document.createElement('a');
    link.href = filtrerImage.value.dataUrl;
    link.download = filtrerImage.value.name;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    notification.value?.showNotification(
      `Téléchargement de ${filtrerImage.value.name} démarré`,
      'info'
    );
  } else {
    notification.value?.showNotification(
      `Impossible de Télécharger l'image car pas de filtre appliqué à l'image sélectionnée}`,
      'error'
    );
  }
};

const handleDeleteImage = async () => {
  if (!selectedImage.value) return;

  if (confirm(`Êtes-vous sûr de vouloir supprimer  "${selectedImage.value.name}"?`)) {
    try {
      const success = await deleteImage(selectedImage.value.id);
      if (success) {
        notification.value?.showNotification(
          `${selectedImage.value.name} supprimée avec succès`,
          'success'
        );
        selectedImage.value = null;
        similarImages.value = [];
      } else {
        notification.value?.showNotification('Échec de la suppression', 'error');
      }
    } catch (error: any) {
      console.error('Failed to load image data:', error);
      const errorMessage = formatErrorMessage(error);
      notification.value?.showNotification(
        `Impossible de charger l'image: ${errorMessage}`,
        'error'
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
      console.error('Failed to load image data:', error);
      const errorMessage = formatErrorMessage(error);
      notification.value?.showNotification(
        `Impossible de charger l'image: ${errorMessage}`,
        'error'
      );
    }
  }
});
watchEffect(async () => {
  if (filtrerImage.value) {
    try {
      filtrerImage.value.dataUrl = await loadImageData(filtrerImage.value.id);
    } catch (error) {
      console.error('Failed to load image data:', error);
      const errorMessage = formatErrorMessage(error);
      notification.value?.showNotification(
        `Impossible de charger l'image: ${errorMessage}`,
        'error'
      );
    }
  }
});

const Apply_filter = async () => {
  if (!selectedImage.value) return;
  filtrerImage.value = JSON.parse(JSON.stringify(selectedImage.value));
  if (!filtrerImage.value) return;
  try {
    console.log('avant url est ' + filtrerImage.value.dataUrl);
    if (infoFilter.value === 'resize') {
      filtrerImage.value.dataUrl = await getImageFilter(
        filtrerImage.value.id,
        'resize',
        resizeWidth.value,
        resizeHeight.value
      );
    } else if (infoFilter.value === 'mirror') {
      filtrerImage.value.dataUrl = await getImageFilter(
        filtrerImage.value.id,
        mirrorDirection.value,
        0
      );
    } else {
      filtrerImage.value.dataUrl = await getImageFilter(
        filtrerImage.value.id,
        infoFilter.value,
        FilterPourcent.value
      );
    }

    console.log('image = ' + filtrerImage.value.name + 'url est' + filtrerImage.value.dataUrl);
  } catch (error) {
    console.error('Failed to load Altered image:', error);
    const errorMessage = formatErrorMessage(error);
    notification.value?.showNotification(
      `Impossible de charger l'image alteré: ${errorMessage}`,
      'error'
    );
  }
};

const submitFile = async () => {
  if (!file.value || isUploading.value) return;

  isUploading.value = true;

  try {
    await uploadImage(file.value);
    file.value = null;
    notification.value?.showNotification('Image téléversée avec succès', 'success');
  } catch (error: any) {
    console.error('Upload failed:', error);
    const errorMessage = formatErrorMessage(error);
    notification.value?.showNotification(`Échec du téléversement: ${errorMessage}`, 'error');
  } finally {
    file.value = null;
    isFileValid.value = false;
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fileInput) fileInput.value = '';
    isUploading.value = false;
  }
};
</script>

<template>
  <Notification ref="notification" />

  <div class="two-column-layout">
    <!-- LEFT COLUMN - 50% width -->
    <div class="left-column">
      <h2>
        {{ $t('edit.title_selected_img') }}
      </h2>
      <div class="selected-image-container">
        <DisplayImage :image="selectedImage" />
      </div>
      <div v-if="selectedImage" class="option-transfo">
        <select v-model="infoFilter">
          <option value="gradienImage">
            {{ $t('button.blur') }}
          </option>
          <option value="modif_lum">
            {{ $t('button.brightness') }}
          </option>
          <option value="invert">
            {{ $t('button.reverse') }}
          </option>
          <option value="rotation">
            {{ $t('button.rotation') }}
          </option>
          <option value="resize">
            {{ $t('button.resize') }}
          </option>
          <option value="mirror">
            {{ $t('button.mirror') }}
          </option>
        </select>
        <input
          v-if="infoFilter === 'gradienImage' || infoFilter === 'modif_lum'"
          type="number"
          v-model="FilterPourcent"
          min="0"
          max="100"
        />

        <select v-if="infoFilter === 'rotation'" v-model="FilterPourcent">
          <option value="90">90°</option>
          <option value="180">180°</option>
          <option value="270">270°</option>
        </select>
        <div v-if="infoFilter === 'resize'">
          <input type="number" v-model.number="resizeWidth" min="1" placeholder="Largeur (px)" />
          <input type="number" v-model.number="resizeHeight" min="1" placeholder="Hauteur (px)" />
        </div>

        <!-- Mirror : choix horizontal / vertical -->
        <select v-if="infoFilter === 'mirror'" v-model="mirrorDirection">
          <option value="mirrorh">{{ $t('label.horizontal') }}</option>
          <option value="mirrorv">{{ $t('label.vertical') }}</option>
        </select>

        <button @click="Apply_filter">
          {{ $t('button.apply') }}
        </button>
      </div>

      <div class="similar-section">
        <h3>
          {{ $t('image_similar.title') }}
        </h3>
        <div class="similar-filters">
          <select v-model="descriptor">
            <option value="rgbcube">
              {{ $t('button.3D') }}
            </option>
            <option value="huesat">
              {{ $t('button.2D') }}
            </option>
          </select>
          <input type="number" v-model="similarCount" min="1" max="10" />
          <button @click="fetchSimilarImages">
            {{ $t('button.research') }}
          </button>
        </div>
        <div class="similar-results">
          <Similar :images="similarImages" @select="handleImageSelect" />
        </div>
      </div>

      <!-- RIGHT COLUMN - 50% width -->
    </div>
    <div class="right-column">
      <h2>
        {{ $t('edit.title_filter_img') }}
      </h2>
      <div class="selected-image-container">
        <DisplayImage :image="filtrerImage" />
      </div>
      <div v-if="selectedImage" class="image-actions">
        <div class="action-buttons">
          <button @click="downloadImage">
            {{ $t('button.download') }}
          </button>
          <button @click="toggleMetadata">
            {{ $t('button.metadata') }}
          </button>
          <button @click="handleDeleteImage" class="delete-button">
            {{ $t('button.delete') }}
          </button>
          <button @click="submitFile" :disabled="isUploading">
            {{ isUploading ? 'Téléversement...' :'Téléverser' }}
          </button>
        </div>
      </div>
      <div v-if="showMetadata && selectedImage" class="metadata-popup">
        <div class="metadata-content panel">
          <h3>
            {{ $t('button.metadata') }}
          </h3>
          <p>
            <strong>
              {{ $t('edit.id') }}
            </strong>
            {{ selectedImage.id }}
          </p>
          <p>
            <strong>
              {{ $t('edit.name') }}
            </strong>
            {{ selectedImage.name }}
          </p>
          <p>
            <strong>
              {{ $t('edit.type') }}
            </strong>
            {{ selectedImage.type }}
          </p>
          <p>
            <strong>
              {{ $t('edit.length') }}
            </strong>
            {{ selectedImage.size }}
          </p>
          <button @click="toggleMetadata" class="close-button">
            {{ $t('edit.close') }}
          </button>
        </div>
      </div>

      <div class="gallery-container">
        <h3>
          {{ $t('edit.title_gallery') }}
        </h3>
        <div class="scrollable-gallery">
          <Gallery :images="images" @select="handleImageSelect" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
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
  background-color: var(--panel_color);
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 10px;
  overflow: hidden;
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
  background-color: var(--panel_color);
  border-radius: 8px;
  padding: 10px;
}

.similar-filters {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
}

.option-transfo {
  height: 5%;
  padding: 10px;
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
  background-color: var(--panel_color);
  border-radius: 8px;
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
  height: 5%;
  background-color: var(--panel_color);
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

.gallery-container {
  height: 40%;
  background-color: var(--panel_color);
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
</style>
