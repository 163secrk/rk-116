<template>
  <div class="form-item">
    <label class="form-label">
      {{ component.label || '未命名' }}
      <span v-if="component.required" class="required">*</span>
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
    </div>
  </div>
</template>

<script setup>
import { NInput, NSelect, NRadioGroup, NRadio, NCheckboxGroup, NCheckbox, NSpace, NDatePicker, NTimePicker } from 'naive-ui'

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
</style>
