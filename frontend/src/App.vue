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
            <div v-if="currentPage === 'builder'" class="toolbar-right">
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
                <n-button size="small" @click="handleSaveAs">
                  <template #icon>
                    <n-icon><DuplicateOutline /></n-icon>
                  </template>
                  另存为
                </n-button>
              </n-space>
            </div>
          </header>

          <div class="main-content">
            <aside class="side-menu">
              <div
                :class="['menu-item', { active: currentPage === 'builder' }]"
                @click="switchToBuilder"
              >
                <n-icon size="20"><BuildOutline /></n-icon>
                <span>表单搭建</span>
              </div>
              <div
                :class="['menu-item', { active: currentPage === 'templates' }]"
                @click="switchToTemplates"
              >
                <n-icon size="20"><DocumentsOutline /></n-icon>
                <span>我的模板</span>
              </div>
            </aside>

            <div v-if="currentPage === 'builder'" class="builder-content">
              <ComponentPanel @add-component="handleAddComponent" />
              <FormCanvas
                :schema-list="schemaList"
                :selected-id="selectedId"
                @select-component="handleSelect"
                @add-component="handleAddComponent"
                @remove-component="handleRemove"
                @add-to-container="handleAddToContainer"
              />
              <PropertyPanel
                :selected-component="selectedComponent"
                @update-component="handleUpdateComponent"
              />
            </div>

            <TemplateManager
              v-if="currentPage === 'templates'"
              ref="templateManagerRef"
              :current-schema-list="schemaList"
              :current-form-name="formName"
              @edit-template="handleEditTemplate"
              @create-new="handleCreateNewTemplate"
            />
          </div>

          <PreviewModal
            v-model:show="showPreview"
            :schema-list="schemaList"
            :form-name="formName"
          />

          <n-modal
            v-model:show="showSaveAsModal"
            preset="dialog"
            title="另存为模板"
            positive-text="确定"
            negative-text="取消"
            @positive-click="handleSaveAsConfirm"
            @negative-click="showSaveAsModal = false"
          >
            <div style="margin-bottom: 12px">请输入新模板名称：</div>
            <n-input
              v-model:value="newTemplateName"
              placeholder="模板名称"
              @keyup.enter="handleSaveAsConfirm"
            />
          </n-modal>
        </div>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  NConfigProvider, NGlobalStyle, NButton, NSpace, NInput, NIcon,
  NMessageProvider, NDialogProvider, NModal, createDiscreteApi
} from 'naive-ui'
import { darkTheme } from 'naive-ui'
import {
  EyeOutline, SaveOutline, DuplicateOutline, BuildOutline, DocumentsOutline
} from '@vicons/ionicons5'
import axios from 'axios'
import ComponentPanel from './components/ComponentPanel.vue'
import FormCanvas from './components/FormCanvas.vue'
import PropertyPanel from './components/PropertyPanel.vue'
import PreviewModal from './components/PreviewModal.vue'
import TemplateManager from './components/TemplateManager.vue'

const { message } = createDiscreteApi(['message'])

const currentPage = ref('builder')
const templateManagerRef = ref(null)

const formName = ref('未命名表单')
const schemaList = ref([])
const selectedId = ref(null)
const showPreview = ref(false)
const currentTemplateId = ref(null)

const showSaveAsModal = ref(false)
const newTemplateName = ref('')

const themeOverrides = {
  common: {
    primaryColor: '#6366f1',
    primaryColorHover: '#818cf8',
    primaryColorPressed: '#4f46e5'
  }
}

const selectedComponent = computed(() => {
  return findComponentById(schemaList.value, selectedId.value)
})

function findComponentById(list, id) {
  for (const item of list) {
    if (item.id === id) return item
    if (item.children && item.children.length > 0) {
      const found = findComponentById(item.children, id)
      if (found) return found
    }
  }
  return null
}

function handleAddComponent(component) {
  const newItem = createComponentItem(component)
  schemaList.value = [...schemaList.value, newItem]
  selectedId.value = newItem.id
}

function handleAddToContainer(containerId, component) {
  const newItem = createComponentItem(component)
  addToContainer(schemaList.value, containerId, newItem)
  schemaList.value = [...schemaList.value]
  selectedId.value = newItem.id
}

function addToContainer(items, containerId, newItem) {
  for (let i = 0; i < items.length; i++) {
    if (items[i].id === containerId && items[i].type === 'container') {
      items[i].children.push(newItem)
      return true
    }
    if (items[i].children && items[i].children.length > 0) {
      if (addToContainer(items[i].children, containerId, newItem)) {
        return true
      }
    }
  }
  return false
}

