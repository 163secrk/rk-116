<template>
  <main class="form-canvas">
    <div class="canvas-header">
      <n-icon size="18"><ColorPaletteOutline /></n-icon>
      <span>表单画布</span>
    </div>
    <div
      class="canvas-area"
      @dragover.prevent="handleDragOver"
      @drop="handleDrop"
      @click.self="handleCanvasClick"
    >
      <div v-if="schemaList.length === 0" class="empty-tip">
        <n-icon size="48"><AddCircleOutline /></n-icon>
        <p>从左侧拖拽组件或点击组件添加到此处</p>
      </div>
      <draggable
        v-else
        v-model="list"
        item-key="id"
        class="canvas-list"
        ghost-class="ghost"
        chosen-class="chosen"
        animation="200"
        @change="onDraggableChange"
      >
        <template #item="{ element, index }">
          <div
            class="canvas-item"
            :class="{ active: selectedId === element.id }"
            @click.stop="handleSelect(element.id)"
          >
            <div class="item-header">
              <div class="item-type-tag">
                <n-icon size="14"><ReorderThreeOutline /></n-icon>
                <span>{{ getTypeName(element.type) }}</span>
              </div>
              <div class="item-actions">
                <n-button
                  text
                  size="tiny"
                  type="error"
                  @click.stop="handleRemove(element.id)"
                >
                  <template #icon>
                    <n-icon size="14"><TrashOutline /></n-icon>
                  </template>
                </n-button>
              </div>
            </div>
            <div class="item-body">
              <FormItemRenderer :component="element" />
            </div>
          </div>
        </template>
      </draggable>
    </div>
  </main>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { NIcon, NButton } from 'naive-ui'
import draggable from 'vuedraggable'
import {
  ColorPaletteOutline,
  AddCircleOutline,
  ReorderThreeOutline,
  TrashOutline
} from '@vicons/ionicons5'
import FormItemRenderer from './FormItemRenderer.vue'

const props = defineProps({
  schemaList: {
    type: Array,
    default: () => []
  },
  selectedId: {
    type: [String, Number, null],
    default: null
  }
})

const emit = defineEmits(['select-component', 'update-list', 'remove-component'])

const list = ref([...props.schemaList])
let isInternalChange = false

watch(() => props.schemaList, (newVal) => {
  if (isInternalChange) {
    isInternalChange = false
    return
  }
  list.value = JSON.parse(JSON.stringify(newVal))
}, { deep: false })

function syncToParent() {
  isInternalChange = true
  emit('update-list', JSON.parse(JSON.stringify(list.value)))
  nextTick(() => {
    isInternalChange = false
  })
}

function onDraggableChange() {
  syncToParent()
}

const typeNameMap = {
  input: '输入框',
  textarea: '文本域',
  select: '下拉框',
  radio: '单选框',
  checkbox: '多选框',
  date: '日期',
  time: '时间',
  rating: '评分',
  upload: '文件上传',
  switch: '开关'
}

function getTypeName(type) {
  return typeNameMap[type] || type
}

function handleDragOver(event) {
  event.dataTransfer.dropEffect = 'copy'
}

function handleDrop(event) {
  try {
    const data = event.dataTransfer.getData('application/json')
    if (data) {
      const comp = JSON.parse(data)
      const newItem = {
        id: Date.now() + Math.random().toString(36).substr(2, 9),
        type: comp.type,
        label: comp.label,
        field: comp.field + Date.now().toString().slice(-4),
        placeholder: comp.placeholder || '',
        options: comp.options ? JSON.parse(JSON.stringify(comp.options)) : [],
        required: false,
        maxStars: comp.maxStars || 5,
        acceptTypes: comp.acceptTypes ? JSON.parse(JSON.stringify(comp.acceptTypes)) : [],
        maxSize: comp.maxSize || 10,
        defaultValue: comp.defaultValue !== undefined ? comp.defaultValue : false,
        validation: {
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
      list.value.push(newItem)
      syncToParent()
      emit('select-component', newItem.id)
    }
  } catch (e) {
    console.log('Drop error:', e)
  }
}

function handleSelect(id) {
  emit('select-component', id)
}

function handleRemove(id) {
  emit('remove-component', id)
}

function handleCanvasClick() {
  emit('select-component', null)
}
</script>

<style scoped>
.form-canvas {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #0f172a;
  overflow: hidden;
}

.canvas-header {
  height: 48px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
  border-bottom: 1px solid #334155;
  color: #e2e8f0;
  font-weight: 600;
  font-size: 14px;
  background: #1e293b;
}

.canvas-area {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.empty-tip {
  height: 100%;
  min-height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #64748b;
  border: 2px dashed #334155;
  border-radius: 12px;
}

.empty-tip p {
  font-size: 14px;
}

.canvas-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.canvas-item {
  background: #1e293b;
  border: 2px solid #334155;
  border-radius: 10px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.canvas-item:hover {
  border-color: #475569;
}

.canvas-item.active {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.item-type-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #94a3b8;
  padding: 4px 8px;
  background: #0f172a;
  border-radius: 4px;
}

.item-actions {
  display: flex;
  gap: 4px;
}

.item-body {
  padding: 0 4px;
}

.ghost {
  opacity: 0.5;
  background: #6366f1 !important;
  border-color: #6366f1 !important;
}

.chosen {
  opacity: 0.9;
}
</style>
