<template>
  <n-modal v-model:show="showModal" preset="card" :title="formName || '表单预览'" style="width: 720px; max-width: 90vw;">
    <div class="preview-content">
      <div v-if="schemaList.length === 0" class="empty-tip">
        <n-icon size="40"><AlertCircleOutline /></n-icon>
        <p>暂无表单内容，请先添加组件</p>
      </div>
      <div v-else class="form-list">
        <div v-for="comp in schemaList" :key="comp.id" class="form-item-wrap">
          <div class="form-label-wrap">
            <span class="form-label">{{ comp.label }}</span>
            <span v-if="comp.validation?.required" class="required-mark">*</span>
          </div>
          <div class="form-control-wrap" :class="{ 'has-error': showError(comp.field) }">
            <n-input
              v-if="comp.type === 'input'"
              v-model:value="formData[comp.field]"
              :placeholder="comp.placeholder"
              :disabled="comp.disabled"
              @blur="handleFieldBlur(comp.field)"
              @update:value="handleFieldChange(comp.field)"
              :status="getFieldStatus(comp.field)"
            />
            <n-input
              v-else-if="comp.type === 'textarea'"
              v-model:value="formData[comp.field]"
              type="textarea"
              :placeholder="comp.placeholder"
              :disabled="comp.disabled"
              :rows="3"
              @blur="handleFieldBlur(comp.field)"
              @update:value="handleFieldChange(comp.field)"
              :status="getFieldStatus(comp.field)"
            />
            <n-select
              v-else-if="comp.type === 'select'"
              v-model:value="formData[comp.field]"
              :options="comp.options"
              :placeholder="comp.placeholder"
              :disabled="comp.disabled"
              @blur="handleFieldBlur(comp.field)"
              @update:value="handleFieldChange(comp.field)"
              :status="getFieldStatus(comp.field)"
            />
            <n-radio-group
              v-else-if="comp.type === 'radio'"
              v-model:value="formData[comp.field]"
              :disabled="comp.disabled"
              @update:value="handleFieldChange(comp.field)"
            >
              <n-space>
                <n-radio v-for="opt in comp.options" :key="opt.value" :value="opt.value">
                  {{ opt.label }}
                </n-radio>
              </n-space>
            </n-radio-group>
            <n-checkbox-group
              v-else-if="comp.type === 'checkbox'"
              v-model:value="formData[comp.field]"
              :disabled="comp.disabled"
              @update:value="handleFieldChange(comp.field)"
            >
              <n-space>
                <n-checkbox v-for="opt in comp.options" :key="opt.value" :value="opt.value">
                  {{ opt.label }}
                </n-checkbox>
              </n-space>
            </n-checkbox-group>
            <n-date-picker
              v-else-if="comp.type === 'date'"
              v-model:value="formData[comp.field]"
              type="date"
              :placeholder="comp.placeholder"
              :disabled="comp.disabled"
              @blur="handleFieldBlur(comp.field)"
              @update:value="handleFieldChange(comp.field)"
            />
            <n-time-picker
              v-else-if="comp.type === 'time'"
              v-model:value="formData[comp.field]"
              :placeholder="comp.placeholder"
              :disabled="comp.disabled"
              @blur="handleFieldBlur(comp.field)"
              @update:value="handleFieldChange(comp.field)"
            />
            <n-rate
              v-else-if="comp.type === 'rating'"
              v-model:value="formData[comp.field]"
              :count="comp.maxStars || 5"
              :disabled="comp.disabled"
              @update:value="handleFieldChange(comp.field)"
            />
            <n-upload
              v-else-if="comp.type === 'upload'"
              v-model:file-list="uploadFileLists[comp.field]"
              :accept="(comp.acceptTypes || []).join(',')"
              :max="1"
              :disabled="comp.disabled"
              @before-upload="(data) => handleBeforeUpload(comp, data)"
            >
              <n-button>
                <template #icon>
                  <n-icon><CloudUploadOutline /></n-icon>
                </template>
                上传文件
              </n-button>
              <template #tip>
                <div class="preview-upload-tip">
                  支持：{{ (comp.acceptTypes || []).join('、') }}，大小不超过 {{ comp.maxSize || 10 }}MB
                </div>
              </template>
            </n-upload>
            <n-switch
              v-else-if="comp.type === 'switch'"
              v-model:value="formData[comp.field]"
              :disabled="comp.disabled"
              @update:value="handleFieldChange(comp.field)"
            />
          </div>
          <div v-if="comp.helpText" class="help-text">{{ comp.helpText }}</div>
          <div v-if="showError(comp.field)" class="error-tip">
            {{ fieldErrors[comp.field][0] }}
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="preview-footer">
        <n-space>
          <n-button @click="showModal = false">关闭</n-button>
          <n-button type="primary" @click="handleSubmit">提交</n-button>
        </n-space>
      </div>
    </template>
  </n-modal>
