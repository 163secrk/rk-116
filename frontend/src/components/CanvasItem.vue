<template>
  <div
    class="canvas-item"
    :class="{ active: selectedId === element.id, 'is-container': element.type === 'container' }"
    @click.stop="handleSelect"
  >
    <div class="item-header">
      <div class="item-type-tag">
        <n-icon size="14"><ReorderThreeOutline /></n-icon>
        <span>{{ getTypeName(element.type) }}</span>
      </div>
      <div class="item-actions">
        <n-button text size="tiny" type="error" @click.stop="handleRemove">
          <template #icon>
            <n-icon size="14"><TrashOutline /></n-icon>
          </template>
        </n-button>
      </div>
    </div>
    <div class="item-body">
      <div
        v-if="element.type === 'container'"
        class="container-drop-area"
        @dragover.prevent="handleContainerDragOver"
        @drop.stop="handleContainerDrop"
        @click.stop="handleSelect"
      >
        <div
          class="container-grid"
          :style="{ gridTemplateColumns: 'repeat(' + (element.columns || 2) + ', 1fr)' }"
        >
          <CanvasItem
            v-for="child in element.children || []"
            :key="child.id"
            :element="child"
            :selected-id="selectedId"
            :level="level + 1"
            class="container-child-item"
            @select="handleChildSelect"
            @remove="handleChildRemove"
            @add-to-container="handleChildAddToContainer"
          />
          <div
            v-if="!element.children || element.children.length === 0"
            class="container-empty-tip"
          >
            <n-icon size="24"><AddCircleOutline /></n-icon>
            <span>拖拽组件到此处</span>
          </div>
        </div>
      </div>
      <FormItemRenderer v-else :component="element" />
    </div>
  </div>
</template>

<script setup>
import { NIcon, NButton } from 'naive-ui'
import {
  ReorderThreeOutline,
  TrashOutline,
  AddCircleOutline
} from '@vicons/ionicons5'
import FormItemRenderer from './FormItemRenderer.vue'

defineOptions({
  name: 'CanvasItem'
})

const props = defineProps({
  element: {
    type: Object,
    required: true
  },
  selectedId: {
    type: [String, Number, null],
    default: null
  },
  level: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['select', 'remove', 'add-to-container'])

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
  switch: '开关',
  container: '布局容器'
}

function getTypeName(type) {
  return typeNameMap[type] || type
}

function handleSelect(e) {
  emit('select', props.element.id)
}

function handleRemove(e) {
  emit('remove', props.element.id)
}

function handleContainerDragOver(e) {
  e.dataTransfer.dropEffect = 'copy'
}

function handleContainerDrop(e) {
  try {
    const data = e.dataTransfer.getData('application/json')
    if (data) {
      const comp = JSON.parse(data)
      emit('add-to-container', props.element.id, comp)
    }
  } catch (err) {
    console.log('Container drop error:', err)
  }
}

function handleChildSelect(id) {
  emit('select', id)
}

function handleChildRemove(id) {
  emit('remove', id)
}

function handleChildAddToContainer(containerId, componentData) {
  emit('add-to-container', containerId, componentData)
}
</script>

<style scoped>
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

.canvas-item.is-container {
  padding: 12px;
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

.container-drop-area {
  min-height: 100px;
  padding: 12px;
  background: #0f172a;
  border: 2px dashed #334155;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.container-drop-area:hover {
  border-color: #475569;
  background: rgba(99, 102, 241, 0.05);
}

.container-grid {
  display: grid;
  gap: 12px;
}

.container-child-item {
  min-width: 0;
}

.container-empty-tip {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px 16px;
  color: #64748b;
  font-size: 13px;
}
</style>
