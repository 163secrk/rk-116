<template>
  <div class="template-manager">
    <div class="manager-layout">
      <aside class="tag-sidebar">
        <div class="sidebar-header">
          <span class="sidebar-title">标签分类</span>
        </div>
        <div class="tag-list">
          <div
            :class="['tag-item', { active: selectedTag === '' }]"
            @click="selectedTag = ''"
          >
            <n-icon><GridOutline /></n-icon>
            <span>全部模板</span>
            <span class="tag-count">{{ templates.length }}</span>
          </div>
          <div
            v-for="tag in allTags"
            :key="tag"
            :class="['tag-item', { active: selectedTag === tag }]"
            @click="selectedTag = tag"
          >
            <n-icon><PricetagOutline /></n-icon>
            <span>{{ tag }}</span>
            <span class="tag-count">{{ getTagCount(tag) }}</span>
          </div>
        </div>
      </aside>

      <div class="main-content-area">
        <div class="manager-header">
          <div class="header-left">
            <h2 class="manager-title">我的模板</h2>
            <n-tag v-if="selectedTag" closable @close="selectedTag = ''" type="info">
              标签: {{ selectedTag }}
            </n-tag>
          </div>
          <div class="header-right">
            <n-space>
              <n-input
                v-model:value="searchKeyword"
                placeholder="搜索模板名称..."
                clearable
                size="small"
                style="width: 240px"
              >
                <template #prefix>
                  <n-icon><SearchOutline /></n-icon>
                </template>
              </n-input>
              <n-select
                v-model:value="sortBy"
                :options="sortOptions"
                size="small"
                style="width: 160px"
              />
              <n-button
                v-if="!isBatchMode"
                size="small"
                @click="isBatchMode = true"
              >
                <template #icon>
                  <n-icon><CheckboxOutline /></n-icon>
                </template>
                批量管理
              </n-button>
              <n-button
                v-else
                size="small"
                type="error"
                :disabled="selectedIds.length === 0"
                @click="handleBatchDelete"
              >
                <template #icon>
                  <n-icon><TrashOutline /></n-icon>
                </template>
                删除选中 ({{ selectedIds.length }})
              </n-button>
              <n-button
                v-if="isBatchMode"
                size="small"
                quaternary
                @click="cancelBatchMode"
              >
                取消
              </n-button>
              <n-button type="primary" size="small" @click="handleCreateNew">
                <template #icon>
                  <n-icon><AddOutline /></n-icon>
                </template>
                新建模板
              </n-button>
            </n-space>
          </div>
        </div>

        <div v-if="loading" class="loading-container">
          <n-spin size="large" />
        </div>

        <n-empty v-else-if="filteredTemplates.length === 0" description="暂无符合条件的模板">
          <n-button type="primary" size="small" @click="handleCreateNew">去搭建</n-button>
        </n-empty>

        <n-grid v-else :cols="responsiveCols" :x-gap="20" :y-gap="20">
          <n-gi v-for="template in filteredTemplates" :key="template.id">
            <n-card hoverable :class="['template-card', { selected: isBatchMode && selectedIds.includes(template.id) }]">
              <template #header>
                <div class="card-header">
                  <div class="card-header-left">
                    <n-checkbox
                      v-if="isBatchMode"
                      :checked="selectedIds.includes(template.id)"
                      @update:checked="(val) => toggleSelect(template.id, val)"
                    />
                    <span class="template-name" :title="template.name">{{ template.name }}</span>
                  </div>
                </div>
              </template>

              <div class="card-content">
                <div v-if="getTemplateTags(template).length > 0" class="tag-row">
                  <n-tag
                    v-for="tag in getTemplateTags(template).slice(0, 3)"
                    :key="tag"
                    size="small"
                    type="info"
                    style="margin-right: 4px; margin-bottom: 4px"
                  >
                    {{ tag }}
                  </n-tag>
                  <span v-if="getTemplateTags(template).length > 3" class="more-tags">
                    +{{ getTemplateTags(template).length - 3 }}
                  </span>
                </div>
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
                    <n-button size="small" @click="handleEditTags(template)">
                      <template #icon>
                        <n-icon><PricetagOutline /></n-icon>
                      </template>
                      标签
                    </n-button>
                  </n-space>
                  <n-button v-if="!isBatchMode" size="small" type="error" quaternary @click="handleDelete(template)">
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
      </div>
    </div>

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

    <n-modal
      v-model:show="showBatchDeleteModal"
      preset="dialog"
      title="批量删除确认"
      positive-text="删除选中"
      negative-text="取消"
      positive-button-props="{ type: 'error' }"
      @positive-click="confirmBatchDelete"
      @negative-click="showBatchDeleteModal = false"
    >
      确定要删除选中的 {{ selectedIds.length }} 个模板吗？此操作不可恢复。
    </n-modal>

    <n-modal
      v-model:show="showTagModal"
      preset="card"
      title="编辑标签"
      :style="{ width: '480px' }"
    >
      <div class="tag-modal-content">
        <div class="tag-section">
          <div class="section-title">已有标签</div>
          <div class="tag-picker">
            <n-tag
              v-for="tag in allTags"
              :key="tag"
              :type="currentEditTags.includes(tag) ? 'primary' : 'default'"
              :checkable="true"
              :checked="currentEditTags.includes(tag)"
              style="margin-right: 8px; margin-bottom: 8px; cursor: pointer"
              @click="toggleTag(tag)"
            >
              {{ tag }}
            </n-tag>
          </div>
        </div>
        <div class="tag-section">
          <div class="section-title">新建标签</div>
          <n-space>
            <n-input
              v-model:value="newTagInput"
              placeholder="输入新标签名称"
              size="small"
              @keyup.enter="addNewTag"
            />
            <n-button size="small" type="primary" @click="addNewTag">
              <template #icon>
                <n-icon><AddOutline /></n-icon>
              </template>
              添加
            </n-button>
          </n-space>
        </div>
        <div class="tag-section">
          <div class="section-title">已选标签</div>
          <div class="selected-tags">
            <n-tag
              v-for="tag in currentEditTags"
              :key="tag"
              closable
              type="primary"
              style="margin-right: 8px; margin-bottom: 8px"
              @close="removeTag(tag)"
            >
              {{ tag }}
            </n-tag>
            <span v-if="currentEditTags.length === 0" class="empty-tags">暂无标签</span>
          </div>
        </div>
      </div>
      <template #footer>
        <n-space justify="end">
          <n-button size="small" @click="showTagModal = false">取消</n-button>
          <n-button size="small" type="primary" @click="saveTags">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import {
  NCard, NGrid, NGi, NSpace, NButton, NIcon, NEmpty, NSpin,
  NModal, NInput, NSelect, NCheckbox, NTag, useMessage, useDialog
} from 'naive-ui'
import {
  AddOutline, CreateOutline, CopyOutline, TrashOutline,
  SearchOutline, CheckboxOutline, GridOutline, PricetagOutline
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
const searchKeyword = ref('')
const sortBy = ref('updatedAt_desc')
const selectedTag = ref('')
const isBatchMode = ref(false)
const selectedIds = ref([])

const showSaveAsModal = ref(false)
const showDeleteModal = ref(false)
const showBatchDeleteModal = ref(false)
const showTagModal = ref(false)

const newTemplateName = ref('')
const templateToDelete = ref(null)
const copySourceTemplate = ref(null)
const currentEditTemplate = ref(null)
const currentEditTags = ref([])
const newTagInput = ref('')

const sortOptions = [
  { label: '修改时间（新→旧）', value: 'updatedAt_desc' },
  { label: '修改时间（旧→新）', value: 'updatedAt_asc' },
  { label: '创建时间（新→旧）', value: 'createdAt_desc' },
  { label: '创建时间（旧→新）', value: 'createdAt_asc' },
  { label: '组件数（多→少）', value: 'components_desc' },
  { label: '组件数（少→多）', value: 'components_asc' }
]

const allTags = computed(() => {
  const tagSet = new Set()
  templates.value.forEach(t => {
    const tags = parseTags(t.tags)
    tags.forEach(tag => tagSet.add(tag))
  })
  return Array.from(tagSet).sort()
})

const filteredTemplates = computed(() => {
  let result = [...templates.value]

  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.trim().toLowerCase()
    result = result.filter(t => t.name.toLowerCase().includes(kw))
  }

  if (selectedTag.value) {
    result = result.filter(t => {
      const tags = parseTags(t.tags)
      return tags.includes(selectedTag.value)
    })
  }

  const [field, order] = sortBy.value.split('_')
  result.sort((a, b) => {
    let valA, valB
    if (field === 'components') {
      valA = getComponentCount(a.schemaData)
      valB = getComponentCount(b.schemaData)
    } else {
      valA = new Date(a[field]).getTime()
      valB = new Date(b[field]).getTime()
    }
    return order === 'desc' ? valB - valA : valA - valB
  })

  return result
})

