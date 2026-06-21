<template>
  <div class="form-item-wrap">
    <template v-if="component.type === 'container'">
      <div
        class="container-wrapper"
        :style="{ gridTemplateColumns: 'repeat(' + (component.columns || 2) + ', 1fr)' }"
      >
        <PreviewItem
          v-for="child in component.children || []"
          :key="child.id"
          :component="child"
          :form-data="formData"
          :field-errors="fieldErrors"
          :touched-fields="touchedFields"
          :upload-file-lists="uploadFileLists"
          :submitted="submitted"
          class="container-child"
          @field-blur="handleChildBlur"
          @field-change="handleChildChange"
          @before-upload="handleChildBeforeUpload"
        />
      </div>
    </template>
    <template v-else>
      <div class="form-label-wrap">
        <span class="form-label">{{ component.label }}</span>
        <span v-if="component.validation?.required" class="required-mark">*</span>
      </div>
      <div class="form-control-wrap" :class="{ 'has-error': showErrorVal }">
        <n-input
          v-if="component.type === 'input'"
          :value="formData[component.field]"
          @update:value="handleInputChange"
          :placeholder="component.placeholder"
          :disabled="component.disabled"
          @blur="handleBlur"
          :status="fieldStatus"
        />
        <n-input
          v-else-if="component.type === 'textarea'"
          :value="formData[component.field]"
          @update:value="handleInputChange"
          type="textarea"
          :placeholder="component.placeholder"
          :disabled="component.disabled"
          :rows="3"
          @blur="handleBlur"
          :status="fieldStatus"
        />
        <n-select
          v-else-if="component.type === 'select'"
          :value="formData[component.field]"
          @update:value="handleInputChange"
          :options="component.options"
          :placeholder="component.placeholder"
          :disabled="component.disabled"
          @blur="handleBlur"
          :status="fieldStatus"
        />
        <n-radio-group
          v-else-if="component.type === 'radio'"
          :value="formData[component.field]"
          @update:value="handleInputChange"
          :disabled="component.disabled"
        >
          <n-space>
            <n-radio v-for="opt in component.options" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </n-radio>
          </n-space>
        </n-radio-group>
        <n-checkbox-group
          v-else-if="component.type === 'checkbox'"
          :value="formData[component.field]"
          @update:value="handleInputChange"
          :disabled="component.disabled"
        >
          <n-space>
            <n-checkbox v-for="opt in component.options" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </n-checkbox>
          </n-space>
        </n-checkbox-group>
        <n-date-picker
          v-else-if="component.type === 'date'"
          :value="formData[component.field]"
          @update:value="handleInputChange"
          type="date"
          :placeholder="component.placeholder"
          :disabled="component.disabled"
          @blur="handleBlur"
        />
        <n-time-picker
          v-else-if="component.type === 'time'"
          :value="formData[component.field]"
          @update:value="handleInputChange"
          :placeholder="component.placeholder"
          :disabled="component.disabled"
          @blur="handleBlur"
        />
        <n-rate
          v-else-if="component.type === 'rating'"
          :value="formData[component.field]"
          @update:value="handleInputChange"
          :count="component.maxStars || 5"
          :disabled="component.disabled"
        />
        <n-upload
          v-else-if="component.type === 'upload'"
          :file-list="uploadFileLists[component.field]"
          @update:file-list="handleFileListChange"
          :accept="(component.acceptTypes || []).join(',')"
          :max="1"
          :disabled="component.disabled"
          @before-upload="handleBeforeUpload"
        >
          <n-button>
            <template #icon>
              <n-icon><CloudUploadOutline /></n-icon>
            </template>
            上传文件
          </n-button>
          <template #tip>
            <div class="preview-upload-tip">
              支持：{{ (component.acceptTypes || []).join('、') }}，大小不超过 {{ component.maxSize || 10 }}MB
            </div>
          </template>
        </n-upload>
        <n-switch
          v-else-if="component.type === 'switch'"
          :value="formData[component.field]"
          @update:value="handleInputChange"
          :disabled="component.disabled"
        />
      </div>
      <div v-if="component.helpText" class="help-text">{{ component.helpText }}</div>
      <div v-if="showErrorVal" class="error-tip">
        {{ fieldErrors[component.field][0] }}
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { NInput, NSelect, NRadioGroup, NRadio, NCheckboxGroup, NCheckbox, NSpace, NDatePicker, NTimePicker, NRate, NUpload, NButton, NIcon, NSwitch } from 'naive-ui'
import { CloudUploadOutline } from '@vicons/ionicons5'

defineOptions({
  name: 'PreviewItem'
})

const props = defineProps({
  component: {
    type: Object,
    required: true
  },
  formData: {
    type: Object,
    required: true
  },
  fieldErrors: {
    type: Object,
    required: true
  },
  touchedFields: {
    type: Object,
    required: true
  },
  uploadFileLists: {
    type: Object,
    required: true
  },
  submitted: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['field-blur', 'field-change', 'before-upload'])

const showErrorVal = computed(() => {
  const hasError = props.fieldErrors[props.component.field] && props.fieldErrors[props.component.field].length > 0
  return hasError && (props.touchedFields[props.component.field] || props.submitted)
})

const fieldStatus = computed(() => {
  if (showErrorVal.value) {
    return 'error'
  }
  return null
})

function handleBlur() {
  emit('field-blur', props.component.field)
}

function handleInputChange(val) {
  props.formData[props.component.field] = val
  emit('field-change', props.component.field)
}

function handleFileListChange(val) {
  props.uploadFileLists[props.component.field] = val
}

function handleBeforeUpload(data) {
  return emit('before-upload', props.component, data)
}

function handleChildBlur(field) {
  emit('field-blur', field)
}

function handleChildChange(field) {
  emit('field-change', field)
}

function handleChildBeforeUpload(comp, data) {
  return emit('before-upload', comp, data)
}
</script>

<style scoped>
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

.container-wrapper {
  display: grid;
  gap: 16px;
  width: 100%;
}

.container-child {
  min-width: 0;
}
</style>
