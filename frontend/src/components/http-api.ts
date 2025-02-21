import axios, { type AxiosResponse } from 'axios';

export interface Images {
    id: number;
    name: string;
    img_in_url: string | null; // all the data of an image stored as bytes64 string

}

export async function getImages() {
    let images: Images[] = [];
    await axios.get("/images")
        .then((json_file) => images = json_file.data)
        .catch((error) => console.error(error));
    for (var i = 0; i < images.length; i = i + 1) {
        images[i].img_in_url = ""
    }
    return images;
}

export async function load_all_imageData(imgs: Images[], n: number) {
    for (let i = 0; i < n; i = i + 1) {
        if (!imgs[i].img_in_url) {
            axios.get(`/images/${i}`, { responseType: "blob" })
                .then(function (bytes_img: AxiosResponse) {
                    const reader = new window.FileReader();
                    reader.onload = () => {
                        imgs[i].img_in_url = reader.result as string;
                    };
                    reader.readAsDataURL(bytes_img.data); // convert all the pixel of the img into a bse64 string that can be interpret by the navigator
                }).catch(error => {
                    console.log(error);
                })
        }
    }
}

export async function load_json() {
    let img:any = [];
    axios.get('/images')
        .then(function (response:AxiosResponse) {
            img = response.data
        }).catch(function (error) {
            console.log(error)
        });
    return img;
}