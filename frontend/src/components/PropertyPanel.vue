<template>
  <aside class="property-panel">
    <div class="panel-header">
      <n-icon size="18"><SettingsOutline /></n-icon>
      <span>属性面板</span>
    </div>
    <div class="panel-content">
      <div v-if="!selectedComponent" class="empty-state">
        <n-icon size="40"><HandRightOutline /></n-icon>
        <p>请选择一个组件进行编辑</p>
      </div>
      <div v-else class="property-form">
        <div class="property-section">
          <div class="section-title">基本属性</div>
          <n-form label-placement="left" label-width="80px">
            <n-form-item label="标签">
              <n-input v-model:value="localComp.label" placeholder="请输入标签" size="small" />
            </n-form-item>
            <n-form-item label="字段名">
              <n-input v-model:value="localComp.field" placeholder="请输入字段名" size="small" />
            </n-form-item>
            <n-form-item v-if="hasPlaceholder" label="占位符">
              <n-input v-model:value="localComp.placeholder" placeholder="请输入占位符" size="small" />
            </n-form-item>
            <n-form-item label="必填">
              <n-switch v-model:value="localComp.required" />
            </n-form-item>
          </n-form>
        </div>

        <div v-if="hasOptions" class="property-section">
          <div class="section-title">
            <span>选项配置</span>
            <n-button text size="tiny" type="primary" @click="addOption">
              <template #icon>
                <n-icon><AddOutline /></n-icon>
              </template>
              添加选项
            </n-button>
          </div>
          <div class="options-list">
            <div
              v-for="(opt, idx) in localComp.options"
              :key="idx"
              class="option-item"
            >
              <n-input
                v-model:value="opt.label"
                placeholder="标签"
                size="small"
                style="flex: 1; margin-right: 8px"
              />
              <n-input
                v-model:value="opt.value"
                placeholder="值"
                size="small"
                style="flex: 1; margin-right: 8px"
              />
              <n-button text size="tiny" type="error" @click="removeOption(idx)">
                <template #icon>
                  <n-icon size="16"><TrashOutline /></n-icon>
                </template>
              </n-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { NIcon, NForm, NFormItem, NInput, NSwitch, NButton } from 'naive-ui'
import { SettingsOutline, HandRightOutline, AddOutline, TrashOutline } from '@vicons/ionicons5'

const props = defineProps({
  selectedComponent: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update-component'])

const localComp = ref({
  id: null,
  label: '',
  field: '',
  placeholder: '',
  options: [],
  required: false
})

watch(() => props.selectedComponent, (val) => {
  if (val) {
    localComp.value = JSON.parse(JSON.stringify(val))
  }
}, { immediate: true, deep: true })

watch(localComp, (val) => {
  if (val.id && props.selectedComponent) {
    emit('update-component', JSON.parse(JSON.stringify(val)))
  }
}, { deep: true })

const hasPlaceholder = computed(() => {
  return ['input', 'textarea', 'select', 'date', 'time'].includes(localComp.value.type)
})

const hasOptions = computed(() => {
  return ['select', 'radio', 'checkbox'].includes(localComp.value.type)
})

function addOption() {
  localComp.value.options.push({
    label: '选项' + (localComp.value.options.length + 1),
    value: 'option' + (localComp.value.options.length + 1)
  })
}

function removeOption(idx) {
  localComp.value.options.splice(idx, 1)
}
</script>

<style scoped>
.property-panel {
  width: 300px;
  background: #1e293b;
  border-left: 1px solid #334155;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.panel-header {
  height: 48px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
  border-bottom: 1px solid #334155;
  color: #e2e8f0;
  font-weight: 600;
  font-size: 14px;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
}

.empty-state {
  padding: 48px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
  text-align: center;
}

.empty-state p {
  font-size: 13px;
}

.property-form {
  padding: 16px;
}

.property-section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  font-weight: 600;
  color: #94a3b8;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #334155;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  align-items: center;
}
</style>