const responsiveCols = computed(() => {
  if (typeof window !== 'undefined') {
    const width = window.innerWidth
    if (width < 768) return 1
    if (width < 1200) return 2
    if (width < 1600) return 3
    return 3
  }
  return 3
})

function parseTags(tagsStr) {
  if (!tagsStr) return []
  try {
    const arr = JSON.parse(tagsStr)
    return Array.isArray(arr) ? arr : []
  } catch {
    return tagsStr.split(',').map(s => s.trim()).filter(Boolean)
  }
}

function getTemplateTags(template) {
  return parseTags(template.tags)
}

function getTagCount(tag) {
  return templates.value.filter(t => {
    const tags = parseTags(t.tags)
    return tags.includes(tag)
  }).length
}

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
      templates.value = res.data.data || []
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

function toggleSelect(id, checked) {
  if (checked) {
    if (!selectedIds.value.includes(id)) {
      selectedIds.value.push(id)
    }
  } else {
    selectedIds.value = selectedIds.value.filter(i => i !== id)
  }
}

function cancelBatchMode() {
  isBatchMode.value = false
  selectedIds.value = []
}

function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    message.warning('请先选择要删除的模板')
    return
  }
  showBatchDeleteModal.value = true
}

async function confirmBatchDelete() {
  if (selectedIds.value.length === 0) return
  try {
    const res = await axios.delete('/api/forms/batch', { data: selectedIds.value })
    if (res.data.success) {
      message.success(`成功删除 ${selectedIds.value.length} 个模板`)
      showBatchDeleteModal.value = false
      cancelBatchMode()
      await loadTemplates()
    } else {
      message.error('删除失败')
    }
  } catch (e) {
    message.error('删除失败：' + e.message)
  }
}

