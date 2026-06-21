<template>
  <div class="template-manager">
    <div class="manager-header">
      <h2 class="manager-title">我的模板</h2>
      <n-button type="primary" size="small" @click="handleCreateNew">
        <template #icon>
          <n-icon><AddOutline /></n-icon>
        </template>
        新建模板
      </n-button>
    </div>

    <div v-if="loading" class="loading-container">
      <n-spin size="large" />
    </div>

    <n-empty v-else-if="templates.length === 0" description="暂无模板，快去搭建一个吧！">
      <n-button type="primary" size="small" @click="handleCreateNew">去搭建</n-button>
    </n-empty>

    <n-grid v-else :cols="responsiveCols" :x-gap="20" :y-gap="20">
      <n-gi v-for="template in templates" :key="template.id">
        <n-card hoverable class="template-card">
          <template #header>
            <div class="card-header">
              <span class="template-name" :title="template.name">{{ template.name }}</span>
            </div>
          </template>

          <div class="card-content">
            <div class="info-item">
              <span class="info-label">组件数</span>
              <span class="info-value">{{ getComponentCount(template.schemaData) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ formatDate(template.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">修改时间</span>
              <span class="info-value">{{ formatDate(template.updatedAt) }}</span>
            </div>
          </div>

          <template #footer>
            <n-space justify="space-between">
              <n-space>
                <n-button size="small" type="primary" @click="handleEdit(template)">
                  <template #icon>
                    <n-icon><CreateOutline /></n-icon>
                  </template>
                  编辑
                </n-button>
                <n-button size="small" @click="handleCopy(template)">
                  <template #icon>
                    <n-icon><CopyOutline /></n-icon>
                  </template>
                  复制
                </n-button>
              </n-space>
              <n-button size="small" type="error" quaternary @click="handleDelete(template)">
                <template #icon>
                  <n-icon><TrashOutline /></n-icon>
                </template>
                删除
              </n-button>
            </n-space>
          </template>
        </n-card>
      </n-gi>
    </n-grid>

    <n-modal
      v-model:show="showSaveAsModal"
      preset="dialog"
      title="另存为模板"
      positive-text="确定"
      negative-text="取消"
      @positive-click="handleSaveAsConfirm"
      @negative-click="showSaveAsModal = false"
    >
      <div style="margin-bottom: 12px">请输入新模板名称：</div>
      <n-input
        v-model:value="newTemplateName"
        placeholder="模板名称"
        @keyup.enter="handleSaveAsConfirm"
      />
    </n-modal>

    <n-modal
      v-model:show="showDeleteModal"
      preset="dialog"
      title="确认删除"
      positive-text="删除"
      negative-text="取消"
      positive-button-props="{ type: 'error' }"
      @positive-click="confirmDelete"
      @negative-click="showDeleteModal = false"
    >
      确定要删除模板「{{ templateToDelete?.name }}」吗？此操作不可恢复。
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import {
  NCard, NGrid, NGi, NSpace, NButton, NIcon, NEmpty, NSpin,
  NModal, NInput, useMessage, useDialog
} from 'naive-ui'
import {
  AddOutline, CreateOutline, CopyOutline, TrashOutline
} from '@vicons/ionicons5'
import axios from 'axios'

const props = defineProps({
  currentSchemaList: {
    type: Array,
    default: () => []
  },
  currentFormName: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['edit-template', 'create-new'])

const message = useMessage()
const dialog = useDialog()

const templates = ref([])
const loading = ref(false)
const showSaveAsModal = ref(false)
const showDeleteModal = ref(false)
const newTemplateName = ref('')
const templateToDelete = ref(null)
const copySourceTemplate = ref(null)

const responsiveCols = computed(() => {
  if (typeof window !== 'undefined') {
    const width = window.innerWidth
    if (width < 768) return 1
    if (width < 1200) return 2
    if (width < 1600) return 3
    return 4
  }
  return 3
})

function countComponents(schemaArray) {
  let count = 0
  for (const item of schemaArray) {
    count++
    if (item.children && item.children.length > 0) {
      count += countComponents(item.children)
    }
  }
  return count
}

function getComponentCount(schemaData) {
  try {
    const arr = JSON.parse(schemaData || '[]')
    return countComponents(arr)
  } catch {
    return 0
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${hh}:${mm}`
}

async function loadTemplates() {
  loading.value = true
  try {
    const res = await axios.get('/api/forms')
    if (res.data.success) {
      templates.value = (res.data.data || []).sort((a, b) => {
        return new Date(b.updatedAt) - new Date(a.updatedAt)
      })
    }
  } catch (e) {
    message.error('加载模板列表失败')
  } finally {
    loading.value = false
  }
}

function handleCreateNew() {
  emit('create-new')
}

function handleEdit(template) {
  emit('edit-template', template)
}

function handleCopy(template) {
  copySourceTemplate.value = template
  newTemplateName.value = template.name + ' 副本'
  showSaveAsModal.value = true
}

async function handleSaveAsConfirm() {
  if (!newTemplateName.value.trim()) {
    message.warning('请输入模板名称')
    return
  }

  let schemaData = ''
  if (copySourceTemplate.value) {
    schemaData = copySourceTemplate.value.schemaData
  } else {
    schemaData = JSON.stringify(props.currentSchemaList || [])
  }

  try {
    const payload = {
      name: newTemplateName.value.trim(),
      schemaData
    }
    const res = await axios.post('/api/forms', payload)
    if (res.data.success) {
      message.success('保存成功')
      showSaveAsModal.value = false
      newTemplateName.value = ''
      copySourceTemplate.value = null
      await loadTemplates()
    } else {
      message.error('保存失败')
    }
  } catch (e) {
    message.error('保存失败：' + e.message)
  }
}

function handleDelete(template) {
  templateToDelete.value = template
  showDeleteModal.value = true
}

async function confirmDelete() {
  if (!templateToDelete.value) return
  try {
    const res = await axios.delete(`/api/forms/${templateToDelete.value.id}`)
    if (res.data.success) {
      message.success('删除成功')
      showDeleteModal.value = false
      templateToDelete.value = null
      await loadTemplates()
    } else {
      message.error('删除失败')
    }
  } catch (e) {
    message.error('删除失败：' + e.message)
  }
}

function openSaveAsFromBuilder() {
  copySourceTemplate.value = null
  newTemplateName.value = (props.currentFormName || '未命名模板') + ' 副本'
  showSaveAsModal.value = true
}

defineExpose({
  loadTemplates,
  openSaveAsFromBuilder
})

onMounted(() => {
  loadTemplates()
})
</script>

<style scoped>
.template-manager {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #0f172a;
}

.manager-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.manager-title {
  font-size: 22px;
  font-weight: 600;
  color: #f1f5f9;
  margin: 0;
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.template-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.template-name {
  font-size: 16px;
  font-weight: 600;
  color: #f1f5f9;
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-content {
  flex: 1;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid rgba(51, 65, 85, 0.5);
}

.info-item:last-of-type {
  border-bottom: none;
}

.info-label {
  font-size: 13px;
  color: #94a3b8;
}

.info-value {
  font-size: 13px;
  color: #cbd5e1;
  font-weight: 500;
}
</style>
