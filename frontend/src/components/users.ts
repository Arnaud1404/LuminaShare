import { ref } from 'vue';

export interface User {
  userid: string;
  name: string;
  bio: string;
}

export const currentUser = ref<User | null>(null);

const storedUser = localStorage.getItem('user');
if (storedUser) {
  try {
    currentUser.value = JSON.parse(storedUser);
  } catch (e) {
    localStorage.removeItem('user');
  }
}

export function isLoggedIn(): boolean {
  return !!currentUser.value;
}
