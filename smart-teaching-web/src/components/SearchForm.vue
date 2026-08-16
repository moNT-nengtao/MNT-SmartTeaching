<template>
  <el-form :model="form" :inline="true" class="search-form" @submit.prevent>
    <slot>
      <el-form-item
        v-for="item in fields"
        :key="item.prop"
        :label="item.label"
      >
        <!-- 输入框 -->
        <el-input
          v-if="item.type === 'input' || !item.type"
          v-model="form[item.prop]"
          :placeholder="item.placeholder || `请输入${item.label}`"
          clearable
          :style="{ width: item.width || '200px' }"
          @keyup.enter="handleSearch"
        />
        <!-- 下拉选择 -->
        <el-select
          v-else-if="item.type === 'select'"
          v-model="form[item.prop]"
          :placeholder="item.placeholder || `请选择${item.label}`"
          clearable
          :style="{ width: item.width || '200px' }"
        >
          <el-option
            v-for="opt in item.options"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <!-- 日期范围 -->
        <el-date-picker
          v-else-if="item.type === 'daterange'"
          v-model="form[item.prop]"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          :style="{ width: item.width || '260px' }"
        />
      </el-form-item>
    </slot>
    <el-form-item>
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive, watch } from 'vue'
import { Search, RefreshLeft } from '@element-plus/icons-vue'

const props = defineProps({
  fields: {
    type: Array,
    default: () => []
  },
  modelValue: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'reset'])

const form = reactive({ ...props.modelValue })

watch(
  () => props.modelValue,
  (val) => {
    Object.keys(form).forEach((key) => delete form[key])
    Object.assign(form, val)
  },
  { deep: true }
)

const handleSearch = () => {
  emit('update:modelValue', { ...form })
  emit('search', { ...form })
}

const handleReset = () => {
  Object.keys(form).forEach((key) => (form[key] = ''))
  emit('update:modelValue', { ...form })
  emit('reset', { ...form })
}
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}
</style>
