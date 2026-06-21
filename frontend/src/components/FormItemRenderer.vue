<template>
  <div class="form-item">
    <label v-if="component.type !== 'container'" class="form-label">
      {{ component.label || '未命名' }}
      <span v-if="component.validation?.required" class="required">*</span>
    </label>
    <div class="form-control">
      <div
        v-if="component.type === 'container'"
        class="container-wrapper"
        :style="{ gridTemplateColumns: `repeat(${component.columns || 2}, 1fr)` }"
      >
        <div
          v-for="child in component.children || []"
          :key="child.id"
          class="container-item"
        >
          <FormItemRenderer :component="child" />
        </div>
        <div v-if="!component.children || component.children.length === 0" class="container-empty">
          空容器，拖拽组件到此处
        </div>
      </div>
      <n-input
        v-else-if="component.type === 'input'"
        :placeholder="component.placeholder"
        size="small"
        :disabled="component.disabled"
        :value="component.defaultValue"
      />
      <n-input
        v-else-if="component.type === 'textarea'"
        type="textarea"
        :placeholder="component.placeholder"
        size="small"
        :rows="3"
        :disabled="component.disabled"
        :value="component.defaultValue"
      />
      <n-select
        v-else-if="component.type === 'select'"
        :options="component.options"
        :placeholder="component.placeholder"
        size="small"
        :disabled="component.disabled"
        :value="component.defaultValue"
      />
      <n-radio-group
        v-else-if="component.type === 'radio'"
        :disabled="component.disabled"
        :value="component.defaultValue"
      >
        <n-space>
          <n-radio v-for="opt in component.options" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </n-radio>
        </n-space>
      </n-radio-group>
      <n-checkbox-group
        v-else-if="component.type === 'checkbox'"
        :disabled="component.disabled"
        :value="component.defaultValue"
      >
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
        :disabled="component.disabled"
        :placeholder="component.placeholder"
        :value="component.defaultValue"
      />
      <n-time-picker
        v-else-if="component.type === 'time'"
        size="small"
        :disabled="component.disabled"
        :placeholder="component.placeholder"
        :value="component.defaultValue"
      />
      <n-rate
        v-else-if="component.type === 'rating'"
        :count="component.maxStars || 5"
        :disabled="component.disabled"
        :value="component.defaultValue"
      />
      <n-upload
        v-else-if="component.type === 'upload'"
        :accept="(component.acceptTypes || []).join(',')"
        :max="1"
        :disabled="component.disabled"
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
        :disabled="component.disabled"
      />
    </div>
    <div v-if="component.helpText && component.type !== 'container'" class="help-text">{{ component.helpText }}</div>
  </div>
</template>

<script setup>
import { NInput, NSelect, NRadioGroup, NRadio, NCheckboxGroup, NCheckbox, NSpace, NDatePicker, NTimePicker, NRate,NUpload, NButton, NIcon, NSwitch } from 'naive-ui'
import { CloudUploadOutline } from '@vicons/ionicons5'

defineOptions({
  name: 'FormItemRenderer'
})

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

.help-text {
  font-size: 12px;
  color: #64748b;
  margin-top: 6px;
  line-height: 1.4;
}

.container-wrapper {
  display: grid;
  gap: 12px;
  padding: 0;
}

.container-item {
  min-width: 0;
}

.container-empty {
  grid-column: 1 / -1;
  padding: 24px;
  text-align: center;
  color: #64748b;
  font-size: 13px;
  border: 2px dashed #334155;
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.5);
}
</style>
