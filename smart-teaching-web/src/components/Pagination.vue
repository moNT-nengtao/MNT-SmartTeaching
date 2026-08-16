<template>
  <div class="pagination-wrapper">
    <el-pagination
      :current-page="currentPage"
      :page-size="pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      background
      @update:current-page="onUpdateCurrentPage"
      @update:page-size="onUpdatePageSize"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  total: {
    type: Number,
    default: 0
  },
  page: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 10
  }
})

const emit = defineEmits(['update:page', 'update:pageSize', 'change'])

const currentPage = ref(props.page)
const pageSize = ref(props.pageSize)

watch(
  () => props.page,
  (val) => (currentPage.value = val)
)

watch(
  () => props.pageSize,
  (val) => (pageSize.value = val)
)

// 处理页码变化（来自分页组件内部）
const onUpdateCurrentPage = (val) => {
  currentPage.value = val
}

// 处理每页条数变化（来自分页组件内部）
const onUpdatePageSize = (val) => {
  pageSize.value = val
}

const handleSizeChange = (size) => {
  emit('update:pageSize', size)
  emit('change', { page: currentPage.value, pageSize: size })
}

const handleCurrentChange = (page) => {
  emit('update:page', page)
  emit('change', { page, pageSize: pageSize.value })
}
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>