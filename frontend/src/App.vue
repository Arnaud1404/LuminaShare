<script setup lang="ts">
import Header from './components/Header.vue';
import { onMounted } from 'vue';

onMounted( () => {
  document.cookie = 'locale=EN'
})

</script>

<template>
  <div class="app-container">
    <Header />
    <main class="main-content">
      <RouterView />
    </main>
  </div>
</template>

<style>
.app-container {
  min-height: 100vh;
  background-color: var(--background_color);
}

.main-content {
  padding: 20px;
  margin-top: 60px;
}
</style>
