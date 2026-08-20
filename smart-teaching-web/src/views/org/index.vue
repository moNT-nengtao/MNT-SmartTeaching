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
              :current-node-key="currentNode?.id"
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
                <el-button v-if="currentNode.type === 'class' && selectedStudents.length > 0" link type="warning" @click="handleRemoveSelectedStudents">
                  移出已选学生
                </el-button>
                <el-button link type="primary" :icon="Edit" @click="handleEdit">编辑</el-button>
                <el-button link type="danger" :icon="Delete" @click="handleDelete">删除</el-button>
              </div>
            </div>
            <div v-if="currentNode?.type === 'class'" class="student-panel">
              <div class="student-toolbar">
                <span>已选择 {{ selectedStudents.length }} 人</span>
                <div class="student-actions">
                  <el-button size="small" @click="selectAllStudents">一键全选</el-button>
                  <el-button size="small" @click="clearStudentSelection">一键取消选择</el-button>
                </div>
              </div>
              <el-table ref="studentTableRef" :data="currentNode.students || []" border stripe @selection-change="handleStudentSelectionChange">
                <el-table-column type="selection" width="50" />
                <el-table-column type="index" label="#" width="60" />
                <el-table-column prop="realName" label="姓名" />
                <el-table-column prop="username" label="学号" />
                <el-table-column label="操作" width="130">
                  <template #default="{ row }">
                    <el-button link type="danger" @click="handleRemoveStudent(row)">移出班级</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <el-table v-else :data="detailList" v-loading="loading" border stripe>
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


    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑组织节点' : '新增组织节点'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="节点类型">
          <el-input
            :value="form.type === 'college' ? '学院' : form.type === 'major' ? '专业' : '班级'"
            disabled
          />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="form.code" placeholder="选填，如CS01" />
        </el-form-item>
        <el-form-item v-if="form.type === 'class'" label="年级">
          <el-select v-model.number="form.gradeYear" placeholder="请选择年级" clearable>
            <el-option v-for="y in yearOptions" :key="y" :label="y" :value="y"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDialog">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>


<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Plus, Edit, Delete, Upload, Download } from '@element-plus/icons-vue'
import { getOrgTree, addOrg, updateOrg, deleteOrg, removeStudentsFromClass, batchImportOrg, batchExportOrg } from '@/api/org'


const loading = ref(false)
const treeData = ref([])
const currentNode = ref(null)
const detailList = ref([])
const studentTableRef = ref(null)
const selectedStudents = ref([])


// 年级下拉选项：2018‑2030年份
const yearOptions = ref([])
for(let i = 2018; i <= 2030; i++){
  yearOptions.value.push(i)
}


const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({
  id: null,
  type: '',
  parentId: 0,
  name: '',
  code: '',
  gradeYear: null
})


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
  selectedStudents.value = []
  if(data.type !== 'class'){
    detailList.value = data.children || []
  }else{
    detailList.value = []
  }
}


const handleStudentSelectionChange = (selection) => {
  selectedStudents.value = selection
}


const selectAllStudents = async () => {
  await nextTick()
  const students = currentNode.value?.students || []
  studentTableRef.value?.clearSelection()
  students.forEach((student) => {
    studentTableRef.value?.toggleRowSelection(student, true)
  })
}


const clearStudentSelection = () => {
  studentTableRef.value?.clearSelection()
}


const handleAdd = (type) => {
  isEdit.value = false
  form.value = {
    id: null,
    type: type,
    parentId: 0,
    name: '',
    code: '',
    gradeYear: null
  }
  dialogVisible.value = true
}


const handleAddChild = () => {
  if (!currentNode.value) return
  isEdit.value = false
  const childType = currentNode.value.type === 'college' ? 'major' : 'class'
  form.value = {
    id: null,
    type: childType,
    parentId: currentNode.value.id,
    name: '',
    code: '',
    gradeYear: null
  }
  dialogVisible.value = true
}


const handleEdit = () => {
  if (!currentNode.value) return
  isEdit.value = true
  form.value = {
    id: currentNode.value.id,
    type: currentNode.value.type,
    parentId: currentNode.value.parentId ?? 0,
    name: currentNode.value.name,
    code: currentNode.value.code,
    gradeYear: currentNode.value.gradeYear ?? null
  }
  dialogVisible.value = true
}


const handleEditItem = (row) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    type: row.type,
    parentId: row.parentId ?? 0,
    name: row.name,
    code: row.code,
    gradeYear: row.gradeYear ?? null
  }
  dialogVisible.value = true
}


const submitDialog = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入名称')
    return
  }
  try {
    if (isEdit.value) {
      await updateOrg(form.value)
      ElMessage.success('编辑成功')
    } else {
      await addOrg(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await fetchTree()
  } catch (err) {
    ElMessage.error('操作失败')
  }
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


const handleDeleteItem = (row) => {
  ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteOrg(row.id, row.type)
      ElMessage.success('删除成功')
      fetchTree()
      currentNode.value = null
      detailList.value = []
    })
    .catch(() => {})
}


// 单个移出学生
const handleRemoveStudent = (student) => {
  ElMessageBox.confirm(`确定将学生【${student.realName}】移出当前班级？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await removeStudentsFromClass([student.id])
    ElMessage.success('移出成功')
    await fetchTree()
  }).catch(()=>{})
}


// 移出已勾选学生：与单个移出共用同一个接口
const handleRemoveSelectedStudents = () => {
  const studentList = selectedStudents.value
  if (!studentList.length) return
  ElMessageBox.confirm(`确定将选中的 ${studentList.length} 名学生移出班级？`, '警告', {
    type: 'warning'
  }).then(async () => {
    await removeStudentsFromClass(studentList.map((student) => student.id))
    ElMessage.success('已选学生移出成功')
    selectedStudents.value = []
    await fetchTree()
  }).catch(()=>{})
}


// 批量导入完整实现
const handleImport = async (file) => {
  const loadingInstance = ElLoading.service({ text: '正在批量导入...' })
  try {
    const formData = new FormData()
    formData.append('file', file)
    await batchImportOrg(formData)
    ElMessage.success('批量导入完成')
    await fetchTree()
  } catch (err) {
    ElMessage.error('批量导入失败')
  } finally {
    loadingInstance.close()
  }
  return false
}

// 批量导出完整实现
const handleExport = async () => {
  try {
    const blob = await batchExportOrg()
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = "组织架构数据.xlsx"
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(downloadUrl)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
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
  gap:8px;
}


.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
}


.student-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  color: var(--text-secondary);
  font-size: 13px;
}


.student-actions {
  display: flex;
  gap: 8px;
}
</style>
