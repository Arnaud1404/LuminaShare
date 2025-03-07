import { ref } from 'vue';


export interface ImageGallery {
    id: number;
    name: string;
    dataUrl: string | null;
    
}
export const images = ref<ImageGallery[]>([]);