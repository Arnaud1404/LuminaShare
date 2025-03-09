import { ref } from "vue";

export interface ImageGallery {
  id: number;
  name: string;
  type: string;
  size: string;
  description: string;
  dataUrl: string | null;
}
export const images = ref<ImageGallery[]>([]);
