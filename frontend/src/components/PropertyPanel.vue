<template>
  <aside class="property-panel">
    <div class="panel-header">
      <n-icon size="18"><SettingsOutline /></n-icon>
      <span>属性面板</span>
    </div>
    <div class="panel-content">
      <div v-if="!selectedComponent" class="empty-state">
        <n-icon size="40"><HandRightOutline /></n-icon>
        <p>请选择一个组件进行编辑</p>
      </div>
      <div v-else class="property-form">
        <div class="property-section">
          <div class="section-title">基本属性</div>
          <n-form label-placement="left" label-width="80px">
            <n-form-item v-if="!isContainerType" label="标签">
              <n-input
                :value="localComp.label"
                @update:value="val => updateField('label', val)"
                placeholder="请输入标签"
                size="small"
              />
              <div class="field-help">表单中显示的字段名称</div>
            </n-form-item>
            <n-form-item v-if="!isContainerType" label="字段名">
              <n-input
                :value="localComp.field"
                @update:value="val => updateField('field', val)"
                placeholder="请输入字段名"
                size="small"
              />
              <div class="field-help">数据提交时的字段标识，建议使用英文</div>
            </n-form-item>
            <n-form-item v-if="hasPlaceholder" label="占位符">
              <n-input
                :value="localComp.placeholder"
                @update:value="val => updateField('placeholder', val)"
                placeholder="请输入占位符"
                size="small"
              />
              <div class="field-help">输入框为空时显示的提示文字</div>
            </n-form-item>
            <n-form-item v-if="!isContainerType" label="帮助说明">
              <n-input
                :value="localComp.helpText"
                @update:value="val => updateField('helpText', val)"
                placeholder="请输入帮助说明"
                size="small"
              />
              <div class="field-help">字段下方显示的灰色提示文字</div>
            </n-form-item>
            <n-form-item v-if="!isContainerType" label="是否禁用">
              <n-switch
                :value="localComp.disabled"
                @update:value="val => updateField('disabled', val)"
              />
              <div class="field-help">禁用后表单字段变灰不可编辑</div>
            </n-form-item>
          </n-form>
        </div>

        <div v-if="isContainerType" class="property-section">
          <div class="section-title">容器配置</div>
          <n-form label-placement="left" label-width="80px">
            <n-form-item label="列数">
              <n-input-number
                :value="localComp.columns"
                @update:value="val => updateField('columns', val)"
                :min="1"
                :max="6"
                size="small"
                style="width: 100%"
              />
              <div class="field-help">布局容器的列数，支持1-6列</div>
            </n-form-item>
          </n-form>
        </div>

        <div v-if="hasDefaultValue" class="property-section">
          <div class="section-title">默认值</div>
          <n-form label-placement="left" label-width="80px">
            <n-form-item v-if="isInputType" label="默认值">
              <n-input
                :value="localComp.defaultValue"
                @update:value="val => updateField('defaultValue', val)"
                placeholder="请输入默认值"
                size="small"
              />
              <div class="field-help">表单初始化时自动填充的值</div>
            </n-form-item>
            <n-form-item v-if="isTextareaType" label="默认值">
              <n-input
                :value="localComp.defaultValue"
                @update:value="val => updateField('defaultValue', val)"
                type="textarea"
                :rows="3"
                placeholder="请输入默认值"
                size="small"
              />
              <div class="field-help">表单初始化时自动填充的值</div>
            </n-form-item>
            <n-form-item v-if="isSelectType || isRadioType" label="默认值">
              <n-select
                :value="localComp.defaultValue"
                @update:value="val => updateField('defaultValue', val)"
                :options="localComp.options"
                placeholder="请选择默认值"
                size="small"
                clearable
              />
              <div class="field-help">表单初始化时默认选中的选项</div>
            </n-form-item>
            <n-form-item v-if="isCheckboxType" label="默认值">
              <n-checkbox-group
                :value="localComp.defaultValue"
                @update:value="val => updateField('defaultValue', val)"
              >
                <n-space>
                  <n-checkbox
                    v-for="opt in localComp.options"
                    :key="opt.value"
                    :value="opt.value"
                  >
                    {{ opt.label }}
                  </n-checkbox>
                </n-space>
              </n-checkbox-group>
              <div class="field-help">表单初始化时默认选中的选项</div>
            </n-form-item>
            <n-form-item v-if="isDateType" label="默认值">
              <n-date-picker
                :value="localComp.defaultValue"
                @update:value="val => updateField('defaultValue', val)"
                type="date"
                size="small"
                clearable
                style="width: 100%"
              />
              <div class="field-help">表单初始化时默认选中的日期</div>
            </n-form-item>
            <n-form-item v-if="isTimeType" label="默认值">
              <n-time-picker
                :value="localComp.defaultValue"
                @update:value="val => updateField('defaultValue', val)"
                size="small"
                clearable
                style="width: 100%"
              />
              <div class="field-help">表单初始化时默认选中的时间</div>
            </n-form-item>
            <n-form-item v-if="isRatingType" label="默认值">
              <n-rate
                :value="localComp.defaultValue"
                @update:value="val => updateField('defaultValue', val)"
                :count="localComp.maxStars || 5"
              />
              <div class="field-help">表单初始化时默认的评分</div>
            </n-form-item>
            <n-form-item v-if="isSwitchType" label="默认值">
              <n-switch
                :value="localComp.defaultValue"
                @update:value="val => updateField('defaultValue', val)"
              />
              <div class="field-help">表单初始化时开关的默认状态</div>
            </n-form-item>
          </n-form>
        </div>

        <div v-if="isRatingType" class="property-section">
          <div class="section-title">评分配置</div>
          <n-form label-placement="left" label-width="80px">
            <n-form-item label="最大星数">
              <n-input-number
                :value="localComp.maxStars"
                @update:value="val => updateField('maxStars', val)"
                :min="1"
                :max="10"
                size="small"
                style="width: 100%"
              />
            </n-form-item>
          </n-form>
        </div>

        <div v-if="isUploadType" class="property-section">
          <div class="section-title">
            <span>上传配置</span>
          </div>
          <n-form label-placement="left" label-width="80px">
            <n-form-item label="大小限制">
              <n-input-number
                :value="localComp.maxSize"
                @update:value="val => updateField('maxSize', val)"
                :min="1"
                size="small"
                style="width: 100%"
              />
              <span class="form-item-suffix">MB</span>
            </n-form-item>
          </n-form>
          <div class="section-sub-title">支持的文件类型</div>
          <div class="options-list">
            <div
              v-for="(type, idx) in localComp.acceptTypes"
              :key="idx"
              class="option-item"
            >
              <n-input
                :value="type"
                @update:value="val => updateAcceptType(idx, val)"
                placeholder="例如: image/* 或 .pdf"
                size="small"
                style="flex: 1; margin-right: 8px"
              />
              <n-button text size="tiny" type="error" @click="removeAcceptType(idx)">
                <template #icon>
                  <n-icon size="16"><TrashOutline /></n-icon>
                </template>
              </n-button>
            </div>
          </div>
          <n-button text size="tiny" type="primary" @click="addAcceptType" style="margin-top: 8px">
            <template #icon>
              <n-icon><AddOutline /></n-icon>
            </template>
            添加文件类型
          </n-button>
        </div>

        <div v-if="isSwitchType" class="property-section">
          <div class="section-title">开关配置</div>
          <n-form label-placement="left" label-width="80px">
            <n-form-item label="默认值">
              <n-switch
                :value="localComp.defaultValue"
                @update:value="val => updateField('defaultValue', val)"
              />
            </n-form-item>
          </n-form>
        </div>

        <div v-if="hasOptions" class="property-section">
          <div class="section-title">
            <span>选项配置</span>
            <n-button text size="tiny" type="primary" @click="addOption">
              <template #icon>
                <n-icon><AddOutline /></n-icon>
              </template>
              添加选项
            </n-button>
          </div>
          <div class="options-list">
            <div
              v-for="(opt, idx) in localComp.options"
              :key="idx"
              class="option-item"
            >
              <n-input
                :value="opt.label"
                @update:value="val => updateOption(idx, 'label', val)"
                placeholder="标签"
                size="small"
                style="flex: 1; margin-right: 8px"
              />
              <n-input
                :value="opt.value"
                @update:value="val => updateOption(idx, 'value', val)"
                placeholder="值"
                size="small"
                style="flex: 1; margin-right: 8px"
              />
              <n-button text size="tiny" type="error" @click="removeOption(idx)">
                <template #icon>
                  <n-icon size="16"><TrashOutline /></n-icon>
                </template>
              </n-button>
            </div>
          </div>
        </div>

        <div v-if="!isContainerType" class="property-section">
          <div class="section-title">校验规则</div>
          <n-form label-placement="left" label-width="80px">
            <div class="validation-item">
              <div class="validation-header">
                <n-switch
                  :value="localComp.validation.required"
                  @update:value="val => updateValidation('required', val)"
                />
                <span class="validation-label">必填校验</span>
              </div>
              <n-form-item v-if="localComp.validation.required" label="提示">
                <n-input
                  :value="localComp.validation.requiredMessage"
                  @update:value="val => updateValidation('requiredMessage', val)"
                  placeholder="必填提示文字"
                  size="small"
                />
              </n-form-item>
            </div>

            <div class="validation-item">
              <div class="validation-header">
                <n-switch
                  :value="hasLengthValidation"
                  @update:value="toggleLengthValidation"
                />
                <span class="validation-label">长度校验</span>
              </div>
              <div v-if="hasLengthValidation" class="validation-body">
                <n-form-item label="最小长度">
                  <n-input-number
                    :value="localComp.validation.minLength"
                    @update:value="val => updateValidation('minLength', val)"
                    :min="0"
                    size="small"
                    style="width: 100%"
                  />
                </n-form-item>
                <n-form-item label="最大长度">
                  <n-input-number
                    :value="localComp.validation.maxLength"
                    @update:value="val => updateValidation('maxLength', val)"
                    :min="0"
                    size="small"
                    style="width: 100%"
                  />
                </n-form-item>
                <n-form-item label="提示文字">
                  <n-input
                    :value="localComp.validation.minLengthMessage"
                    @update:value="val => updateValidation('minLengthMessage', val)"
                    placeholder="最小长度提示"
                    size="small"
                  />
                </n-form-item>
                <n-form-item label="">
                  <n-input
                    :value="localComp.validation.maxLengthMessage"
                    @update:value="val => updateValidation('maxLengthMessage', val)"
                    placeholder="最大长度提示"
                    size="small"
                  />
                </n-form-item>
              </div>
            </div>

            <div v-if="supportsPattern" class="validation-item">
              <div class="validation-header">
                <n-switch
                  :value="hasPatternValidation"
                  @update:value="togglePatternValidation"
                />
                <span class="validation-label">正则表达式</span>
              </div>
              <div v-if="hasPatternValidation" class="validation-body">
                <n-form-item label="表达式">
                  <n-input
                    :value="localComp.validation.pattern"
                    @update:value="val => updateValidation('pattern', val)"
                    placeholder="例如: ^[a-zA-Z]+$"
                    size="small"
                  />
                </n-form-item>
                <n-form-item label="提示文字">
                  <n-input
                    :value="localComp.validation.patternMessage"
                    @update:value="val => updateValidation('patternMessage', val)"
                    placeholder="正则校验提示"
                    size="small"
                  />
                </n-form-item>
              </div>
            </div>

            <div v-if="supportsNumberRange" class="validation-item">
              <div class="validation-header">
                <n-switch
                  :value="hasNumberValidation"
                  @update:value="toggleNumberValidation"
                />
                <span class="validation-label">数字范围</span>
              </div>
              <div v-if="hasNumberValidation" class="validation-body">
                <n-form-item label="最小值">
                  <n-input-number
                    :value="localComp.validation.min"
                    @update:value="val => updateValidation('min', val)"
                    size="small"
                    style="width: 100%"
                  />
                </n-form-item>
                <n-form-item label="最大值">
                  <n-input-number
                    :value="localComp.validation.max"
                    @update:value="val => updateValidation('max', val)"
                    size="small"
                    style="width: 100%"
                  />
                </n-form-item>
                <n-form-item label="提示文字">
                  <n-input
                    :value="localComp.validation.minMessage"
                    @update:value="val => updateValidation('minMessage', val)"
                    placeholder="最小值提示"
                    size="small"
                  />
                </n-form-item>
                <n-form-item label="">
                  <n-input
                    :value="localComp.validation.maxMessage"
                    @update:value="val => updateValidation('maxMessage', val)"
                    placeholder="最大值提示"
                    size="small"
                  />
                </n-form-item>
              </div>
            </div>
          </n-form>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { NIcon, NForm, NFormItem, NInput, NSwitch, NButton, NInputNumber, NSelect, NCheckboxGroup, NCheckbox, NSpace, NDatePicker, NTimePicker, NRate } from 'naive-ui'
