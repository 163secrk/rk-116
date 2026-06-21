export function validateField(value, schema) {
  const errors = []
  const validation = schema.validation || {}

  const isEmpty = value === null || value === undefined || value === '' ||
    (Array.isArray(value) && value.length === 0)

  if (validation.required) {
    if (isEmpty) {
      errors.push(validation.requiredMessage || '该项为必填项')
    }
  }

  if (!isEmpty) {
    const strValue = String(value)

    if (validation.minLength !== null && validation.minLength !== undefined) {
      if (strValue.length < validation.minLength) {
        errors.push(validation.minLengthMessage || '长度不能小于最小值')
      }
    }

    if (validation.maxLength !== null && validation.maxLength !== undefined) {
      if (strValue.length > validation.maxLength) {
        errors.push(validation.maxLengthMessage || '长度不能大于最大值')
      }
    }

    if (validation.pattern !== null && validation.pattern !== undefined && validation.pattern.trim()) {
      try {
        const regex = new RegExp(validation.pattern)
        if (!regex.test(strValue)) {
          errors.push(validation.patternMessage || '格式不正确')
        }
      } catch (e) {
        // 正则表达式无效时跳过
      }
    }

    if ((validation.min !== null && validation.min !== undefined) ||
        (validation.max !== null && validation.max !== undefined)) {
      const numValue = Number(value)
      if (!isNaN(numValue)) {
        if (validation.min !== null && validation.min !== undefined) {
          if (numValue < validation.min) {
            errors.push(validation.minMessage || '数值不能小于最小值')
          }
        }
        if (validation.max !== null && validation.max !== undefined) {
          if (numValue > validation.max) {
            errors.push(validation.maxMessage || '数值不能大于最大值')
          }
        }
      }
    }
  }

  return {
    valid: errors.length === 0,
    errors
  }
}

export function validateForm(formData, schemaList) {
  const results = {}
  let allValid = true

  schemaList.forEach(schema => {
    const value = formData[schema.field]
    const result = validateField(value, schema)
    results[schema.field] = result
    if (!result.valid) {
      allValid = false
    }
  })

  return {
    valid: allValid,
    fields: results
  }
}
