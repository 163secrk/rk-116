<template>
  <div class="published-container">
    <div class="published-header">
      <h2 class="page-title">已发布表单</h2>
      <n-button :loading="loading" @click="loadPublishedForms" size="small">
        <template #icon>
          <n-icon><RefreshOutline /></n-icon>
        </template>
        刷新
      </n-button>
    </div>

    <div v-if="loading" class="loading-wrap">
      <n-spin size="large" />
    </div>

    <n-empty v-else-if="publishedForms.length === 0" description="暂无已发布的表单" />

    <n-data-table
      v-else
      :columns="columns"
      :data="publishedForms"
      :pagination="pagination"
      :row-key="(row) => row.id"
      bordered
      striped
      size="small"
    >
      <template #body="{ row }">
        <tr>
          <td class="col-name">{{ row.formName }}</td>
          <td class="col-link">
            <div class="link-wrap">
              <span class="link-text">{{ getShareLink(row.token) }}</span>
              <n-button text size="tiny" @click="copyLink(row.token)">
                <template #icon>
                  <n-icon><CopyOutline /></n-icon>
                </template>
                复制
              </n-button>
            </div>
          </td>
          <td class="col-time">{{ formatTime(row.publishedAt) }}</td>
          <td class="col-count">{{ row.submitCount || 0 }}</td>
          <td class="col-status">
            <n-tag :type="row.status === 'PUBLISHED' ? 'success' : 'default'" size="small" round>
              {{ row.status === 'PUBLISHED' ? '已发布' : '已下线' }}
            </n-tag>
          </td>
          <td class="col-action">
            <n-space>
              <n-button
                text
                type="primary"
                size="tiny"
                @click="openLink(row.token)"
              >
                <template #icon>
                  <n-icon><OpenOutline /></n-icon>
                </template>
                打开
              </n-button>
              <n-button
                v-if="row.status === 'PUBLISHED'"
                text
                type="warning"
                size="tiny"
                @click="handleOffline(row)"
              >
                <template #icon>
                  <n-icon><CloudOfflineOutline /></n-icon>
                </template>
                下线
              </n-button>
            </n-space>
          </td>
        </tr>
      </template>
    </n-data-table>

    <n-modal v-model:show="showOfflineModal" preset="dialog" title="下线确认" positive-text="确认下线" negative-text="取消" type="warning" @positive-click="confirmOffline">
      <div>确定要下线此表单吗？下线后用户将无法通过链接访问该表单。</div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import {
  NButton, NIcon, NSpin, NEmpty, NDataTable, NSpace, NTag, NModal, createDiscreteApi
} from 'naive-ui'
import {
  RefreshOutline, CopyOutline, OpenOutline, CloudOfflineOutline
} from '@vicons/ionicons5'
import axios from 'axios'

const { message } = createDiscreteApi(['message'])

const loading = ref(false)
const publishedForms = ref([])
const showOfflineModal = ref(false)
const offlineTarget = ref(null)

const pagination = {
  pageSize: 10
}

const columns = [
  { title: '表单名', key: 'formName', width: 200 },
  { title: '分享链接', key: 'token', width: 320 },
  { title: '发布时间', key: 'publishedAt', width: 180 },
  { title: '填写次数', key: 'submitCount', width: 100 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 160 }
]

function getShareLink(token) {
  return `${window.location.origin}${window.location.pathname}#/fill/${token}`
}

function formatTime(time) {
  if (!time) return '-'
  const d = new Date(time)
  if (isNaN(d.getTime())) return time
  const pad = (n) => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function loadPublishedForms() {
  loading.value = true
  try {
    const res = await axios.get('/api/published-forms')
    if (res.data.success) {
      publishedForms.value = res.data.data || []
    } else {
      message.error(res.data.message || '加载失败')
    }
  } catch (e) {
    message.error('加载失败：' + e.message)
  } finally {
    loading.value = false
  }
}

async function copyLink(token) {
  const link = getShareLink(token)
  try {
    await navigator.clipboard.writeText(link)
    message.success('链接已复制')
  } catch (e) {
    const textarea = document.createElement('textarea')
    textarea.value = link
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    message.success('链接已复制')
  }
}

function openLink(token) {
  const link = getShareLink(token)
  window.open(link, '_blank')
}

function handleOffline(row) {
  offlineTarget.value = row
  showOfflineModal.value = true
}

async function confirmOffline() {
  if (!offlineTarget.value) return
  try {
    const res = await axios.put(`/api/published-forms/offline/${offlineTarget.value.id}`)
    if (res.data.success) {
      message.success('已下线')
      showOfflineModal.value = false
      offlineTarget.value = null
      loadPublishedForms()
    } else {
      message.error(res.data.message || '下线失败')
    }
  } catch (e) {
    message.error('下线失败：' + e.message)
  }
}

onMounted(() => {
  loadPublishedForms()
})

defineExpose({ loadPublishedForms })
</script>

<style scoped>
.published-container {
  flex: 1;
  padding: 24px;
  overflow: auto;
  background: #0f172a;
}

.published-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #f1f5f9;
  margin: 0;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}

.link-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.link-text {
  flex: 1;
  color: #94a3b8;
  font-size: 12px;
  font-family: monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 240px;
}

.col-name {
  color: #e2e8f0;
  font-weight: 500;
}

.col-link {
  font-family: monospace;
}

.col-time {
  color: #94a3b8;
  font-size: 13px;
}

.col-count {
  color: #cbd5e1;
}
</style>
