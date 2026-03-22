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

const localeCookie = document.cookie
    .split(';')
    .map((cookie) => cookie.trim())
    .find((cookie) => cookie.startsWith('locale='))
    ?.split('=')[1];

const locale = ['EN', 'FR', 'AR'].includes(localeCookie || '') ? localeCookie : 'EN';

const safeMessageCompiler = (message: unknown) => {
    return (ctx: any) => {
        if (typeof message !== 'string') {
            return '';
        }

        return message.replace(/\{(\w+)\}/g, (_match, key) => {
            const value = ctx?.named?.(key);
            return value == null ? `{${key}}` : String(value);
        });
    };
};

// createApp(App).use(router).mount('#app')

const i18n = createI18n({
    allowComposition: true,
    locale,
    fallbackLocale: 'EN',
    messageCompiler: safeMessageCompiler,
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
