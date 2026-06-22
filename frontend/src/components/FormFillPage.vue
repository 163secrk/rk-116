<template>
  <div class="fill-page">
    <div v-if="loading" class="loading-wrap">
      <n-spin size="large" description="加载中..." />
    </div>

    <div v-else-if="error" class="error-wrap">
      <n-icon size="48" color="#ef4444"><AlertCircleOutline /></n-icon>
      <p class="error-text">{{ error }}</p>
    </div>

    <div v-else-if="needPassword && !passwordVerified" class="fill-container">
      <div class="form-header">
        <h1 class="form-title">{{ formName }}</h1>
        <p class="form-desc">该表单需要访问密码，请输入后继续</p>
      </div>
      <div class="form-body">
        <n-form label-placement="left" label-width="100px">
          <n-form-item label="访问密码">
            <n-input
              v-model:value="inputPassword"
              type="password"
              show-password-on="mousedown"
              placeholder="请输入访问密码"
              size="large"
              @keyup.enter="verifyPassword"
            />
          </n-form-item>
        </n-form>
      </div>
      <div class="form-footer">
        <n-button type="primary" size="large" :loading="verifyingPassword" @click="verifyPassword">
          确认
        </n-button>
      </div>
    </div>

    <div v-else class="fill-container">
      <div class="form-header">
        <h1 class="form-title">{{ formName }}</h1>
        <p v-if="schemaList.length === 0" class="form-desc">该表单暂无内容</p>
        <div v-if="submissionLimitInfo" class="limit-info">
          <n-icon size="16" color="#f59e0b"><AlertCircleOutline /></n-icon>
          <span>{{ submissionLimitInfo }}</span>
        </div>
      </div>

      <div v-if="schemaList.length > 0 && !reachedSubmitLimit" class="form-body">
        <div class="form-list">
          <PreviewItem
            v-for="comp in schemaList"
            :key="comp.id"
            :component="comp"
            :form-data="formData"
            :field-errors="fieldErrors"
            :touched-fields="touchedFields"
            :upload-file-lists="uploadFileLists"
            :submitted="submitted"
            @field-blur="handleFieldBlur"
            @field-change="handleFieldChange"
            @before-upload="handleBeforeUpload"
          />
        </div>
      </div>

      <div v-else-if="reachedSubmitLimit" class="limit-reached-wrap">
        <n-icon size="48" color="#f59e0b"><AlertCircleOutline /></n-icon>
        <p class="limit-reached-text">已达上限</p>
        <p class="limit-reached-desc">您已达到该表单的提交次数限制</p>
      </div>

      <div v-if="schemaList.length > 0 && !reachedSubmitLimit" class="form-footer">
        <n-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
          提交
        </n-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { NButton, NSpin, NIcon, NForm, NFormItem, NInput, createDiscreteApi } from 'naive-ui'
import { AlertCircleOutline } from '@vicons/ionicons5'
import axios from 'axios'
import PreviewItem from './PreviewItem.vue'
import { validateField, validateForm } from '../utils/validation.js'

const props = defineProps({
  token: {
    type: String,
    required: true
  }
})

const { message } = createDiscreteApi(['message'])

const loading = ref(true)
const error = ref('')
const formName = ref('')
const schemaList = ref([])
const submitted = ref(false)
const submitting = ref(false)

const needPassword = ref(false)
const passwordVerified = ref(false)
const inputPassword = ref('')
const verifyingPassword = ref(false)
const publishedFormData = ref(null)
const visitorSubmitCount = ref(0)

const formData = reactive({})
const fieldErrors = reactive({})
const touchedFields = reactive({})
const uploadFileLists = reactive({})

const VISITOR_ID_KEY = 'form_visitor_id'

function getOrCreateVisitorId() {
  let vid = localStorage.getItem(VISITOR_ID_KEY)
  if (!vid) {
    vid = 'v_' + Date.now().toString(36) + Math.random().toString(36).substr(2, 9)
    localStorage.setItem(VISITOR_ID_KEY, vid)
  }
  return vid
}

