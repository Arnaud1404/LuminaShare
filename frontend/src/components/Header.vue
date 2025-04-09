<script setup lang="ts">
import { ref } from 'vue';

const isSidebarOpen = ref(false);

function toggleSidebar() {
  isSidebarOpen.value = !isSidebarOpen.value;
}
</script>

<template>
  <header class="app-header">
    <button class="toggle-sidebar" @click="toggleSidebar">
      <span class="burger-icon"></span>
      <span class="burger-icon"></span>
      <span class="burger-icon"></span>
    </button>

    <h1>LuminaShare - Partage de Photos</h1>
  </header>

  <div class="sidebar-backdrop" v-if="isSidebarOpen" @click="toggleSidebar"></div>

  <nav class="sidebar" :class="{ open: isSidebarOpen }">
    <div class="sidebar-header">
      <div class="logo">
        <h2>LuminaShare</h2>
      </div>
      <button class="close-button" @click="toggleSidebar">X</button>
    </div>

    <div class="main-links">
      <RouterLink :to="{ name: 'home' }" class="nav-link" @click="toggleSidebar"
        >Home Page</RouterLink
      >
      <RouterLink :to="{ name: 'user' }" class="nav-link" @click="toggleSidebar"
        >Utilisateur</RouterLink
      >
      <RouterLink :to="{ name: 'users' }" class="nav-link" @click="toggleSidebar"
        >Les autres</RouterLink
      >
      <RouterLink :to="{ name: 'modif' }" class="nav-link" @click="toggleSidebar"
        >Modifier</RouterLink
      >
    </div>

    <div class="auth-links">
      <RouterLink :to="{ name: 'login' }" class="nav-link" @click="toggleSidebar"
        >Connexion</RouterLink
      >
      <RouterLink :to="{ name: 'register' }" class="nav-link" @click="toggleSidebar"
        >S'inscrire</RouterLink
      >
    </div>
  </nav>
</template>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 60px;
  background-color: #222;
  display: flex;
  align-items: center;
  padding: 0 20px;
  z-index: 90;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.app-header h1 {
  color: white;
  margin: 0;
  font-size: 1.5rem;
  width: 100%;
  text-align: center;
}

.sidebar {
  width: 250px;
  height: 100%;
  position: fixed;
  top: 0;
  left: -250px;
  background-color: #333;
  overflow-y: auto;
  z-index: 100;
  transition: left 0.3s;
  padding: 20px 0;
}

.sidebar.open {
  left: 0;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  padding: 0 20px;
  margin-bottom: 20px;
}

.logo h2 {
  color: white;
  margin: 0;
}

.nav-link {
  display: block;
  padding: 10px 20px;
  color: white;
  text-decoration: none;
  border-bottom: 1px solid #444;
}

.nav-link:hover {
  background-color: #444;
}

.auth-links {
  margin-top: 30px;
  border-top: 1px solid #444;
  padding-top: 10px;
}

.toggle-sidebar {
  width: 40px;
  height: 40px;
  background-color: #333;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  margin-right: 10px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 5px;
}

.burger-icon,
.burger-icon::before,
.burger-icon::after {
  content: '';
  display: block;
  width: 22px;
  height: 3px;
  background-color: white;
  border-radius: 2px;
}

.burger-icon {
  background-color: white;
}

.close-button {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
}

.sidebar-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 99;
}

.main-links {
  margin-bottom: 20px;
}
</style>
