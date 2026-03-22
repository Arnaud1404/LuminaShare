<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { registerUser } from './http-api';

const router = useRouter();
const userid = ref('');
const name = ref('');
const password = ref('');
const isLoading = ref(false);
const errorMessage = ref('');

async function handleRegister() {
  if (!userid.value || !name.value || !password.value) {
    errorMessage.value = 'Tous les champs sont obligatoires';
    return;
  }

  isLoading.value = true;
  errorMessage.value = '';

  try {
    const success = await registerUser(userid.value, name.value, password.value);

    if (success) {
      router.push('/login?registered=true');
    } else {
      errorMessage.value = 'Cet ID utilisateur existe déjà';
    }
  } catch (error) {
    errorMessage.value = 'Une erreur est survenue';
    console.error(error);
  } finally {
    isLoading.value = false;
  }
}
</script>

<template>
  <div class="register-container panel">
    <h1>
      {{ $t('header.register') }}
    </h1>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <form @submit.prevent="handleRegister">
      <div class="form-group">
        <label for="userid">
          {{ $t('login.id') }}
        </label>
        <input
          type="text"
          id="userid"
          v-model="userid"
          required
          :placeholder="$t('register.choose_userid')"
        />
      </div>

      <div class="form-group">
        <label for="name">
          {{ $t('register.name') }}
        </label>
        <input type="text" id="name" v-model="name" required :placeholder="$t('register.name')" />
      </div>

      <div class="form-group">
        <label for="password">
          {{ $t('login.mdp') }}
        </label>
        <input
          type="password"
          id="password"
          v-model="password"
          required
          :placeholder="$t('login.enter_mdp')"
        />
      </div>

      <div class="form-actions">
        <button type="submit" :disabled="isLoading">
          {{ isLoading ? 'Inscription...' : "S'inscrire" }}
        </button>
      </div>

      <div class="login-link">
        {{ $t('register.exist') }}
        <RouterLink :to="{ name: 'login' }">{{ $t('login.login_now') }}</RouterLink>
      </div>
    </form>
  </div>
</template>

<style scoped>
.register-container {
  max-width: 400px;
  margin: 2rem auto;
  padding: 2rem;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
}

input {
  width: 100%;
  padding: 0.5rem;
  border-radius: 4px;
  border: 1px solid #ccc;
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.form-actions {
  margin-top: 1.5rem;
}

button {
  width: 100%;
  padding: 0.5rem;
  background-color: var(--color_button);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:disabled {
  opacity: 0.7;
}

.error-message {
  background-color: rgba(255, 0, 0, 0.2);
  color: #ff3333;
  padding: 0.5rem;
  margin-bottom: 1rem;
  border-radius: 4px;
}

.login-link {
  margin-top: 1rem;
  text-align: center;
}
</style>
