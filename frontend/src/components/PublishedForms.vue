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
    />

    <n-modal v-model:show="showOfflineModal" preset="dialog" title="下线确认" positive-text="确认下线" negative-text="取消" type="warning" @positive-click="confirmOffline">
      <div>确定要下线此表单吗？下线后用户将无法通过链接访问该表单。</div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
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

const columns = [
  {
    title: '表单名',
    key: 'formName',
    width: 200,
    render: (row) => h('span', { style: 'color: #e2e8f0; font-weight: 500' }, row.formName)
  },
  {
    title: '分享链接',
    key: 'token',
    width: 340,
    render: (row) =>
      h('div', { style: 'display: flex; align-items: center; gap: 8px' }, [
        h('span', {
          style: 'color: #94a3b8; font-size: 12px; font-family: monospace; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 240px',
          title: getShareLink(row.token)
        }, getShareLink(row.token)),
        h(NButton, {
          text: true, size: 'tiny', onClick: () => copyLink(row.token)
        }, {
          icon: () => h(NIcon, null, { default: () => h(CopyOutline) }),
          default: () => '复制'
        })
      ])
  },
  {
    title: '发布时间',
    key: 'publishedAt',
    width: 180,
    render: (row) => h('span', { style: 'color: #94a3b8; font-size: 13px' }, formatTime(row.publishedAt))
  },
  {
    title: '填写次数',
    key: 'submitCount',
    width: 100,
    render: (row) => h('span', { style: 'color: #cbd5e1' }, row.submitCount || 0)
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(NTag, {
        type: row.status === 'PUBLISHED' ? 'success' : 'default',
        size: 'small',
        round: true
      }, { default: () => row.status === 'PUBLISHED' ? '已发布' : '已下线' })
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    render: (row) =>
      h(NSpace, null, {
        default: () => [
          h(NButton, {
            text: true, type: 'primary', size: 'tiny',
            onClick: () => openLink(row.token)
          }, {
            icon: () => h(NIcon, null, { default: () => h(OpenOutline) }),
            default: () => '打开'
          }),
          row.status === 'PUBLISHED'
            ? h(NButton, {
                text: true, type: 'warning', size: 'tiny',
                onClick: () => handleOffline(row)
              }, {
                icon: () => h(NIcon, null, { default: () => h(CloudOfflineOutline) }),
                default: () => '下线'
              })
            : null
        ]
      })
  }
]

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
</style>
