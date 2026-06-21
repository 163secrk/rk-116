import { createApp } from 'vue'
import {
  create,
  NConfigProvider,
  NMessageProvider,
  NDialogProvider,
  NGlobalStyle,
  NButton,
  NSpace,
  NInput,
  NIcon,
  NForm,
  NFormItem,
  NSelect,
  NRadioGroup,
  NRadio,
  NCheckboxGroup,
  NCheckbox,
  NSwitch,
  NDatePicker,
  NTimePicker,
  NModal
} from 'naive-ui'
import App from './App.vue'

const naive = create({
  components: [
    NConfigProvider,
    NMessageProvider,
    NDialogProvider,
    NGlobalStyle,
    NButton,
    NSpace,
    NInput,
    NIcon,
    NForm,
    NFormItem,
    NSelect,
    NRadioGroup,
    NRadio,
    NCheckboxGroup,
    NCheckbox,
    NSwitch,
    NDatePicker,
    NTimePicker,
    NModal
  ]
})

const app = createApp(App)
app.use(naive)
app.mount('#app')
