import { createApp } from 'vue';
import './style.css';
import App from './App.vue';
import router from './router';
import { createI18n } from 'vue-i18n';
import EN from './locale/en.json'
import FR from './locale/fr.json'
import AR from './locale/ar.json'

if (!document.cookie.includes('locale')) {
    document.cookie = 'locale=EN';
}

// createApp(App).use(router).mount('#app')

const i18n = createI18n({
    locale: document.cookie.split('=')[1] || 'EN',
    messages: {
        EN: EN,
        FR: FR,
        AR: AR,
    }
})

const app = createApp(App);
app.use(router);
app.use(i18n);
app.mount('#app');
