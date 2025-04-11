import { ref } from 'vue';

export interface ImageGallery {
  id: number;
  name: string;
  type: string;
  size: string;
  similarity?: number;
  dataUrl?: string;
  userid?: string;
  ispublic?: boolean;
  likes?: number;
}
export const images = ref<ImageGallery[]>([]);
