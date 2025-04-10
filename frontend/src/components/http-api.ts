import axios, { type AxiosResponse } from 'axios';
import { images, type ImageGallery } from './images';

export async function getImagesAsJSON() {
  let json: ImageGallery[] = [];
  await axios
    .get('/images')
    .then((response) => (json = response.data))
    .catch((error) => console.error(error));
  return json;
}
export async function getSimilarImagesAsJSON(
  id: number,
  count: number = 5,
  descriptor: string = 'rgbcube'
): Promise<ImageGallery[]> {
  let json: ImageGallery[] = [];
  try {
    const response = await axios.get(
      `/images/${id}/similar?number=${count}&descriptor=${descriptor}`
    );
    json = response.data;
  } catch (error) {
    console.error(error);
  }
  return json;
}
export async function loadImageData(imageID: number): Promise<string> {
  return axios
    .get(`/images/${imageID}`, { responseType: 'blob' })
    .then(function (response: AxiosResponse) {
      return new Promise<string>((resolve) => {
        const reader = new window.FileReader();
        reader.readAsDataURL(response.data);
        reader.onload = () => resolve(reader.result as string);
      });
    })
    .catch((error) => {
      console.log(error);
      throw error;
    });
}

/**
 * Loads data URLs for an array of image metadata
 * @param jsonImages Array of image metadata without data URLs
 * @returns Array of complete images with data URLs
 */
async function loadImageDataUrls(jsonImages: ImageGallery[]): Promise<ImageGallery[]> {
  const imageDataUrlArray: ImageGallery[] = [];

  for (const image of jsonImages) {
    try {
      const dataUrl = await loadImageData(image.id);
      imageDataUrlArray.push({
        id: image.id,
        name: image.name,
        type: image.type,
        size: image.size,
        similarity: image.similarity,
        dataUrl: dataUrl,
      });
    } catch (error) {
      console.error(`Failed to load image data for ID ${image.id}:`, error);
    }
  }

  return imageDataUrlArray;
}

export async function loadAllImages(): Promise<ImageGallery[]> {
  const json = await getImagesAsJSON();
  const completeImages = await loadImageDataUrls(json);
  images.value = completeImages;
  return completeImages;
}

export async function getSimilarImages(
  id: number,
  count: number = 5,
  descriptor: string = 'rgbcube'
): Promise<ImageGallery[]> {
  const json = await getSimilarImagesAsJSON(id, count, descriptor);
  return await loadImageDataUrls(json);
}

export async function uploadImage(file: File): Promise<boolean> {
  const formData = new FormData();
  formData.append('file', file);

  try {
    //   const fileNameExists = images.value.some(img => img.name === file.name);
    //   if (fileNameExists) {
    //     throw new Error("Une image avec ce nom existe déjà. Veuillez renommer votre fichier.");
    //   }
    await axios.post('/images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    await loadAllImages();
    return true;
  } catch (error) {
    console.error('Upload failed:', error);
    throw error;
  }
}

export async function deleteImage(imageID: number): Promise<boolean> {
  try {
    await axios.delete(`/images/${imageID}`);

    images.value = images.value.filter((img) => img.id !== imageID);
    return true;
  } catch (error) {
    console.error(`Delete failed for image ${imageID}:`, error);
    return false;
  }
}

export async function refreshImages(): Promise<void> {
  try {
    const updatedImages = await loadAllImages();
    images.value = updatedImages;
  } catch (error) {
    console.error('Failed to refresh images:', error);
  }
}

export async function getImageFilter(id: number, filter: string) {
  filter = id + filter;
  return axios
    .get(`/images/${id}`, { responseType: 'blob' })
    .then(function (response: AxiosResponse) {
      return new Promise<string>((resolve) => {
        const reader = new window.FileReader();
        reader.readAsDataURL(response.data);
        reader.onload = () => resolve(reader.result as string);
      });
    })
}