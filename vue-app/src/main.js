import { createApp } from 'vue'
import App from './App.vue'
import store from './store'
import Toast from "vue-toastification"
import "vue-toastification/dist/index.css"

const app = createApp(App)

const options = {
  timeout: 3000 // Set default timeout to 3 seconds
}

app.use(store)
app.use(Toast, options)
app.mount('#app')
