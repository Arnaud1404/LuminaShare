import { ref } from 'vue';

export interface ImageGallery {
  id: number;
  name: string;
  type: string;
  size: string;
  dataUrl: string | null;
  similarity?: number;
}
export const images = ref<ImageGallery[]>([]);
