import { ref } from 'vue';
/**
 * User interface
 *
 * @export
 * @interface User
 */
export interface User {
  userid: string;
  name: string;
  bio: string;
}
/** The current logged in user */
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
