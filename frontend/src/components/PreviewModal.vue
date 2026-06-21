<template>
  <n-modal v-model:show="showModal" preset="card" :title="formName || '表单预览'" style="width: 720px; max-width: 90vw;">
    <div class="preview-content">
      <div v-if="schemaList.length === 0" class="empty-tip">
        <n-icon size="40"><AlertCircleOutline /></n-icon>
        <p>暂无表单内容，请先添加组件</p>
      </div>
      <n-form v-else :model="formData" label-placement="left" label-width="100px">
        <div v-for="comp in schemaList" :key="comp.id" class="preview-item">
          <n-form-item :label="comp.label" :required="comp.required">
            <n-input
              v-if="comp.type === 'input'"
              v-model:value="formData[comp.field]"
              :placeholder="comp.placeholder"
            />
            <n-input
              v-else-if="comp.type === 'textarea'"
              v-model:value="formData[comp.field]"
              type="textarea"
              :placeholder="comp.placeholder"
              :rows="3"
            />
            <n-select
              v-else-if="comp.type === 'select'"
              v-model:value="formData[comp.field]"
              :options="comp.options"
              :placeholder="comp.placeholder"
            />
            <n-radio-group v-else-if="comp.type === 'radio'" v-model:value="formData[comp.field]">
              <n-space>
                <n-radio v-for="opt in comp.options" :key="opt.value" :value="opt.value">
                  {{ opt.label }}
                </n-radio>
              </n-space>
            </n-radio-group>
            <n-checkbox-group v-else-if="comp.type === 'checkbox'" v-model:value="formData[comp.field]">
              <n-space>
                <n-checkbox v-for="opt in comp.options" :key="opt.value" :value="opt.value">
                  {{ opt.label }}
                </n-checkbox>
              </n-space>
            </n-checkbox-group>
            <n-date-picker
              v-else-if="comp.type === 'date'"
              v-model:value="formData[comp.field]"
              type="date"
            />
            <n-time-picker
              v-else-if="comp.type === 'time'"
              v-model:value="formData[comp.field]"
            />
          </n-form-item>
        </div>
      </n-form>
    </div>
    <template #footer>
      <div class="preview-footer">
        <n-space>
          <n-button @click="showModal = false">关闭</n-button>
          <n-button type="primary" @click="handleSubmit">提交</n-button>
        </n-space>
      </div>
    </template>
  </n-modal>
</template>

<script setup>
import { ref, watch, reactive, computed } from 'vue'
import { NModal, NForm, NFormItem, NInput, NSelect, NRadioGroup, NRadio, NCheckboxGroup, NCheckbox, NSpace, NButton, NIcon, createDiscreteApi } from 'naive-ui'
import { AlertCircleOutline } from '@vicons/ionicons5'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  schemaList: {
    type: Array,
    default: () => []
  },
  formName: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:show'])
const { message } = createDiscreteApi(['message'])

const showModal = computed({
  get: () => props.show,
  set: (val) => emit('update:show', val)
})

const formData = reactive({})

watch(() => props.schemaList, (list) => {
  Object.keys(formData).forEach(key => delete formData[key])
  list.forEach(item => {
    if (item.type === 'checkbox') {
      formData[item.field] = []
    } else {
      formData[item.field] = null
    }
  })
}, { immediate: true, deep: true })

function handleSubmit() {
  console.log('表单数据:', formData)
  message.success('表单提交成功！请查看控制台输出')
}
</script>

<style scoped>
.preview-content {
  padding: 8px 0;
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 0;
  gap: 12px;
  color: #64748b;
}

.empty-tip p {
  font-size: 14px;
}

.preview-item {
  margin-bottom: 4px;
}

.preview-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
