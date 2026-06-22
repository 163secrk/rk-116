<template>
  <FormFillPage v-if="isFillPage" :token="fillToken" />
  <n-config-provider v-else :theme="darkTheme" :theme-overrides="themeOverrides">
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
                <n-tag
                  v-if="publishStatus"
                  type="success"
                  size="small"
                  round
                  style="margin-right: 8px"
                >
                  <template #icon>
                    <n-icon><CloudDoneOutline /></n-icon>
                  </template>
                  已发布
                </n-tag>
                <n-tag
                  v-else
                  type="default"
                  size="small"
                  round
                  style="margin-right: 8px; opacity: 0.7"
                >
                  <template #icon>
                    <n-icon><CloudOutline /></n-icon>
                  </template>
                  未发布
                </n-tag>
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
                <n-button
                  type="warning"
                  size="small"
                  :loading="publishing"
                  @click="handlePublish"
                >
                  <template #icon>
                    <n-icon><RocketOutline /></n-icon>
                  </template>
                  {{ publishStatus ? '重新发布' : '发布' }}
                </n-button>
                <n-button size="small" @click="showPublishSettings = true">
                  <template #icon>
                    <n-icon><SettingsOutline /></n-icon>
                  </template>
                  发布设置
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
              <div
                :class="['menu-item', { active: currentPage === 'published' }]"
                @click="switchToPublished"
              >
                <n-icon size="20"><RocketOutline /></n-icon>
                <span>已发布表单</span>
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

            <PublishedForms
              v-if="currentPage === 'published'"
              ref="publishedFormsRef"
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

          <n-modal
            v-model:show="showPublishSuccessModal"
            preset="card"
            title="发布成功"
            style="width: 520px"
          >
            <div class="publish-success-content">
              <div class="publish-success-tip">表单已发布，您可以通过以下链接分享给他人填写：</div>
              <div class="share-link-wrap">
                <n-input :value="shareLink" readonly size="large" />
                <n-button type="primary" size="large" @click="copyShareLink">
                  <template #icon>
                    <n-icon><CopyOutline /></n-icon>
                  </template>
                  复制链接
                </n-button>
              </div>
            </div>
            <template #footer>
              <div style="text-align: right">
                <n-button type="primary" @click="showPublishSuccessModal = false">
                  确定
                </n-button>
              </div>
            </template>
          </n-modal>

          <n-drawer v-model:show="showPublishSettings" placement="right" :width="440">
            <n-drawer-content title="发布设置" :closable="true">
              <n-form label-placement="left" label-width="130">
                <n-form-item label="提交截止时间">
                  <n-date-picker
                    v-model:value="publishSettings.deadline"
                    type="datetime"
                    clearable
                    placeholder="选择截止时间（不填则不限制）"
                    style="width: 100%"
                    format="yyyy-MM-dd HH:mm"
                    value-format="yyyy-MM-dd HH:mm:ss"
                  />
                </n-form-item>
                <n-form-item label="每人提交次数">
                  <div style="display: flex; flex-direction: column; gap: 12px;">
                    <n-radio-group v-model:value="publishSettings.submitLimitType">
                      <n-space vertical>
                        <n-radio value="unlimited">不限</n-radio>
                        <n-radio value="once">1次</n-radio>
                        <n-radio value="custom">
                          <span style="display: inline-flex; align-items: center; gap: 8px;">
                            N次
                            <n-input-number
                              v-model:value="publishSettings.customLimit"
                              :min="1"
                              :max="9999"
                              size="small"
                              :disabled="publishSettings.submitLimitType !== 'custom'"
                              placeholder="次数"
                            />
                          </span>
                        </n-radio>
                      </n-space>
                    </n-radio-group>
                  </div>
                </n-form-item>
                <n-form-item label="访问密码">
                  <n-input
                    v-model:value="publishSettings.accessPassword"
                    type="password"
                    show-password-on="click"
                    maxlength="50"
                    placeholder="选填，填写后访问需要输入密码"
                    clearable
                  />
                </n-form-item>
              </n-form>
              <template #footer>
                <div style="display: flex; justify-content: flex-end; gap: 8px;">
                  <n-button @click="showPublishSettings = false">取消</n-button>
                  <n-button type="primary" @click="savePublishSettings">保存设置</n-button>
                </div>
              </template>
            </n-drawer-content>
          </n-drawer>
        </div>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { ref, computed, onMounted, watch, reactive } from 'vue'
import {
  NConfigProvider, NGlobalStyle, NButton, NSpace, NInput, NIcon, NTag,
  NMessageProvider, NDialogProvider, NModal, NDrawer, NDrawerContent,
  NForm, NFormItem, NDatePicker, NRadioGroup, NRadio, NInputNumber,
  createDiscreteApi
} from 'naive-ui'
import { darkTheme } from 'naive-ui'
import {
  EyeOutline, SaveOutline, DuplicateOutline, BuildOutline, DocumentsOutline,
  RocketOutline, CloudDoneOutline, CloudOutline, CopyOutline, SettingsOutline
} from '@vicons/ionicons5'
import axios from 'axios'
import ComponentPanel from './components/ComponentPanel.vue'
import FormCanvas from './components/FormCanvas.vue'
import PropertyPanel from './components/PropertyPanel.vue'
import PreviewModal from './components/PreviewModal.vue'
import TemplateManager from './components/TemplateManager.vue'
import PublishedForms from './components/PublishedForms.vue'
import FormFillPage from './components/FormFillPage.vue'

const { message } = createDiscreteApi(['message'])

const currentPage = ref('builder')
const templateManagerRef = ref(null)
const publishedFormsRef = ref(null)

