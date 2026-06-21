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
      <div v-else class="canvas-list">
        <CanvasItem
          v-for="element in schemaList"
          :key="element.id"
          :element="element"
          :selected-id="selectedId"
          :level="0"
          @select="handleSelect"
          @remove="handleRemove"
          @add-to-container="handleAddToContainer"
        />
      </div>
    </div>
  </main>
</template>

<script setup>
import { NIcon } from 'naive-ui'
import {
  ColorPaletteOutline,
  AddCircleOutline
} from '@vicons/ionicons5'
import CanvasItem from './CanvasItem.vue'

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

const emit = defineEmits(['select-component', 'add-component', 'remove-component', 'add-to-container'])

function handleDragOver(event) {
  event.dataTransfer.dropEffect = 'copy'
}

function handleDrop(event) {
  try {
    const data = event.dataTransfer.getData('application/json')
    if (data) {
      const comp = JSON.parse(data)
      emit('add-component', comp)
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

function handleAddToContainer(containerId, componentData) {
  emit('add-to-container', containerId, componentData)
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
</style>
