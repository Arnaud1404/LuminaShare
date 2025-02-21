<script setup lang="ts">

import type {Images} from './http-api';


defineProps<{
   images:Images[]
}>()


</script>

<template>
<div v-for=" img in images" class="img_container" >
  <img v-if="img.img_in_url" :src = "img.img_in_url" >
  <p class="description_image"> {{img.name}}</p>
</div>
</template>

<style>

.description_image {

  height: 100px;
  line-height: 100px;
  text-align: center;
  border: 1px dashed #5c61af;

}

.img_container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
  padding: 1rem;
}

.img_container img {
  max-width: 100%;
  max-height: auto;
  object-fit: cover;
  border-radius: 4px;
}

.img_container p {
  margin-top: 0.5rem;
}
</style>