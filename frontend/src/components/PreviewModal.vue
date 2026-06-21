<template>
  <n-modal v-model:show="showModal" preset="card" :title="formName || '表单预览'" style="width: 720px; max-width: 90vw;">
    <div class="preview-content">
      <div v-if="schemaList.length === 0" class="empty-tip">
        <n-icon size="40"><AlertCircleOutline /></n-icon>
        <p>暂无表单内容，请先添加组件</p>
      </div>
      <div v-else class="form-list">
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
import { NModal, NSpace, NButton, NIcon, createDiscreteApi } from 'naive-ui'
import { AlertCircleOutline } from '@vicons/ionicons5'
import { validateField, validateForm } from '../utils/validation.js'
import PreviewItem from './PreviewItem.vue'

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

watch(() => props.schemaList, (list) => {
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
  const flatList = collectFields(props.schemaList)
  return flatList.find(item => item.field === field)
}

function validateOneField(field) {
  const schema = getSchemaByField(field)
  if (!schema) return
  const result = validateField(formData[field], schema)
  fieldErrors[field] = result.errors
}

function validateAllFields() {
  const flatList = collectFields(props.schemaList)
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

function handleSubmit() {
  submitted.value = true
  validateAllFields()
  const flatList = collectFields(props.schemaList)
  const result = validateForm(formData, flatList)
  if (result.valid) {
    console.log('表单数据:', formData)
    message.success('表单提交成功！请查看控制台输出')
  } else {
    message.error('请检查表单填写是否正确')
    const firstErrorField = flatList.find(schema => {
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

.preview-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
