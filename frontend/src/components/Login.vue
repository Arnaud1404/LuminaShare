<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { loginUser } from './http-api';

const router = useRouter();
const userid = ref('');
const password = ref('');
const isLoading = ref(false);
const errorMessage = ref('');

async function handleLogin() {
  if (!userid.value || !password.value) {
    errorMessage.value = 'Veuillez entrer un ID et un mot de passe';
    return;
  }

  isLoading.value = true;
  errorMessage.value = '';

  try {
    console.log('Attempting login with:', userid.value);
    const success = await loginUser(userid.value, password.value);

    if (success) {
      console.log('Login successful, redirecting...');
      router.push('/');
    } else {
      console.error('Login failed');
      errorMessage.value = 'ID utilisateur ou mot de passe incorrect';
    }
  } catch (error) {
    console.error('Login error:', error);
    errorMessage.value = 'Une erreur est survenue lors de la connexion';
  } finally {
    isLoading.value = false;
  }
}
</script>

<template>
  <div class="login-container panel">
    <h1>
      {{ $t('header.login') }}
    </h1>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <form @submit.prevent="handleLogin">
      <div class="form-group">
        <label for="userid"> {{ $t('login.id') }} </label>
        <input type="text" id="userid" v-model="userid" required placeholder="Entrez votre ID" />
      </div>

      <div class="form-group">
        <label for="password">{{ $t('login.mdp') }}</label>
        <input
          type="password"
          id="password"
          v-model="password"
          required
          placeholder="Entrez votre mot de passe"
        />
      </div>

      <div class="form-actions">
        <button type="submit" :disabled="isLoading">
          {{ isLoading ? 'Connexion...' : 'Se connecter' }}
        </button>
      </div>

      <div class="register-link">
        {{ $t('login.no-account') }}
        <RouterLink :to="{ name: 'register' }">{{ $t('header.register') }}</RouterLink>
      </div>
    </form>
  </div>
</template>

<style scoped>
.login-container {
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

.register-link {
  margin-top: 1rem;
  text-align: center;
}
</style>
