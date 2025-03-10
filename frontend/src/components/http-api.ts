import axios, { type AxiosResponse } from "axios";
import { images, type ImageGallery } from "./images";

export async function getImagesAsJSON() {
  let json: ImageGallery[] = [];
  await axios
    .get("/images")
    // Writes the id and name attributes but not dataUrl
    .then((response) => (json = response.data))
    .catch((error) => console.error(error));
  return json;
}

export async function loadImageData(imageID: number): Promise<string> {
  return axios
    .get(`/images/${imageID}`, { responseType: "blob" })
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

export async function loadAllImages(): Promise<ImageGallery[]> {
  const json = await getImagesAsJSON();
  const imageDataUrlArray: ImageGallery[] = [];

  for (const image of json) {
    const dataUrl = await loadImageData(image.id);
    imageDataUrlArray.push({
      id: image.id,
      name: image.name,
      type: image.type,
      size: image.size,
      description: image.description,
      dataUrl: dataUrl,
    });
  }
  images.value = imageDataUrlArray;
  return imageDataUrlArray;
}

export async function uploadImage(file: File): Promise<boolean> {
  const formData = new FormData();
  formData.append("file", file);

  try {
  //   const fileNameExists = images.value.some(img => img.name === file.name);
  //   if (fileNameExists) {
  //     throw new Error("Une image avec ce nom existe déjà. Veuillez renommer votre fichier.");
  //   }
    await axios.post("/images", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    await loadAllImages();
    return true;
  } catch (error) {
    console.error("Upload failed:", error);
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
    console.error("Failed to refresh images:", error);
  }
}