const visitorId = getOrCreateVisitorId()

axios.interceptors.request.use(config => {
  config.headers['X-Visitor-Id'] = visitorId
  return config
})

const reachedSubmitLimit = computed(() => {
  if (!publishedFormData.value || publishedFormData.value.maxSubmissionsPerPerson == null || publishedFormData.value.maxSubmissionsPerPerson <= 0) {
    return false
  }
  return visitorSubmitCount.value >= publishedFormData.value.maxSubmissionsPerPerson
})

const submissionLimitInfo = computed(() => {
  const infos = []
  if (publishedFormData.value) {
    const pf = publishedFormData.value
    if (pf.deadline) {
      const d = new Date(pf.deadline)
      if (!isNaN(d.getTime())) {
        const pad = (n) => n.toString().padStart(2, '0')
        infos.push('截止时间：' + d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()))
      }
    }
    if (pf.maxSubmissionsPerPerson != null && pf.maxSubmissionsPerPerson > 0) {
      infos.push('已提交 ' + visitorSubmitCount.value + '/' + pf.maxSubmissionsPerPerson + ' 次')
    }
  }
  return infos.join('  |  ')
})

function collectFields(list) {
  const fields = []
  list.forEach(item => {
    if (item.type === 'container') {
      if (item.children) {
        fields.push(...collectFields(item.children))
      }
    } else {
      fields.push(item)
    }
  })
  return fields
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

function initFormData(list) {
  Object.keys(formData).forEach(key => delete formData[key])
  Object.keys(fieldErrors).forEach(key => delete fieldErrors[key])
  Object.keys(touchedFields).forEach(key => delete touchedFields[key])
  Object.keys(uploadFileLists).forEach(key => delete uploadFileLists[key])
  submitted.value = false

  const flatList = collectFields(list)
  flatList.forEach(item => {
    if (item.type === 'checkbox') {
      formData[item.field] = item.defaultValue !== undefined ? item.defaultValue : []
    } else if (item.type === 'switch') {
      formData[item.field] = item.defaultValue !== undefined ? item.defaultValue : false
    } else if (item.type === 'upload') {
      formData[item.field] = null
      uploadFileLists[item.field] = []
    } else if (item.type === 'input' || item.type === 'textarea') {
      formData[item.field] = item.defaultValue !== undefined ? item.defaultValue : ''
    } else {
      formData[item.field] = item.defaultValue !== undefined ? item.defaultValue : null
    }
    fieldErrors[item.field] = []
    touchedFields[item.field] = false
  })
}

function getSchemaByField(field) {
  const flatList = collectFields(schemaList.value)
  return flatList.find(item => item.field === field)
}

function validateOneField(field) {
  const schema = getSchemaByField(field)
  if (!schema) return
  const result = validateField(formData[field], schema)
  fieldErrors[field] = result.errors
}

function validateAllFields() {
  const flatList = collectFields(schemaList.value)
  flatList.forEach(schema => {
    validateOneField(schema.field)
  })
}

function handleFieldBlur(field) {
  touchedFields[field] = true
  validateOneField(field)
}

function handleFieldChange(field) {
  if (touchedFields[field] || submitted.value) {
    validateOneField(field)
  }
}

function handleBeforeUpload(comp, data) {
  const file = data.file
  const maxSizeMB = comp.maxSize || 10
  if (file.file.size > maxSizeMB * 1024 * 1024) {
    message.error(`文件大小不能超过 ${maxSizeMB}MB`)
    return false
  }
  const acceptTypes = comp.acceptTypes || []
  if (acceptTypes.length > 0) {
    const fileName = file.name.toLowerCase()
    const fileType = file.type ? file.type.toLowerCase() : ''
    const matched = acceptTypes.some(accept => {
      const a = accept.toLowerCase()
      if (a.endsWith('/*')) {
        return fileType.startsWith(a.replace('/*', '/'))
      }
      if (a.startsWith('.')) {
        return fileName.endsWith(a)
      }
      return fileType === a || fileName.endsWith(a)
    })
    if (!matched) {
      message.error('文件类型不支持')
      return false
    }
  }
  formData[comp.field] = file.name
  handleFieldChange(comp.field)
  return true
}

async function verifyPassword() {
  if (!inputPassword.value || !inputPassword.value.trim()) {
    message.warning('请输入访问密码')
    return
  }
  verifyingPassword.value = true
  try {
    if (publishedFormData.value && publishedFormData.value.accessPassword === inputPassword.value.trim()) {
      passwordVerified.value = true
      message.success('验证通过')
    } else {
      message.error('密码错误')
    }
  } finally {
    verifyingPassword.value = false
  }
}

async function handleSubmit() {
  submitted.value = true
  validateAllFields()
  const flatList = collectFields(schemaList.value)
  const result = validateForm(formData, flatList)
  if (!result.valid) {
    message.error('请检查表单填写是否正确')
    const firstErrorField = flatList.find(schema => {
      const r = validateField(formData[schema.field], schema)
      return !r.valid
    })
    if (firstErrorField) {
      touchedFields[firstErrorField.field] = true
    }
    return
  }

  submitting.value = true
  try {
    const res = await axios.post(`/api/published-forms/submit/${props.token}`, {
      formData: { ...formData }
    })
    if (!res.data.success) {
      message.error(res.data.message || '提交失败')
      return
    }
    visitorSubmitCount.value += 1
    message.success('提交成功！感谢您的填写')
    submitted.value = false
    Object.keys(touchedFields).forEach(key => {
      touchedFields[key] = false
    })
  } catch (e) {
    if (e.response && e.response.data && e.response.data.message) {
      message.error(e.response.data.message)
    } else {
      message.error('提交失败：' + e.message)
    }
  } finally {
    submitting.value = false
  }
}

async function loadForm() {
  loading.value = true
  error.value = ''
  try {
    const res = await axios.get(`/api/published-forms/token/${props.token}`)
    if (!res.data.success) {
      error.value = res.data.message || '加载失败'
      return
    }
    const { publishedForm, formSchema, visitorSubmitCount: vsc } = res.data.data
    publishedFormData.value = publishedForm
    formName.value = publishedForm.formName || formSchema.name
    visitorSubmitCount.value = vsc || 0
    if (publishedForm.accessPassword && publishedForm.accessPassword.trim()) {
      needPassword.value = true
    }
    try {
      const list = JSON.parse(formSchema.schemaData) || []
      schemaList.value = list.map(item => ensureValidation(item))
      initFormData(schemaList.value)
    } catch (e) {
      schemaList.value = []
    }
  } catch (e) {
    if (e.response && e.response.data) {
      error.value = e.response.data.message || '加载失败'
    } else {
      error.value = '加载失败：' + e.message
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadForm()
})
</script>

<style>
.fill-page {
  min-height: 100vh;
  background: #f1f5f9;
  padding: 40px 16px;
}

.fill-container {
  max-width: 720px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.form-header {
  padding: 32px 32px 20px;
  border-bottom: 1px solid #e2e8f0;
}

.form-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.form-desc {
  font-size: 14px;
  color: #64748b;
  margin-top: 8px;
}

.limit-info {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  padding: 8px 12px;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 6px;
  font-size: 13px;
  color: #92400e;
}

.form-body {
  padding: 24px 32px;
}

.form-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-footer {
  padding: 20px 32px 32px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #e2e8f0;
}

.loading-wrap {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 120px 0;
  gap: 16px;
}

.error-wrap {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 120px 0;
  gap: 16px;
}

.error-text {
  font-size: 16px;
  color: #64748b;
}

.limit-reached-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 32px;
  gap: 12px;
  text-align: center;
}

.limit-reached-text {
  font-size: 20px;
  font-weight: 600;
  color: #92400e;
  margin: 0;
}

.limit-reached-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}
</style>
