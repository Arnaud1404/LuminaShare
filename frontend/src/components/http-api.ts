import axios, { type AxiosResponse } from 'axios';
import { images, type ImageGallery } from './images';
import { currentUser } from './users';

export async function loginUser(userid: string, password: string): Promise<boolean> {
  try {
    const response = await axios.post('/api/auth/login', { userid, password });
    console.log('Login response:', response.data);
    currentUser.value = response.data;
    localStorage.setItem('user', JSON.stringify(response.data));
    return true;
  } catch (error) {
    console.error('Login failed:', error);
    return false;
  }
}

export async function registerUser(
  userid: string,
  name: string,
  password: string
): Promise<boolean> {
  try {
    await axios.post('/api/auth/register', {
      userid,
      name,
      password,
    });
    return true;
  } catch (error) {
    console.error('Registration failed:', error);
    return false;
  }
}

export function logoutUser(): void {
  currentUser.value = null;
  localStorage.removeItem('user');
}


export async function toggleImagePrivacy(imageId: number): Promise<void> {
  const response = await fetch(`/images/${imageId}/privacy`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json'
    }
  });
  
  if (!response.ok) {
    throw new Error('Failed to toggle image privacy');
  }
}
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
        ...image,
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

/**
 * Likes an image, each user can only like an image once
 * @param imageId The ID of the image to like
 * @returns The new like count and like status
 */
export async function toggleLike(imageId: number): Promise<{likes: number, isLiked: boolean}> {
  try {
    if (!currentUser.value?.userid) {
      throw new Error('User must be logged in to like images');
    }
    
    const response = await axios.post(
      `/images/${imageId}/toggle-like?userid=${currentUser.value.userid}`
    );
    
    const image = images.value.find((img) => img.id === imageId);
    if (image && response.data) {
      image.likes = response.data.likes;
      image.isLiked = response.data.isLiked;
    }
    
    return response.data;
  } catch (error) {
    console.error(`Failed to toggle like for image ${imageId}:`, error);
    throw error;
  }
}

export async function checkLikeStatus(imageId: number): Promise<boolean> {
  try {
    if (!currentUser.value?.userid) {
      return false;
    }
    
    const response = await axios.get(
      `/images/${imageId}/like-status?userid=${currentUser.value.userid}`
    );
    
    return response.data.isLiked;
  } catch (error) {
    console.error(`Failed to check like status for image ${imageId}:`, error);
    return false;
  }
}

/**
 * Unlikes an image
 * @param imageId The ID of the image to unlike
 * @returns The new like count, or -1 if failed
 */
export async function unlikeImage(imageId: number): Promise<number> {
  try {
    const response = await axios.post(`/images/${imageId}/unlike`);

    const image = images.value.find((img) => img.id === imageId);
    if (image && response.data && typeof response.data.likes === 'number') {
      image.likes = response.data.likes;
    }

    return response.data.likes;
  } catch (error) {
    console.error(`Failed to unlike image ${imageId}:`, error);
    return -1;
  }
}

export async function setImageLikes(imageId: number, likes: number): Promise<boolean> {
  try {
    const response = await axios.put(`/images/${imageId}/set-likes?likes=${likes}`);
    
    const image = images.value.find((img) => img.id === imageId);
    if (image && response.data) {
      image.likes = response.data.likes;
    }
    
    return true;
  } catch (error) {
    console.error(`Failed to set like count for image ${imageId}:`, error);
    return false;
  }
}

/**
 * Gets all images for a specific user
 * @param userid The user ID to get images for
 * @param includePrivate Whether to include private images
 * @returns Array of images with data URLs
 */
export async function getUserImages(
  userid: string,
  includePrivate: boolean = false
): Promise<ImageGallery[]> {
  try {
    const currentUserid = currentUser.value?.userid || '';

    const response = await axios.get(
      `/images/user/${userid}?includePrivate=${includePrivate}&currentUserid=${currentUserid}`
    );
    return await loadImageDataUrls(response.data);
  } catch (error) {
    console.error(`Failed to get user images for ${userid}:`, error);
    return [];
  }
}

export async function getUserProfile(userid: string): Promise<any> {
  try {
    const response = await axios.get(`/users/${userid}`);
    return response.data;
  } catch (error) {
    console.error(`Failed to get user profile for ${userid}:`, error);
    return null;
  }
}

export async function uploadImage(file: File, isPublic: boolean = false): Promise<boolean> {
  const formData = new FormData();
  formData.append('file', file);
  if (currentUser.value) {
    formData.append('userid', currentUser.value.userid);
    formData.append('ispublic', isPublic.toString());
  }
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

export async function getImageFilter(id: number, filter: string, number: number) {
  return axios

    .get(`/images/${id}/filter?filter=${filter}&number=${number}`, { responseType: 'blob' })
    .then(function (response: AxiosResponse) {
      return new Promise<string>((resolve) => {
        const reader = new window.FileReader();
        reader.readAsDataURL(response.data);
        reader.onload = () => resolve(reader.result as string);
      });
    });
}