</template>

<script setup>
import { ref, watch, reactive, computed } from 'vue'
import { NModal, NInput, NSelect, NRadioGroup, NRadio, NCheckboxGroup, NCheckbox, NSpace, NButton, NIcon, NRate, NUpload, NSwitch, createDiscreteApi } from 'naive-ui'
import { AlertCircleOutline, CloudUploadOutline } from '@vicons/ionicons5'
import { validateField, validateForm } from '../utils/validation.js'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  schemaList: {
    type: Array,
    default: () => []
  },
  formName: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:show'])
const { message } = createDiscreteApi(['message'])

const showModal = computed({
  get: () => props.show,
  set: (val) => emit('update:show', val)
})

const formData = reactive({})
const fieldErrors = reactive({})
const touchedFields = reactive({})
const uploadFileLists = reactive({})
const submitted = ref(false)

watch(() => props.schemaList, (list) => {
  Object.keys(formData).forEach(key => delete formData[key])
  Object.keys(fieldErrors).forEach(key => delete fieldErrors[key])
  Object.keys(touchedFields).forEach(key => delete touchedFields[key])
  Object.keys(uploadFileLists).forEach(key => delete uploadFileLists[key])
  submitted.value = false
  list.forEach(item => {
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
}, { immediate: true, deep: true })

watch(() => props.show, (val) => {
  if (val) {
    submitted.value = false
    Object.keys(touchedFields).forEach(key => {
      touchedFields[key] = false
    })
    validateAllFields()
  }
})

function getSchemaByField(field) {
  return props.schemaList.find(item => item.field === field)
}

function validateOneField(field) {
  const schema = getSchemaByField(field)
  if (!schema) return
  const result = validateField(formData[field], schema)
  fieldErrors[field] = result.errors
}

function validateAllFields() {
  props.schemaList.forEach(schema => {
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

function showError(field) {
  const hasError = fieldErrors[field] && fieldErrors[field].length > 0
  return hasError && (touchedFields[field] || submitted.value)
}

function getFieldStatus(field) {
  if (showError(field)) {
    return 'error'
  }
  return null
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

function handleSubmit() {
  submitted.value = true
  validateAllFields()
  const result = validateForm(formData, props.schemaList)
  if (result.valid) {
    console.log('表单数据:', formData)
    message.success('表单提交成功！请查看控制台输出')
  } else {
    message.error('请检查表单填写是否正确')
    const firstErrorField = props.schemaList.find(schema => {
      const r = validateField(formData[schema.field], schema)
      return !r.valid
    })
    if (firstErrorField) {
      touchedFields[firstErrorField.field] = true
    }
  }
}
</script>

<style scoped>
.preview-content {
  padding: 8px 0;
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 0;
  gap: 12px;
  color: #64748b;
}

.empty-tip p {
  font-size: 14px;
}

.form-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-item-wrap {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label-wrap {
  display: flex;
  align-items: center;
  gap: 2px;
}

.form-label {
  font-size: 13px;
  color: #334155;
  font-weight: 500;
}

.required-mark {
  color: #ef4444;
  font-size: 14px;
  line-height: 1;
}

.form-control-wrap {
  width: 100%;
}

.error-tip {
  font-size: 12px;
  color: #ef4444;
  line-height: 1.4;
}

.preview-footer {
  display: flex;
  justify-content: flex-end;
}

.preview-upload-tip {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.help-text {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
  line-height: 1.4;
}
</style>
