import { ref } from 'vue';
/**
 * Image interface
 *
 * @export
 * @interface ImageGallery
 */
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
/** Images for the user, loaded by loadAllImages*/
export const images = ref<ImageGallery[]>([]);
