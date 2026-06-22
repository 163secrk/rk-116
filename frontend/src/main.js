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
  NModal,
  NCard,
  NGrid,
  NGi,
  NEmpty,
  NSpin,
  NMenu,
  NDivider,
  NTag,
  NDataTable,
  NRate,
  NUpload
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
    NModal,
    NCard,
    NGrid,
    NGi,
    NEmpty,
    NSpin,
    NMenu,
    NDivider,
    NTag,
    NDataTable,
    NRate,
    NUpload
  ]
})

const app = createApp(App)
app.use(naive)
app.mount('#app')
