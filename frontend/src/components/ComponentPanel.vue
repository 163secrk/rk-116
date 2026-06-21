<template>
  <aside class="component-panel">
    <div class="panel-header">
      <n-icon size="18"><ConstructOutline /></n-icon>
      <span>组件面板</span>
    </div>
    <div class="panel-content">
      <div class="category-title">基础组件</div>
      <div class="component-grid">
        <div
          v-for="comp in componentList"
          :key="comp.type"
          class="component-item"
          draggable="true"
          @dragstart="handleDragStart($event, comp)"
          @click="$emit('add-component', comp)"
        >
          <div class="component-icon">
            <n-icon size="20">
              <component :is="comp.icon" />
            </n-icon>
          </div>
          <span class="component-name">{{ comp.label }}</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { NIcon } from 'naive-ui'
import {
  ConstructOutline,
  TextOutline,
  ListOutline,
  CheckmarkCircleOutline,
  RadioButtonOnOutline,
  DocumentTextOutline,
  CalendarOutline,
  TimeOutline,
  StarOutline,
  CloudUploadOutline,
  ToggleOutline
} from '@vicons/ionicons5'

const emit = defineEmits(['add-component'])

const componentList = [
  {
    type: 'input',
    label: '输入框',
    field: 'input_',
    placeholder: '请输入',
    icon: TextOutline,
    defaultValue: '',
    helpText: '',
    disabled: false
  },
  {
    type: 'textarea',
    label: '文本域',
    field: 'textarea_',
    placeholder: '请输入',
    icon: DocumentTextOutline,
    defaultValue: '',
    helpText: '',
    disabled: false
  },
  {
    type: 'select',
    label: '下拉框',
    field: 'select_',
    placeholder: '请选择',
    icon: ListOutline,
    options: [
      { label: '选项一', value: 'option1' },
      { label: '选项二', value: 'option2' }
    ],
    defaultValue: null,
    helpText: '',
    disabled: false
  },
  {
    type: 'radio',
    label: '单选框',
    field: 'radio_',
    icon: RadioButtonOnOutline,
    options: [
      { label: '选项一', value: 'option1' },
      { label: '选项二', value: 'option2' }
    ],
    defaultValue: null,
    helpText: '',
    disabled: false
  },
  {
    type: 'checkbox',
    label: '多选框',
    field: 'checkbox_',
    icon: CheckmarkCircleOutline,
    options: [
      { label: '选项一', value: 'option1' },
      { label: '选项二', value: 'option2' }
    ],
    defaultValue: [],
    helpText: '',
    disabled: false
  },
  {
    type: 'date',
    label: '日期',
    field: 'date_',
    placeholder: '请选择日期',
    icon: CalendarOutline,
    defaultValue: null,
    helpText: '',
    disabled: false
  },
  {
    type: 'time',
    label: '时间',
    field: 'time_',
    placeholder: '请选择时间',
    icon: TimeOutline,
    defaultValue: null,
    helpText: '',
    disabled: false
  },
  {
    type: 'rating',
    label: '评分',
    field: 'rating_',
    icon: StarOutline,
    maxStars: 5,
    defaultValue: null,
    helpText: '',
    disabled: false
  },
  {
    type: 'upload',
    label: '文件上传',
    field: 'upload_',
    icon: CloudUploadOutline,
    acceptTypes: ['image/*', 'application/pdf', '.doc', '.docx'],
    maxSize: 10,
    helpText: '',
    disabled: false
  },
  {
    type: 'switch',
    label: '开关',
    field: 'switch_',
    icon: ToggleOutline,
    defaultValue: false,
    helpText: '',
    disabled: false
  }
]

function handleDragStart(event, comp) {
  event.dataTransfer.setData('application/json', JSON.stringify(comp))
  event.dataTransfer.effectAllowed = 'copy'
}
</script>

<style scoped>
.component-panel {
  width: 240px;
  background: #1e293b;
  border-right: 1px solid #334155;
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
  padding: 16px;
}

.category-title {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.component-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.component-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 8px;
  background: #0f172a;
  border: 1px solid #334155;
  border-radius: 8px;
  cursor: grab;
  transition: all 0.2s ease;
}

.component-item:hover {
  border-color: #6366f1;
  background: rgba(99, 102, 241, 0.1);
  transform: translateY(-2px);
}

.component-item:active {
  cursor: grabbing;
}

.component-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(99, 102, 241, 0.15);
  border-radius: 8px;
  color: #818cf8;
}

.component-name {
  font-size: 12px;
  color: #cbd5e1;
}
</style>
