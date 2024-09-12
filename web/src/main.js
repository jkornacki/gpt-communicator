import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import './index.css'
import router from './router'

import showdown from 'showdown';
import hljs from 'highlight.js';
import 'highlight.js/styles/default.css';

const app = createApp(App)

app.config.globalProperties.$showdown = showdown;
app.config.globalProperties.$hljs = hljs;

app.use(createPinia())
app.use(router)

app.mount('#app')
