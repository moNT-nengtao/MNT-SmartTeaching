<template>
  <div class="chart-card page-card">
    <div class="chart-header">
      <span class="chart-title">{{ title }}</span>
      <slot name="extra" />
    </div>
    <div v-loading="loading" class="chart-body">
      <div ref="chartRef" class="chart-container" :style="{ height: height + 'px' }" />
      <div v-if="!loading && !hasData" class="chart-empty">
        <el-empty description="暂无数据" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  option: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  },
  height: {
    type: Number,
    default: 300
  },
  hasData: {
    type: Boolean,
    default: true
  }
})

const chartRef = ref(null)
let chartInstance = null

const initChart = () => {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  if (props.option && Object.keys(props.option).length > 0) {
    chartInstance.setOption(props.option)
  }
}

const handleResize = () => {
  chartInstance?.resize()
}

watch(
  () => props.option,
  (newOption) => {
    if (chartInstance && newOption) {
      chartInstance.setOption(newOption, true)
    }
  },
  { deep: true }
)

onMounted(() => {
  nextTick(() => {
    initChart()
    window.addEventListener('resize', handleResize)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.chart-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.chart-body {
  flex: 1;
  position: relative;
}

.chart-container {
  width: 100%;
}

.chart-empty {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
</style>