function createComponentItem(component) {
  const baseItem = {
    id: Date.now() + Math.random().toString(36).substr(2, 9),
    type: component.type,
    label: component.label,
    field: component.field + Date.now().toString().slice(-4),
    placeholder: component.placeholder || '',
    options: component.options ? JSON.parse(JSON.stringify(component.options)) : [],
    required: false,
    maxStars: component.maxStars || 5,
    acceptTypes: component.acceptTypes ? JSON.parse(JSON.stringify(component.acceptTypes)) : [],
    maxSize: component.maxSize || 10,
    defaultValue: component.defaultValue !== undefined ? component.defaultValue : null,
    helpText: component.helpText || '',
    disabled: component.disabled !== undefined ? component.disabled : false,
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
  if (component.type === 'container') {
    baseItem.columns = component.columns || 2
    baseItem.children = []
  }
  return baseItem
}

function handleSelect(id) {
  selectedId.value = id
}

function handleUpdateList(list) {
  schemaList.value = [...list]
}

function handleRemove(id) {
  schemaList.value = removeComponentById(schemaList.value, id)
  if (selectedId.value === id) {
    selectedId.value = null
  }
}

function removeComponentById(list, id) {
  return list.filter(item => {
    if (item.id === id) return false
    if (item.children && item.children.length > 0) {
      item.children = removeComponentById(item.children, id)
    }
    return true
  })
}

function handleUpdateComponent(updated) {
  schemaList.value = updateComponentById(schemaList.value, updated)
}

function updateComponentById(list, updated) {
  return list.map(item => {
    if (item.id === updated.id) {
      return { ...item, ...updated }
    }
    if (item.children && item.children.length > 0) {
      return { ...item, children: updateComponentById(item.children, updated) }
    }
    return item
  })
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

    let res
    if (currentTemplateId.value) {
      res = await axios.put(`/api/forms/${currentTemplateId.value}`, payload)
    } else {
      res = await axios.post('/api/forms', payload)
      if (res.data.success && res.data.data) {
        currentTemplateId.value = res.data.data.id
      }
    }

    if (res.data.success) {
      message.success('保存成功')
      if (templateManagerRef.value) {
        templateManagerRef.value.loadTemplates()
      }
    } else {
      message.error('保存失败')
    }
  } catch (e) {
    message.error('保存失败：' + e.message)
  }
}

function handleSaveAs() {
  newTemplateName.value = (formName.value || '未命名模板') + ' 副本'
  showSaveAsModal.value = true
}

async function handleSaveAsConfirm() {
  if (!newTemplateName.value.trim()) {
    message.warning('请输入模板名称')
    return
  }
  try {
    const payload = {
      name: newTemplateName.value.trim(),
      schemaData: JSON.stringify(schemaList.value)
    }
    const res = await axios.post('/api/forms', payload)
    if (res.data.success) {
      message.success('另存成功')
      showSaveAsModal.value = false
      formName.value = newTemplateName.value.trim()
      currentTemplateId.value = res.data.data.id
      newTemplateName.value = ''
      if (templateManagerRef.value) {
        templateManagerRef.value.loadTemplates()
      }
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
  if (item.type === 'rating' && item.maxStars === undefined) {
    item.maxStars = 5
  }
  if (item.type === 'upload') {
    if (item.acceptTypes === undefined) {
      item.acceptTypes = []
    }
    if (item.maxSize === undefined) {
      item.maxSize = 10
    }
  }
  if (item.type === 'container') {
    if (item.columns === undefined) {
      item.columns = 2
    }
    if (item.children === undefined) {
      item.children = []
    }
    item.children = item.children.map(child => ensureValidation(child))
  }
  if (item.defaultValue === undefined) {
    if (item.type === 'switch') {
      item.defaultValue = false
    } else if (item.type === 'checkbox') {
      item.defaultValue = []
    } else if (item.type === 'input' || item.type === 'textarea') {
      item.defaultValue = ''
    } else if (item.type === 'container') {
      item.defaultValue = null
    } else {
      item.defaultValue = null
    }
  }
  if (item.helpText === undefined) {
    item.helpText = ''
  }
  if (item.disabled === undefined) {
    item.disabled = false
  }
  return item
}

function switchToBuilder() {
  currentPage.value = 'builder'
}

function switchToTemplates() {
  currentPage.value = 'templates'
  if (templateManagerRef.value) {
    templateManagerRef.value.loadTemplates()
  }
}

function handleEditTemplate(template) {
  formName.value = template.name
  currentTemplateId.value = template.id
  selectedId.value = null
  try {
    const list = JSON.parse(template.schemaData) || []
    schemaList.value = list.map(item => ensureValidation(item))
  } catch (e) {
    schemaList.value = []
  }
  currentPage.value = 'builder'
  message.success('已加载模板：' + template.name)
}

function handleCreateNewTemplate() {
  formName.value = '未命名表单'
  schemaList.value = []
  selectedId.value = null
  currentTemplateId.value = null
  currentPage.value = 'builder'
}

onMounted(async () => {
  try {
    const res = await axios.get('/api/forms')
    if (res.data.success && res.data.data && res.data.data.length > 0) {
      const latest = res.data.data[res.data.data.length - 1]
      formName.value = latest.name
      currentTemplateId.value = latest.id
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

.side-menu {
  width: 180px;
  background: #1e293b;
  border-right: 1px solid #334155;
  padding: 16px 0;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  color: #cbd5e1;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  border-left: 3px solid transparent;
}

.menu-item:hover {
  background: rgba(99, 102, 241, 0.1);
  color: #e2e8f0;
}

.menu-item.active {
  background: rgba(99, 102, 241, 0.15);
  color: #818cf8;
  border-left-color: #6366f1;
}

.builder-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}
</style>
