<template>
  <div class="org-page">
    <div class="page-card">
      <div class="page-header">
        <span class="page-title">组织架构（学院 / 专业 / 班级）</span>
        <div>
          <el-button :icon="Download" @click="handleExport">批量导出</el-button>
          <el-upload :show-file-list="false" :before-upload="handleImport" accept=".xlsx,.xls">
            <el-button :icon="Upload">批量导入</el-button>
          </el-upload>
        </div>
      </div>

      <el-row :gutter="20">
        <el-col :span="8">
          <div class="tree-panel">
            <div class="panel-header">
              <span>学院列表</span>
              <el-button link type="primary" :icon="Plus" @click="handleAdd('college')">新增学院</el-button>
            </div>
            <el-tree
              :data="treeData"
              :props="{ label: 'name', children: 'children' }"
              node-key="id"
              :expand-on-click-node="false"
              :default-expand-all="true"
              @node-click="handleNodeClick"
            >
              <template #default="{ node, data }">
                <span class="tree-node">
                  <el-icon><component :is="data.type === 'college' ? 'OfficeBuilding' : data.type === 'major' ? 'Collection' : 'User'" /></el-icon>
                  <span>{{ data.name }}</span>
                </span>
              </template>
            </el-tree>
          </div>
        </el-col>
        <el-col :span="16">
          <div class="detail-panel">
            <div class="panel-header">
              <span>{{ currentNode?.name || '请选择左侧节点' }}</span>
              <div v-if="currentNode">
                <el-button v-if="currentNode.type !== 'class'" link type="primary" :icon="Plus" @click="handleAddChild">
                  新增{{ currentNode.type === 'college' ? '专业' : '班级' }}
                </el-button>
                <el-button link type="primary" :icon="Edit" @click="handleEdit">编辑</el-button>
                <el-button link type="danger" :icon="Delete" @click="handleDelete">删除</el-button>
              </div>
            </div>
            <el-table :data="detailList" v-loading="loading" border stripe>
              <el-table-column prop="name" label="名称" />
              <el-table-column prop="code" label="编码" width="150" />
              <el-table-column prop="studentCount" label="学生数" width="100" />
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button link type="primary" @click="handleEditItem(row)">编辑</el-button>
                  <el-button link type="danger" @click="handleDeleteItem(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Upload, Download } from '@element-plus/icons-vue'
import { getOrgTree, addOrg, updateOrg, deleteOrg } from '@/api/org'

const loading = ref(false)
const treeData = ref([])
const currentNode = ref(null)
const detailList = ref([])

const fetchTree = async () => {
  loading.value = true
  try {
    const res = await getOrgTree()
    treeData.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleNodeClick = (data) => {
  currentNode.value = data
  detailList.value = data.children || []
}

const handleAdd = (type) => {
  ElMessage.info(`新增${type === 'college' ? '学院' : '节点'}功能待实现`)
}

const handleAddChild = () => {
  ElMessage.info('新增下级节点功能待实现')
}

const handleEdit = () => {
  ElMessage.info('编辑功能待实现')
}

const handleDelete = () => {
  if (!currentNode.value) return
  ElMessageBox.confirm(`确定删除「${currentNode.value.name}」吗？删除前将校验下级关联。`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteOrg(currentNode.value.id, currentNode.value.type)
    ElMessage.success('删除成功')
    fetchTree()
    currentNode.value = null
    detailList.value = []
  }).catch(() => {})
}

const handleEditItem = (row) => {
  ElMessage.info('编辑功能待实现')
}

const handleDeleteItem = (row) => {
  ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(() => ElMessage.success('删除成功'))
    .catch(() => {})
}

const handleImport = () => {
  ElMessage.info('批量导入功能待实现')
  return false
}

const handleExport = () => {
  ElMessage.info('批量导出功能待实现')
}

onMounted(() => {
  fetchTree()
})
</script>

<style scoped>
.tree-panel, .detail-panel {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  min-height: 500px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  font-weight: 600;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>
