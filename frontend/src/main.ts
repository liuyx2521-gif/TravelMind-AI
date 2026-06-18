import '@unocss/reset/tailwind.css'
import 'uno.css'
import './styles.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import naive from 'naive-ui'
import App from './App.vue'
import { router } from './router'

createApp(App).use(createPinia()).use(router).use(naive).mount('#app')
