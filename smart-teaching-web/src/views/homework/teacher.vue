<template>
  <div class="homework-page">
    <SearchForm v-model="searchParams" :fields="searchFields" @search="fetchList" @reset="fetchList" />

    <div class="page-card">
      <div class="page-header">
        <span class="page-title">作业管理</span>
        <el-button type="primary" :icon="Plus" @click="openPublishDialog">发布作业</el-button>
      </div>

      <el-table :data="homeworkList" v-loading="loading" border stripe>
        <el-table-column prop="title" label="作业标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="courseName" label="所属课程" width="160" show-overflow-tooltip />
        <el-table-column prop="deadline" label="截止时间" width="170">
          <template #default="{ row }">
            {{ row.deadline || '无' }}
          </template>
        </el-table-column>
        <el-table-column label="提交情况" width="120">
          <template #default="{ row }">
            <span>{{ row.gradedCount || 0 }}/{{ row.submissionCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openSubmissionDialog(row)">查看提交</el-button>
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="searchParams.pageNum" v-model:page-size="searchParams.pageSize" :total="total" @change="fetchList" />
    </div>

    <!-- 发布/编辑作业对话框 -->
    <el-dialog v-model="publishDialogVisible" :title="isEdit ? '编辑作业' : '发布作业'" width="600px" @close="resetForm">
      <el-form :model="form" label-width="80px">
        <el-form-item label="所属课程" required>
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option v-for="c in courseOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="作业标题" required>
          <el-input v-model="form.title" placeholder="请输入作业标题" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="作业要求">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入作业要求" />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="form.deadline" type="datetime" placeholder="选择截止时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="附件">
          <input ref="attachmentInputRef" type="file" style="display: none" @change="handleAttachmentChange" />
          <el-button :icon="Upload" @click="triggerAttachmentInput">上传附件（可选）</el-button>
          <span v-if="form.fileName" class="file-name">{{ form.fileName }}</span>
          <el-button v-if="form.fileName" link type="danger" @click="clearAttachment">移除</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ isEdit ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>

    <!-- 提交列表对话框 -->
    <el-dialog v-model="submissionDialogVisible" title="作业提交列表" width="900px">
      <el-table :data="submissionList" v-loading="submissionLoading" border stripe max-height="500">
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="className" label="班级" width="120" show-overflow-tooltip />
        <el-table-column prop="submitTime" label="提交时间" width="170" />
        <el-table-column prop="content" label="提交内容" min-width="150" show-overflow-tooltip />
        <el-table-column label="附件" width="100">
          <template #default="{ row }">
            <el-link v-if="row.attachmentUrl" type="primary" @click="handleDownloadAttachment(row)">下载附件</el-link>
            <span v-else>无</span>
          </template>
        </el-table-column>
        <el-table-column label="成绩" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.score !== null && row.score !== undefined" :type="row.score >= 60 ? 'success' : 'danger'">{{ row.score }}</el-tag>
            <span v-else style="color: #909399">未批改</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openGradeDialog(row)">批改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 批改对话框 -->
    <el-dialog v-model="gradeDialogVisible" title="批改作业" width="500px">
      <el-form :model="gradeForm" label-width="80px">
        <el-form-item label="学生">
          <span>{{ gradeForm.studentName }}（{{ gradeForm.studentNo }}）</span>
        </el-form-item>
        <el-form-item label="成绩" required>
          <el-input-number v-model="gradeForm.score" :min="0" :max="100" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="gradeForm.comment" type="textarea" :rows="3" placeholder="请输入评语（可选）" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gradeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="grading" @click="handleGrade">提交批改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { getCourseList } from '@/api/course'
import {
  getHomeworkList, publishHomework, updateHomework, deleteHomework,
  getSubmissionList, gradeHomework, downloadAttachment
} from '@/api/homework'

const loading = ref(false)
const submitting = ref(false)
const grading = ref(false)
const submissionLoading = ref(false)
const homeworkList = ref([])
const total = ref(0)
const courseOptions = ref([])

const searchParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  courseId: ''
})

const searchFields = [
  { prop: 'title', label: '作业标题', type: 'input' },
  { prop: 'courseId', label: '所属课程', type: 'select', options: [] }
]

const publishDialogVisible = ref(false)
const isEdit = ref(false)
const attachmentInputRef = ref(null)
const form = reactive({
  id: null,
  courseId: null,
  title: '',
  content: '',
  deadline: null,
  file: null,
  fileName: ''
})

