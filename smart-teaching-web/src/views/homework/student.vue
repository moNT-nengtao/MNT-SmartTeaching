<template>
  <div class="homework-page">
    <SearchForm v-model="searchParams" :fields="searchFields" @search="fetchList" @reset="fetchList" />

    <div class="page-card">
      <div class="page-header">
        <span class="page-title">我的作业</span>
      </div>

      <el-table :data="homeworkList" v-loading="loading" border stripe>
        <el-table-column prop="title" label="作业标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="courseName" label="所属课程" width="140" show-overflow-tooltip />
        <el-table-column prop="deadline" label="截止时间" width="170">
          <template #default="{ row }">
            <span :class="{ 'deadline-overdue': isOverdue(row.deadline) }">{{ row.deadline || '无' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="提交状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.submitStatus === 1" type="success">已提交</el-tag>
            <el-tag v-else type="warning">未提交</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="成绩" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.score !== null && row.score !== undefined" :type="row.score >= 60 ? 'success' : 'danger'">{{ row.score }}</el-tag>
            <span v-else style="color: #909399">待批改</span>
          </template>
        </el-table-column>
        <el-table-column label="教师评语" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.comment || '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="170">
          <template #default="{ row }">
            {{ row.submitTime || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetailDialog(row)">查看要求</el-button>
            <el-button link type="primary" @click="openSubmitDialog(row)">{{ row.submitStatus === 1 ? '重新提交' : '提交作业' }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="searchParams.pageNum" v-model:page-size="searchParams.pageSize" :total="total" @change="fetchList" />
    </div>

    <!-- 作业要求详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="作业要求" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="作业标题">{{ currentHomework.title }}</el-descriptions-item>
        <el-descriptions-item label="所属课程">{{ currentHomework.courseName }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ currentHomework.deadline || '无' }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ currentHomework.createTime }}</el-descriptions-item>
        <el-descriptions-item label="作业要求">
          <div style="white-space: pre-wrap; line-height: 1.6;">{{ currentHomework.content || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="附件">
          <el-link v-if="currentHomework.attachmentUrl" type="primary" @click="handleDownloadAttachment">
            {{ currentHomework.attachmentName || '下载附件' }}
          </el-link>
          <span v-else>无</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 提交作业对话框 -->
    <el-dialog v-model="submitDialogVisible" :title="isResubmit ? '重新提交作业' : '提交作业'" width="600px" @close="resetSubmitForm">
      <div class="submit-info">
        <p><strong>作业：</strong>{{ currentHomework.title }}</p>
        <p><strong>课程：</strong>{{ currentHomework.courseName }}</p>
        <p v-if="currentHomework.deadline"><strong>截止：</strong>{{ currentHomework.deadline }}</p>
      </div>
      <el-form :model="submitForm" label-width="80px">
        <el-form-item label="提交内容">
          <el-input v-model="submitForm.content" type="textarea" :rows="5" placeholder="请输入作业内容" />
        </el-form-item>
        <el-form-item label="附件">
          <input ref="attachmentInputRef" type="file" style="display: none" @change="handleAttachmentChange" />
          <el-button :icon="Upload" @click="triggerAttachmentInput">上传附件（可选）</el-button>
          <span v-if="submitForm.fileName" class="file-name">{{ submitForm.fileName }}</span>
          <el-button v-if="submitForm.fileName" link type="danger" @click="clearAttachment">移除</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { getMyHomework, submitHomework, downloadAttachment } from '@/api/homework'

const loading = ref(false)
const submitting = ref(false)
const homeworkList = ref([])
const total = ref(0)

const searchParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  courseId: ''
})

const searchFields = [
  { prop: 'title', label: '作业标题', type: 'input' }
]

const detailDialogVisible = ref(false)
const submitDialogVisible = ref(false)
const isResubmit = ref(false)
const currentHomework = ref({})
const attachmentInputRef = ref(null)

const submitForm = reactive({
  homeworkId: null,
  content: '',
  file: null,
  fileName: ''
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getMyHomework(searchParams)
    homeworkList.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } catch (error) {
    console.error('获取作业列表失败', error)
  } finally {
    loading.value = false
  }
}

const isOverdue = (deadline) => {
  if (!deadline) return false
  return new Date(deadline) < new Date()
}

const openDetailDialog = (row) => {
  currentHomework.value = row
  detailDialogVisible.value = true
}

const openSubmitDialog = (row) => {
  currentHomework.value = row
  isResubmit.value = row.submitStatus === 1
  submitForm.homeworkId = row.id
  submitForm.content = ''
  submitForm.file = null
  submitForm.fileName = ''
  submitDialogVisible.value = true
}

const resetSubmitForm = () => {
  submitForm.homeworkId = null
  submitForm.content = ''
  submitForm.file = null
  submitForm.fileName = ''
}

const triggerAttachmentInput = () => {
  attachmentInputRef.value?.click()
}

const handleAttachmentChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    submitForm.file = file
    submitForm.fileName = file.name
  }
}

const clearAttachment = () => {
  submitForm.file = null
  submitForm.fileName = ''
  if (attachmentInputRef.value) attachmentInputRef.value.value = ''
}

const handleSubmit = async () => {
  if (!submitForm.content && !submitForm.file) {
    ElMessage.warning('请输入作业内容或上传附件')
    return
  }
  submitting.value = true
  try {
    const formData = new FormData()
    formData.append('homeworkId', submitForm.homeworkId)
    if (submitForm.content) formData.append('content', submitForm.content)
    if (submitForm.file) formData.append('file', submitForm.file)

    await submitHomework(formData)
    ElMessage.success(isResubmit.value ? '重新提交成功' : '提交成功')
    submitDialogVisible.value = false
    fetchList()
  } catch (error) {
    console.error('提交失败', error)
  } finally {
    submitting.value = false
  }
}

// 下载附件（带404检测）
const handleDownloadAttachment = () => {
  downloadAttachment(currentHomework.value.attachmentUrl, currentHomework.value.attachmentName || '作业附件')
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.file-name {
  margin-left: 10px;
  color: #606266;
  font-size: 13px;
}
.submit-info {
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}
.submit-info p {
  margin: 4px 0;
  font-size: 14px;
}
.deadline-overdue {
  color: #f56c6c;
}
</style>
