<script setup lang="ts">

import { ref } from 'vue'
import axios, { type AxiosResponse } from 'axios';
import Gallery from "./Gallery.vue" //regarder props
import { getImages,load_all_imageData } from './http-api';
import type {Images} from './http-api';

var file = ref();
var Images = ref<Images[]>([])
var imgs = ref(); //images en json

async function json(){
axios.get('/images')
        .then(function (response:AxiosResponse) {
            imgs.value = response.data
        }).catch(function (error:any) {
            console.log(error)
        });
      }

function handleFileUpload( event:any ){
  file.value = event.target.files[0];
}

async function submitFile(){
  let fromData = new FormData();
  fromData.append('file',file.value);

  axios.post('/images',
              fromData,
              {
                headers: {
                  'Content-Type': 'multipart/from-data'
                }
              }
  ).then(function(){
      getImages().
      then(response => {
        Images.value = response;
        load_all_imageData( Images.value, Images.value.length).
        then(() =>
        {
          json().then(response => {
          imgs.value = response;
          })
        })
        .catch(error => console.log(`ERROR: ${error}`));
      })
      .catch(error => console.log(`ERROR: ${error}`));
    })
  .catch(error => console.log(`ERROR: ${error}`));

}


json().then(response => {
  imgs.value = response;
})


getImages().then(response => {
  Images.value = response;
  load_all_imageData( Images.value, Images.value.length)
})
.catch(error => console.log(`ERROR: ${error}`));


var img_src = ref("")
var selected_image = ref()

async function change_img( selected_image:number) {
axios.get("/images/" + selected_image, { responseType:"blob" })
    .then(function (img_bytes: AxiosResponse) {
	const reader = new window.FileReader();
	reader.readAsDataURL(img_bytes.data);
	reader.onload = function() {
	    const imageDataUrl = (reader.result as string);
	    img_src.value = imageDataUrl;
	}
});
}



</script>

<template>
  <h1> Projet </h1>

  <div class="container">
    <div>
      <hr/>
      <label>File
        <input type="file" @change="handleFileUpload( $event )"/>
      </label>
      <br>
      <button v-on:click="submitFile()">Submit</button>
    </div>
  </div>


  <p>
     the list of images in json format is : {{ imgs }}
  </p>

  <p>
    here is the list of the images currently stored in the backend server :
    <select v-model="selected_image" @change=" change_img(selected_image)">
      <option v-for="image in imgs" :value="image.id">
        {{image.name}}
      </option>
    </select>
  </p>
  <img :src= "img_src" id="select_file">

  <section id="Gallery">
    <h2> Gallery</h2>
    <Gallery :images="Images"/>

  </section>

</template>

<style scoped>

#select_file{
  max-width: 60%;
  height: auto;
}

</style>
