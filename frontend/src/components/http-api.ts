import axios, { type AxiosResponse } from 'axios';

export interface ImageGallery {
    id: number;
    name: string;
    dataUrl: string | null;
    
}

export async function getImagesAsJSON() {
    let json: ImageGallery[] = []
    await axios.get("/images")
        // Writes the id and name attributes but not dataUrl
        .then((response) => json = response.data)
        .catch((error) => console.error(error));
    return json;
}

export async function loadImageData(imageID: number): Promise<string> {
    return axios.get(`/images/${imageID}`, { responseType: "blob" })
        .then(function (response: AxiosResponse) {
            return new Promise<string>((resolve) => {
                const reader = new window.FileReader();
                reader.readAsDataURL(response.data);
                reader.onload = () => resolve(reader.result as string);
            });
        }).catch(error => {
            console.log(error);
            throw error;
        });
}



export async function loadAllImages(): Promise<ImageGallery[]> {
    const json = await getImagesAsJSON();
    const imageDataUrlArray: ImageGallery[] = [];
    
    for (let i = 0; i < json.length; i++) {
        const dataUrl = await loadImageData(i);
        imageDataUrlArray.push({
            id: json[i].id,
            name: json[i].name,
            dataUrl: dataUrl
        });
    }
    return imageDataUrlArray;
}