function handleEditTags(template) {
  currentEditTemplate.value = template
  currentEditTags.value = [...getTemplateTags(template)]
  newTagInput.value = ''
  showTagModal.value = true
}

function toggleTag(tag) {
  const idx = currentEditTags.value.indexOf(tag)
  if (idx > -1) {
    currentEditTags.value.splice(idx, 1)
  } else {
    currentEditTags.value.push(tag)
  }
}

function addNewTag() {
  const tag = newTagInput.value.trim()
  if (!tag) {
    message.warning('请输入标签名称')
    return
  }
  if (currentEditTags.value.includes(tag)) {
    message.warning('该标签已存在')
    return
  }
  currentEditTags.value.push(tag)
  newTagInput.value = ''
}

function removeTag(tag) {
  const idx = currentEditTags.value.indexOf(tag)
  if (idx > -1) {
    currentEditTags.value.splice(idx, 1)
  }
}

async function saveTags() {
  if (!currentEditTemplate.value) return
  try {
    const tagsStr = JSON.stringify(currentEditTags.value)
    const res = await axios.put(`/api/forms/${currentEditTemplate.value.id}`, {
      name: currentEditTemplate.value.name,
      schemaData: currentEditTemplate.value.schemaData,
      tags: tagsStr
    })
    if (res.data.success) {
      message.success('标签保存成功')
      showTagModal.value = false
      currentEditTemplate.value = null
      currentEditTags.value = []
      await loadTemplates()
    } else {
      message.error('保存失败')
    }
  } catch (e) {
    message.error('保存失败：' + e.message)
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
  display: flex;
  overflow: hidden;
  background: #0f172a;
}

.manager-layout {
  display: flex;
  width: 100%;
  height: 100%;
}

.tag-sidebar {
  width: 220px;
  background: #1e293b;
  border-right: 1px solid #334155;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.sidebar-header {
  padding: 16px 20px;
  border-bottom: 1px solid #334155;
}

.sidebar-title {
  font-size: 14px;
  font-weight: 600;
  color: #f1f5f9;
}

.tag-list {
  padding: 8px 0;
  flex: 1;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  color: #cbd5e1;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.tag-item:hover {
  background: rgba(99, 102, 241, 0.1);
  color: #e2e8f0;
}

.tag-item.active {
  background: rgba(99, 102, 241, 0.15);
  color: #818cf8;
  border-right: 3px solid #6366f1;
}

.tag-item span:not(.tag-count) {
  flex: 1;
}

.tag-count {
  font-size: 12px;
  color: #94a3b8;
  background: rgba(51, 65, 85, 0.5);
  padding: 2px 8px;
  border-radius: 10px;
}

.tag-item.active .tag-count {
  background: rgba(99, 102, 241, 0.3);
  color: #c7d2fe;
}

.main-content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 24px;
}

.manager-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.manager-title {
  font-size: 22px;
  font-weight: 600;
  color: #f1f5f9;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
  flex: 1;
}

.template-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.2s;
}

.template-card.selected {
  box-shadow: 0 0 0 2px #6366f1;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.card-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
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

.tag-row {
  margin-bottom: 12px;
  min-height: 28px;
}

.more-tags {
  font-size: 12px;
  color: #94a3b8;
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

.tag-modal-content {
  padding: 8px 0;
}

.tag-section {
  margin-bottom: 20px;
}

.tag-section:last-of-type {
  margin-bottom: 0;
}

.section-title {
  font-size: 13px;
  font-weight: 500;
  color: #94a3b8;
  margin-bottom: 10px;
}

.tag-picker {
  display: flex;
  flex-wrap: wrap;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  min-height: 32px;
}

.empty-tags {
  font-size: 13px;
  color: #64748b;
  font-style: italic;
}
</style>
