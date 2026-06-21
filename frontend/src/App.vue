<template>
  <n-config-provider :theme="darkTheme" :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <n-global-style />
        <div class="app-container">
          <header class="top-toolbar">
            <div class="toolbar-left">
              <h1 class="app-title">低代码表单搭建器</h1>
            </div>
            <div class="toolbar-right">
              <n-input
                v-model:value="formName"
                placeholder="表单名称"
                style="width: 240px; margin-right: 16px"
                size="small"
              />
              <n-space>
                <n-button type="primary" size="small" @click="showPreview = true">
                  <template #icon>
                    <n-icon><EyeOutline /></n-icon>
                  </template>
                  预览
                </n-button>
                <n-button type="success" size="small" @click="handleSave">
                  <template #icon>
                    <n-icon><SaveOutline /></n-icon>
                  </template>
                  保存
                </n-button>
              </n-space>
            </div>
          </header>

          <div class="main-content">
            <ComponentPanel @add-component="handleAddComponent" />
            <FormCanvas
              :schema-list="schemaList"
              :selected-id="selectedId"
              @select-component="handleSelect"
              @update-list="handleUpdateList"
              @remove-component="handleRemove"
            />
            <PropertyPanel
              :selected-component="selectedComponent"
              @update-component="handleUpdateComponent"
            />
          </div>

          <PreviewModal
            v-model:show="showPreview"
            :schema-list="schemaList"
            :form-name="formName"
          />
        </div>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { NConfigProvider, NGlobalStyle, NButton, NSpace, NInput, NIcon, NMessageProvider, NDialogProvider, createDiscreteApi } from 'naive-ui'
import { darkTheme } from 'naive-ui'
import { EyeOutline, SaveOutline } from '@vicons/ionicons5'
import axios from 'axios'
import ComponentPanel from './components/ComponentPanel.vue'
import FormCanvas from './components/FormCanvas.vue'
import PropertyPanel from './components/PropertyPanel.vue'
import PreviewModal from './components/PreviewModal.vue'

const { message } = createDiscreteApi(['message'])

const formName = ref('未命名表单')
const schemaList = ref([])
const selectedId = ref(null)
const showPreview = ref(false)

const themeOverrides = {
  common: {
    primaryColor: '#6366f1',
    primaryColorHover: '#818cf8',
    primaryColorPressed: '#4f46e5'
  }
}

const selectedComponent = computed(() => {
  return schemaList.value.find(item => item.id === selectedId.value) || null
})

function handleAddComponent(component) {
  const newItem = {
    id: Date.now() + Math.random().toString(36).substr(2, 9),
    type: component.type,
    label: component.label,
    field: component.field,
    placeholder: component.placeholder || '',
    options: component.options ? JSON.parse(JSON.stringify(component.options)) : [],
    required: false,
    validation: {
      required: false,
      requiredMessage: '该项为必填项',
      minLength: null,
      maxLength: null,
      minLengthMessage: '长度不能小于最小值',
      maxLengthMessage: '长度不能大于最大值',
      pattern: null,
      patternMessage: '格式不正确',
      min: null,
      max: null,
      minMessage: '数值不能小于最小值',
      maxMessage: '数值不能大于最大值'
    }
  }
  schemaList.value.push(newItem)
  selectedId.value = newItem.id
}

function handleSelect(id) {
  selectedId.value = id
}

function handleUpdateList(list) {
  schemaList.value = [...list]
}

function handleRemove(id) {
  schemaList.value = schemaList.value.filter(item => item.id !== id)
  if (selectedId.value === id) {
    selectedId.value = null
  }
}

function handleUpdateComponent(updated) {
  schemaList.value = schemaList.value.map(item =>
    item.id === updated.id ? { ...item, ...updated } : item
  )
}

async function handleSave() {
  if (!formName.value.trim()) {
    message.warning('请输入表单名称')
    return
  }
  try {
    const payload = {
      name: formName.value,
      schemaData: JSON.stringify(schemaList.value)
    }
    const res = await axios.post('/api/forms', payload)
    if (res.data.success) {
      message.success('保存成功')
    } else {
      message.error('保存失败')
    }
  } catch (e) {
    message.error('保存失败：' + e.message)
  }
}

function ensureValidation(item) {
  if (!item.validation) {
    item.validation = {
      required: false,
      requiredMessage: '该项为必填项',
      minLength: null,
      maxLength: null,
      minLengthMessage: '长度不能小于最小值',
      maxLengthMessage: '长度不能大于最大值',
      pattern: null,
      patternMessage: '格式不正确',
      min: null,
      max: null,
      minMessage: '数值不能小于最小值',
      maxMessage: '数值不能大于最大值'
    }
  }
  return item
}

onMounted(async () => {
  try {
    const res = await axios.get('/api/forms')
    if (res.data.success && res.data.data && res.data.data.length > 0) {
      const latest = res.data.data[res.data.data.length - 1]
      formName.value = latest.name
      try {
        const list = JSON.parse(latest.schemaData) || []
        schemaList.value = list.map(item => ensureValidation(item))
      } catch (e) {
        schemaList.value = []
      }
    }
  } catch (e) {
    console.log('加载表单失败')
  }
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  width: 100%;
}

body {
  background: #0f172a;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #0f172a;
}

.top-toolbar {
  height: 56px;
  background: #1e293b;
  border-bottom: 1px solid #334155;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.app-title {
  font-size: 18px;
  font-weight: 600;
  color: #f1f5f9;
  letter-spacing: 0.5px;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}
</style>
