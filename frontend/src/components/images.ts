import { ref } from 'vue';

export interface ImageGallery {
  id: number;
  name: string;
  type: string;
  size: string;
  url: string;
  dataUrl?: string;
  rgbcube?: number[];
  huesat?: number[];
  similarity?: number;
  userid?: string;
  ispublic: boolean;
  likes: number;
  isLiked?: boolean;
}

export const images = ref<ImageGallery[]>([]);
