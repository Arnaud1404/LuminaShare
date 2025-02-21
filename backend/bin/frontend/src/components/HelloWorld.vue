<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios';

defineProps<{ msg: string }>()

/* const imgs = axios.get('/images').then(function (reponse){
  console.log(reponse.data);
}).catch(function (error){
  console.log(error);
}).finally(function(){

}); */

var imgs = axios({
  method: 'get',
  url: 'http://localhost:8181/images',
  responseType: 'stream'
})
  .then(function (response) {
    response.data
  });

/* const imgs = axios.get('http://localhost:8181').then(function (response) {
    console.log(response.data);
  })
  .catch(function (error) {
    console.error(error);
  });
  */

const count = ref(0)
</script>

<template>
  <h1>{{ msg }}</h1>

  <div class="card">
    <button type="button" @click="count++">count is {{ count }}</button>
    <p>
      Edit
      <code>components/HelloWorld.vue</code> to test HMR
    </p>
  </div>
  <p>
    here is the list of the images currently stored in the backend server : {{ imgs }}
  </p>
  <p>
    Check out
    <a href="https://vuejs.org/guide/quick-start.html#local" target="_blank"
      >create-vue</a
    >, the official Vue + Vite starter
  </p>
  <p>
    Learn more about IDE Support for Vue in the
    <a
      href="https://vuejs.org/guide/scaling-up/tooling.html#ide-support"
      target="_blank"
      >Vue Docs Scaling up Guide</a
    >.
  </p>
  <p class="read-the-docs">Click on the Vite and Vue logos to learn more</p>
</template>

<style scoped>
.read-the-docs {
  color: #888;
}
</style>