const formName = ref('未命名表单')
const schemaList = ref([])
const selectedId = ref(null)
const showPreview = ref(false)
const currentTemplateId = ref(null)

const showSaveAsModal = ref(false)
const newTemplateName = ref('')

const publishStatus = ref(null)
const publishing = ref(false)
const shareLink = ref('')
const showPublishSuccessModal = ref(false)
const showPublishSettings = ref(false)
const publishSettings = reactive({
  deadline: null,
  submitLimitType: 'unlimited',
  customLimit: 1,
  accessPassword: ''
})

const isFillPage = computed(() => window.location.hash.startsWith('#/fill/'))
const fillToken = computed(() => {
  const match = window.location.hash.match(/#\/fill\/(.+)/)
  return match ? match[1] : ''
})

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
      if (currentTemplateId.value) {
        checkPublishStatus()
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
      checkPublishStatus()
    } else {
      message.error('保存失败')
    }
  } catch (e) {
    message.error('保存失败：' + e.message)
  }
}

function buildPublishSettingsPayload() {
  const settings = {}
  if (publishSettings.deadline) {
    settings.deadline = publishSettings.deadline
  }
  if (publishSettings.submitLimitType === 'once') {
    settings.maxSubmissionsPerPerson = 1
  } else if (publishSettings.submitLimitType === 'custom' && publishSettings.customLimit > 0) {
    settings.maxSubmissionsPerPerson = publishSettings.customLimit
  }
  if (publishSettings.accessPassword && publishSettings.accessPassword.trim()) {
    settings.accessPassword = publishSettings.accessPassword.trim()
  }
  return settings
}

function loadSettingsFromPublishStatus(status) {
  if (!status) {
    publishSettings.deadline = null
    publishSettings.submitLimitType = 'unlimited'
    publishSettings.customLimit = 1
    publishSettings.accessPassword = ''
    return
  }
  if (status.deadline) {
    const d = new Date(status.deadline)
    if (!isNaN(d.getTime())) {
      publishSettings.deadline = status.deadline
    } else {
      publishSettings.deadline = null
    }
  } else {
    publishSettings.deadline = null
  }
  if (status.maxSubmissionsPerPerson != null && status.maxSubmissionsPerPerson > 0) {
    if (status.maxSubmissionsPerPerson === 1) {
      publishSettings.submitLimitType = 'once'
    } else {
      publishSettings.submitLimitType = 'custom'
      publishSettings.customLimit = status.maxSubmissionsPerPerson
    }
  } else {
    publishSettings.submitLimitType = 'unlimited'
  }
  publishSettings.accessPassword = status.accessPassword || ''
}

async function handlePublish() {
  if (schemaList.value.length === 0) {
    message.warning('请先添加表单组件')
    return
  }
  if (!currentTemplateId.value) {
    message.info('请先保存表单')
    await handleSave()
    if (!currentTemplateId.value) return
  }
  publishing.value = true
  try {
    const payload = {
      name: formName.value,
      schemaData: JSON.stringify(schemaList.value)
    }
    await axios.put(`/api/forms/${currentTemplateId.value}`, payload)

    const settings = buildPublishSettingsPayload()
    const res = await axios.post(`/api/published-forms/publish/${currentTemplateId.value}`, { settings })
    if (res.data.success) {
      publishStatus.value = res.data.data
      shareLink.value = `${window.location.origin}${window.location.pathname}#/fill/${res.data.data.token}`
      showPublishSuccessModal.value = true
      if (publishedFormsRef.value) {
        publishedFormsRef.value.loadPublishedForms()
      }
    } else {
      message.error(res.data.message || '发布失败')
    }
  } catch (e) {
    message.error('发布失败：' + e.message)
  } finally {
    publishing.value = false
  }
}

function savePublishSettings() {
  message.success('发布设置已保存')
  showPublishSettings.value = false
}

async function checkPublishStatus() {
  if (!currentTemplateId.value) {
    publishStatus.value = null
    loadSettingsFromPublishStatus(null)
    return
  }
  try {
    const res = await axios.get(`/api/published-forms/status/${currentTemplateId.value}`)
    if (res.data.success) {
      publishStatus.value = res.data.data
      loadSettingsFromPublishStatus(res.data.data)
    } else {
      publishStatus.value = null
      loadSettingsFromPublishStatus(null)
    }
  } catch (e) {
    publishStatus.value = null
    loadSettingsFromPublishStatus(null)
  }
}

function copyShareLink() {
  if (!shareLink.value) return
  navigator.clipboard.writeText(shareLink.value).then(() => {
    message.success('链接已复制')
  }).catch(() => {
    const textarea = document.createElement('textarea')
    textarea.value = shareLink.value
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    message.success('链接已复制')
  })
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

function switchToPublished() {
  currentPage.value = 'published'
  if (publishedFormsRef.value) {
    publishedFormsRef.value.loadPublishedForms()
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
  checkPublishStatus()
  currentPage.value = 'builder'
  message.success('已加载模板：' + template.name)
}

function handleCreateNewTemplate() {
  formName.value = '未命名表单'
  schemaList.value = []
  selectedId.value = null
  currentTemplateId.value = null
  publishStatus.value = null
  currentPage.value = 'builder'
}

watch(currentTemplateId, () => {
  checkPublishStatus()
})

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
      checkPublishStatus()
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

.publish-success-content {
  padding: 8px 0;
}

.publish-success-tip {
  font-size: 14px;
  color: #334155;
  margin-bottom: 16px;
}

.share-link-wrap {
  display: flex;
  gap: 8px;
}
</style>
