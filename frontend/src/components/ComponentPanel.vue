<template>
  <aside class="component-panel">
    <div class="panel-header">
      <n-icon size="18"><ConstructOutline /></n-icon>
      <span>组件面板</span>
    </div>
    <div class="panel-content">
      <div
        v-for="category in categories"
        :key="category.name"
        class="category-section"
      >
        <div
          class="category-header"
          @click="toggleCategory(category.name)"
        >
          <n-icon size="16" class="collapse-icon" :class="{ expanded: expandedCategories.includes(category.name) }">
            <ChevronForwardOutline />
          </n-icon>
          <span class="category-title">{{ category.label }}</span>
        </div>
        <div v-show="expandedCategories.includes(category.name)" class="category-body">
          <div class="component-grid">
            <div
              v-for="comp in category.components"
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
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref } from 'vue'
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
  ToggleOutline,
  ChevronForwardOutline,
  GridOutline
} from '@vicons/ionicons5'

const emit = defineEmits(['add-component'])

const expandedCategories = ref(['basic', 'selection', 'advanced', 'layout'])

const categories = [
  {
    name: 'basic',
    label: '基础组件',
    components: [
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
        type: 'switch',
        label: '开关',
        field: 'switch_',
        icon: ToggleOutline,
        defaultValue: false,
        helpText: '',
        disabled: false
      }
    ]
  },
  {
    name: 'selection',
    label: '选择组件',
    components: [
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
      }
    ]
  },
  {
    name: 'advanced',
    label: '高级组件',
    components: [
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
      }
    ]
  },
  {
    name: 'layout',
    label: '布局容器',
    components: [
      {
        type: 'container',
        label: '布局容器',
        field: 'container_',
        icon: GridOutline,
        columns: 2,
        children: [],
        helpText: '',
        disabled: false
      }
    ]
  }
]

function toggleCategory(name) {
  const index = expandedCategories.value.indexOf(name)
  if (index > -1) {
    expandedCategories.value.splice(index, 1)
  } else {
    expandedCategories.value.push(name)
  }
}

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
  flex-shrink: 0;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
}

.category-section {
  border-bottom: 1px solid #334155;
}

.category-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  cursor: pointer;
  user-select: none;
  transition: background 0.2s ease;
}

.category-header:hover {
  background: rgba(99, 102, 241, 0.05);
}

.collapse-icon {
  color: #64748b;
  transition: transform 0.2s ease;
  flex-shrink: 0;
}

.collapse-icon.expanded {
  transform: rotate(90deg);
}

.category-title {
  font-size: 12px;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 600;
}

.category-body {
  padding: 0 16px 16px 16px;
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
