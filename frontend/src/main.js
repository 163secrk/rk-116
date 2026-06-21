import { createApp } from 'vue'
import { create, darkTheme } from 'naive-ui'
import App from './App.vue'

const naive = create()

const app = createApp(App)
app.use(naive)
app.mount('#app')