import { SettingsOutline, HandRightOutline, AddOutline, TrashOutline } from '@vicons/ionicons5'

const props = defineProps({
  selectedComponent: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update-component'])

function getDefaultValidation() {
  return {
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

function getDefaultLocalComp() {
  return {
    id: null,
    type: '',
    label: '',
    field: '',
    placeholder: '',
    options: [],
    required: false,
    maxStars: 5,
    acceptTypes: [],
    maxSize: 10,
    defaultValue: null,
    helpText: '',
    disabled: false,
    columns: 2,
    children: [],
    validation: getDefaultValidation()
  }
}

const localComp = ref(getDefaultLocalComp())
let currentId = null

function ensureValidation(comp) {
  if (!comp.validation) {
    comp.validation = getDefaultValidation()
  }
  if (comp.type === 'rating' && comp.maxStars === undefined) {
    comp.maxStars = 5
  }
  if (comp.type === 'upload') {
    if (comp.acceptTypes === undefined) {
      comp.acceptTypes = []
    }
    if (comp.maxSize === undefined) {
      comp.maxSize = 10
    }
  }
  if (comp.type === 'container') {
    if (comp.columns === undefined) {
      comp.columns = 2
    }
    if (comp.children === undefined) {
      comp.children = []
    }
    comp.children = comp.children.map(child => ensureValidation(child))
  }
  if (comp.defaultValue === undefined) {
    if (comp.type === 'switch') {
      comp.defaultValue = false
    } else if (comp.type === 'checkbox') {
      comp.defaultValue = []
    } else if (comp.type === 'input' || comp.type === 'textarea') {
      comp.defaultValue = ''
    } else if (comp.type === 'container') {
      comp.defaultValue = null
    } else {
      comp.defaultValue = null
    }
  }
  if (comp.helpText === undefined) {
    comp.helpText = ''
  }
  if (comp.disabled === undefined) {
    comp.disabled = false
  }
  return comp
}

watch(() => props.selectedComponent, (val) => {
  const newId = val ? val.id : null
  if (newId !== currentId) {
    currentId = newId
    if (val) {
      localComp.value = ensureValidation(JSON.parse(JSON.stringify(val)))
    } else {
      localComp.value = getDefaultLocalComp()
    }
  }
}, { immediate: true })

function emitUpdate() {
  if (localComp.value.id) {
    emit('update-component', JSON.parse(JSON.stringify(localComp.value)))
  }
}

function updateField(key, value) {
  localComp.value[key] = value
  emitUpdate()
}

function updateValidation(key, value) {
  localComp.value.validation[key] = value
  emitUpdate()
}

function updateAcceptType(idx, value) {
  localComp.value.acceptTypes.splice(idx, 1, value)
  emitUpdate()
}

function addAcceptType() {
  localComp.value.acceptTypes.push('')
  emitUpdate()
}

function removeAcceptType(idx) {
  localComp.value.acceptTypes.splice(idx, 1)
  emitUpdate()
}

function updateOption(idx, key, value) {
  localComp.value.options[idx][key] = value
  emitUpdate()
}

const hasPlaceholder = computed(() => {
  return !isContainerType.value && ['input', 'textarea', 'select', 'date', 'time'].includes(localComp.value.type)
})

const hasDefaultValue = computed(() => {
  return !isContainerType.value && ['input', 'textarea', 'select', 'radio', 'checkbox', 'date', 'time', 'rating', 'switch'].includes(localComp.value.type)
})

const hasOptions = computed(() => {
  return !isContainerType.value && ['select', 'radio', 'checkbox'].includes(localComp.value.type)
})

const isInputType = computed(() => {
  return localComp.value.type === 'input'
})

const isTextareaType = computed(() => {
  return localComp.value.type === 'textarea'
})

const isSelectType = computed(() => {
  return localComp.value.type === 'select'
})

const isRadioType = computed(() => {
  return localComp.value.type === 'radio'
})

const isCheckboxType = computed(() => {
  return localComp.value.type === 'checkbox'
})

const isDateType = computed(() => {
  return localComp.value.type === 'date'
})

const isTimeType = computed(() => {
  return localComp.value.type === 'time'
})

const isRatingType = computed(() => {
  return localComp.value.type === 'rating'
})

const isUploadType = computed(() => {
  return localComp.value.type === 'upload'
})

const isSwitchType = computed(() => {
  return localComp.value.type === 'switch'
})

const isContainerType = computed(() => {
  return localComp.value.type === 'container'
})

const supportsPattern = computed(() => {
  return ['input', 'textarea'].includes(localComp.value.type)
})

const supportsNumberRange = computed(() => {
  return ['input'].includes(localComp.value.type)
})

const hasLengthValidation = computed(() => {
  return localComp.value.validation.minLength !== null || localComp.value.validation.maxLength !== null
})

const hasPatternValidation = computed(() => {
  return localComp.value.validation.pattern !== null && localComp.value.validation.pattern !== undefined
})

const hasNumberValidation = computed(() => {
  return localComp.value.validation.min !== null || localComp.value.validation.max !== null
})

function toggleLengthValidation(enabled) {
  if (enabled) {
    if (localComp.value.validation.minLength === null) {
      localComp.value.validation.minLength = 0
    }
    if (localComp.value.validation.maxLength === null) {
      localComp.value.validation.maxLength = 100
    }
  } else {
    localComp.value.validation.minLength = null
    localComp.value.validation.maxLength = null
  }
  emitUpdate()
}

function togglePatternValidation(enabled) {
  if (enabled) {
    if (localComp.value.validation.pattern === null || localComp.value.validation.pattern === undefined) {
      localComp.value.validation.pattern = ''
    }
  } else {
    localComp.value.validation.pattern = null
  }
  emitUpdate()
}

function toggleNumberValidation(enabled) {
  if (enabled) {
    if (localComp.value.validation.min === null) {
      localComp.value.validation.min = 0
    }
    if (localComp.value.validation.max === null) {
      localComp.value.validation.max = 100
    }
  } else {
    localComp.value.validation.min = null
    localComp.value.validation.max = null
  }
  emitUpdate()
}

function addOption() {
  localComp.value.options.push({
    label: '选项' + (localComp.value.options.length + 1),
    value: 'option' + (localComp.value.options.length + 1)
  })
  emitUpdate()
}

function removeOption(idx) {
  localComp.value.options.splice(idx, 1)
  emitUpdate()
}
</script>

<style scoped>
.property-panel {
  width: 300px;
  background: #1e293b;
  border-left: 1px solid #334155;
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
}

.empty-state {
  padding: 48px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
  text-align: center;
}

.empty-state p {
  font-size: 13px;
}

.property-form {
  padding: 16px;
}

.property-section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  font-weight: 600;
  color: #94a3b8;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #334155;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  align-items: center;
}

.validation-item {
  margin-bottom: 16px;
  padding: 12px;
  background: #0f172a;
  border: 1px solid #334155;
  border-radius: 8px;
}

.validation-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 0;
}

.validation-label {
  font-size: 13px;
  color: #cbd5e1;
  font-weight: 500;
}

.validation-body {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #334155;
}

.validation-body :deep(.n-form-item) {
  margin-bottom: 10px;
}

.validation-body :deep(.n-form-item:last-child) {
  margin-bottom: 0;
}

.section-sub-title {
  font-size: 12px;
  font-weight: 500;
  color: #94a3b8;
  margin: 12px 0 8px 0;
}

.form-item-suffix {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: #94a3b8;
  pointer-events: none;
}

:deep(.n-form-item) {
  position: relative;
}

.field-help {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
  line-height: 1.4;
}
</style>
