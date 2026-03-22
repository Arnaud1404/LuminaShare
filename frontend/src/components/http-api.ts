import axios, { type AxiosResponse } from 'axios';
import { images, type ImageGallery } from './images';
import { currentUser } from './users';

/**
 * Logs in a user with the backend server
 * Side effects:
 * - Sets the currentUser.value to the user data
 * - Stores user data in localStorage to keep data between page refreshes
 * @category Authentication
 * @param userid - The user's login ID
 * @param password - The user's password
 * @returns Promise that resolves to true if login was successful, false otherwise
 *
 */
export async function loginUser(userid: string, password: string): Promise<boolean> {
  try {
    const response = await axios.post('/api/auth/login', { userid, password });
    currentUser.value = response.data;
    localStorage.setItem('user', JSON.stringify(response.data));
    return true;
  } catch (error) {
    console.error('Login failed:', error);
    return false;
  }
}

/**
 * Registers a user with the backend server
 *
 * @category Authentication
 * @param userid The chosen userid for the new user
 * @param name The display name for the new user
 * @param password The chosen userid for the new user
 * @returns Promise that resolves to true if login was successful, false otherwise
 */
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

/**
 * Logs out the current user and removes cookies
 *
 */
export function logoutUser(): void {
  currentUser.value = null;
  localStorage.removeItem('user');
}

/**
 * Toggles the privacy field of a given imageId.
 * @requires Logged in
 * @param imageId
 *
 * @returns a void Promise
 */
export async function toggleImagePrivacy(imageId: number): Promise<void> {
  try {
    const response = await axios.patch(
      `/images/${imageId}/privacy`,
      {},
      {
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );

    const image = images.value.find((img) => img.id === imageId);
    if (image && response.data) {
      image.ispublic = response.data.ispublic;
    }

    return response.data;
  } catch (error) {
    console.error(`Failed to toggle privacy for image ${imageId}:`, error);
    throw error;
  }
}
/**
 * Gets all similar images as a JSON WITHOUT the dataUrls (the body of the image)
 * @param {number} id
 * @param {number} [count=5]
 * @param {string} [descriptor='rgbcube']
 * @return {*}  {Promise<ImageGallery[]>}
 */
async function getSimilarImagesAsJSON(
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

/**
 * Retrieves the dataUrl from the backend via its image id
 *
 * @export
 * @param {number} imageID
 * @return {*}  {Promise<string>} String representing the image pixels
 */
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
 * Loads data URLs for an array of image JSONs
 * @param jsonImages Array of image JSONs without data URLs
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

/**
 * Loads all images of a user, if the user is not logged in, the array is empty
 *
 * @export
 * @return {*}  {Promise<ImageGallery[]>}
 */
export async function loadAllImages(): Promise<ImageGallery[]> {
  if (currentUser.value) {
    images.value = await getUserImages(currentUser.value.userid, true);
    return getUserImages(currentUser.value.userid, true);
  } else {
    return [];
  }
}
/**
 * Retrieves the most similar images to the given one using L2 Distance
 *
 * @export
 * @param {number} id the id of the input image
 * @param {number} [count=5] the number of similar images to be returned
 * @param {string} [descriptor='rgbcube'] the descriptor to use (rgbcube or huesat)
 * @return {*}  A list of Similar images
 */
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
export async function toggleLike(imageId: number): Promise<{ likes: number; isLiked: boolean }> {
  try {
    if (!currentUser.value?.userid) {
      throw new Error('User must be logged in to like images');
    }

    const response = await axios.put(
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

/**
 * Loads all public images from all users
 * @returns Array of public images with data URLs
 */
export async function loadAllPublicImages(): Promise<ImageGallery[]> {
  try {
    const response = await axios.get('/images');
    const publicImages = response.data.filter((img: any) => img.ispublic === true);
    return await loadImageDataUrls(publicImages);
  } catch (error) {
    console.error('Failed to load public images:', error);
    return [];
  }
}

/**
 * Checks if an image has already been liked by the current user, useful on page reloads
 *
 * @export
 * @param {number} imageId
 * @return {*}  {Promise<boolean>}
 */
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
/**
 * Used for debugging, sets the like count of the given image
 *
 * @export
 * @param {number} imageId
 * @param {number} likes
 * @return {*}  {Promise<boolean>}
 */
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
/**
 * Returns the user data (userid, name, bio)
 *
 * @export
 * @param {string} userid
 * @return {*}  {Promise<any>}
 */
export async function getUserProfile(userid: string): Promise<any> {
  try {
    const response = await axios.get(`/users/${userid}`);
    return response.data;
  } catch (error) {
    console.error(`Failed to get user profile for ${userid}:`, error);
    return null;
  }
}
/**
 * Uploads an image with the userid and ispublic tags set to the currentuser and private by default respectively
 *
 * @export
 * @param {File} file
 * @param {boolean} [isPublic=false]
 * @return {*}  {Promise<boolean>}
 */
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
    return true;
  } catch (error) {
    console.error('Upload failed:', error);
    throw error;
  }
}
/**
 * Deletes an image from the backend
 *
 * @export
 * @param {number} imageID
 * @return {*}  {Promise<boolean>}
 */
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
/**
 *Refreshes the image list
 *
 * @export
 * @return {*}  {Promise<void>}
 */
export async function refreshImages(): Promise<void> {
  try {
    const updatedImages = await loadAllImages();
    images.value = updatedImages;
  } catch (error) {
    console.error('Failed to refresh images:', error);
  }
}
/**
 * Returns the dataUrl after the filter has been applied
 *
 * @export
 * @param {number} id
 * @param {string} filter
 * @param {number} number
 * @param {number} [height]
 * @return {*}
 */
export async function getImageFilter(id: number, filter: string, number: number, height?: number) {
  const url = height
    ? `/images/${id}/filter?filter=${filter}&number=${number}&height=${height}`
    : `/images/${id}/filter?filter=${filter}&number=${number}`;
  return axios.get(url, { responseType: 'blob' }).then(function (response: AxiosResponse) {
    return new Promise<string>((resolve) => {
      const reader = new window.FileReader();
      reader.readAsDataURL(response.data);
      reader.onload = () => resolve(reader.result as string);
    });
  });
}