const submissionDialogVisible = ref(false)
const submissionList = ref([])
const currentHomework = ref(null)

const gradeDialogVisible = ref(false)
const gradeForm = reactive({
  submissionId: null,
  studentName: '',
  studentNo: '',
  score: null,
  comment: ''
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getHomeworkList(searchParams)
    homeworkList.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } catch (error) {
    console.error('获取作业列表失败', error)
  } finally {
    loading.value = false
  }
}

const fetchCourses = async () => {
  try {
    const res = await getCourseList({ pageNum: 1, pageSize: 200 })
    courseOptions.value = res.data?.records ?? []
    searchFields[1].options = courseOptions.value.map(c => ({ label: c.name, value: c.id }))
  } catch (error) {
    console.error('获取课程列表失败', error)
  }
}

const openPublishDialog = () => {
  isEdit.value = false
  resetForm()
  publishDialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  form.id = row.id
  form.courseId = row.courseId
  form.title = row.title
  form.content = row.content
  form.deadline = row.deadline
  form.file = null
  form.fileName = row.attachmentName || ''
  publishDialogVisible.value = true
}

const resetForm = () => {
  form.id = null
  form.courseId = null
  form.title = ''
  form.content = ''
  form.deadline = null
  form.file = null
  form.fileName = ''
}

const triggerAttachmentInput = () => {
  attachmentInputRef.value?.click()
}

const handleAttachmentChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    form.file = file
    form.fileName = file.name
  }
}

const clearAttachment = () => {
  form.file = null
  form.fileName = ''
  if (attachmentInputRef.value) attachmentInputRef.value.value = ''
}

const handleSubmit = async () => {
  if (!form.courseId) {
    ElMessage.warning('请选择课程')
    return
  }
  if (!form.title || !form.title.trim()) {
    ElMessage.warning('请输入作业标题')
    return
  }
  submitting.value = true
  try {
    const formData = new FormData()
    if (isEdit.value) formData.append('id', form.id)
    formData.append('courseId', form.courseId)
    formData.append('title', form.title.trim())
    if (form.content) formData.append('content', form.content)
    if (form.deadline) formData.append('deadline', form.deadline)
    if (form.file) formData.append('file', form.file)

    if (isEdit.value) {
      await updateHomework(formData)
      ElMessage.success('编辑成功')
    } else {
      await publishHomework(formData)
      ElMessage.success('发布成功')
    }
    publishDialogVisible.value = false
    fetchList()
  } catch (error) {
    console.error('提交失败', error)
  } finally {
    submitting.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除作业"${row.title}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteHomework(row.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      console.error('删除失败', error)
    }
  }).catch(() => {})
}

const openSubmissionDialog = async (row) => {
  currentHomework.value = row
  submissionDialogVisible.value = true
  submissionLoading.value = true
  try {
    const res = await getSubmissionList(row.id)
    submissionList.value = res.data ?? []
  } catch (error) {
    console.error('获取提交列表失败', error)
  } finally {
    submissionLoading.value = false
  }
}

const openGradeDialog = (row) => {
  gradeForm.submissionId = row.id
  gradeForm.studentName = row.studentName
  gradeForm.studentNo = row.studentNo
  gradeForm.score = row.score
  gradeForm.comment = row.comment
  gradeDialogVisible.value = true
}

const handleGrade = async () => {
  if (gradeForm.score === null || gradeForm.score === undefined) {
    ElMessage.warning('请输入成绩')
    return
  }
  grading.value = true
  try {
    await gradeHomework({
      submissionId: gradeForm.submissionId,
      score: gradeForm.score,
      comment: gradeForm.comment
    })
    ElMessage.success('批改成功')
    gradeDialogVisible.value = false
    if (currentHomework.value) {
      openSubmissionDialog(currentHomework.value)
    }
    fetchList()
  } catch (error) {
    console.error('批改失败', error)
  } finally {
    grading.value = false
  }
}

// 下载附件（带404检测）
const handleDownloadAttachment = (row) => {
  downloadAttachment(row.attachmentUrl, row.attachmentName || '作业附件')
}

onMounted(() => {
  fetchCourses()
  fetchList()
})
</script>

<style scoped>
.file-name {
  margin-left: 10px;
  color: #606266;
  font-size: 13px;
}
</style>
