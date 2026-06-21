<template>
  <div class="form-item">
    <label class="form-label">
      {{ component.label || '未命名' }}
      <span v-if="component.validation?.required" class="required">*</span>
    </label>
    <div class="form-control">
      <n-input
        v-if="component.type === 'input'"
        :placeholder="component.placeholder"
        size="small"
        disabled
      />
      <n-input
        v-else-if="component.type === 'textarea'"
        type="textarea"
        :placeholder="component.placeholder"
        size="small"
        :rows="3"
        disabled
      />
      <n-select
        v-else-if="component.type === 'select'"
        :options="component.options"
        :placeholder="component.placeholder"
        size="small"
        disabled
      />
      <n-radio-group v-else-if="component.type === 'radio'" disabled>
        <n-space>
          <n-radio v-for="opt in component.options" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </n-radio>
        </n-space>
      </n-radio-group>
      <n-checkbox-group v-else-if="component.type === 'checkbox'" disabled>
        <n-space>
          <n-checkbox v-for="opt in component.options" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </n-checkbox>
        </n-space>
      </n-checkbox-group>
      <n-date-picker
        v-else-if="component.type === 'date'"
        type="date"
        size="small"
        disabled
      />
      <n-time-picker
        v-else-if="component.type === 'time'"
        size="small"
        disabled
      />
      <n-rate
        v-else-if="component.type === 'rating'"
        :count="component.maxStars || 5"
        disabled
      />
      <n-upload
        v-else-if="component.type === 'upload'"
        :accept="(component.acceptTypes || []).join(',')"
        :max="1"
        disabled
        trigger
      >
        <n-button size="small">
          <template #icon>
            <n-icon><CloudUploadOutline /></n-icon>
          </template>
          上传文件
        </n-button>
        <template #tip>
          <div class="upload-tip">
            支持：{{ (component.acceptTypes || []).join('、') }}，大小不超过 {{ component.maxSize || 10 }}MB
          </div>
        </template>
      </n-upload>
      <n-switch
        v-else-if="component.type === 'switch'"
        :value="component.defaultValue"
        disabled
      />
    </div>
  </div>
</template>

<script setup>
import { NInput, NSelect, NRadioGroup, NRadio, NCheckboxGroup, NCheckbox, NSpace, NDatePicker, NTimePicker, NRate,NUpload, NButton, NIcon, NSwitch } from 'naive-ui'
import { CloudUploadOutline } from '@vicons/ionicons5'

defineProps({
  component: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.form-item {
  width: 100%;
}

.form-label {
  display: block;
  font-size: 13px;
  color: #cbd5e1;
  margin-bottom: 8px;
  font-weight: 500;
}

.required {
  color: #ef4444;
  margin-left: 2px;
}

.form-control {
  width: 100%;
}

.upload-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}
</style>
