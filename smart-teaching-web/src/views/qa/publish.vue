<template>
  <div class="qa-publish-page">
    <div class="page-card">
      <div class="page-header">
        <span class="page-title">发布问题</span>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width: 700px">
        <el-form-item label="选择课程" prop="courseId">
          <el-select v-model="form.courseId" filterable style="width: 100%">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题标签" prop="tag">
          <el-radio-group v-model="form.tag">
            <el-radio value="知识点">知识点</el-radio>
            <el-radio value="作业">作业</el-radio>
            <el-radio value="考试">考试</el-radio>
            <el-radio value="其他">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="问题标题" prop="title">
          <el-input v-model="form.title" placeholder="请简要描述你的问题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="问题内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请详细描述你的问题，便于他人解答"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="匿名发布">
          <el-switch v-model="form.isAnonymous" />
          <span style="color: var(--text-secondary); margin-left: 8px; font-size: 13px">开启后将隐藏你的身份信息</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">发布</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { publishQuestion } from '@/api/qa'
import { getMyCourses } from '@/api/selection'

const router = useRouter()
const formRef = ref(null)
const courseList = ref([])

const form = reactive({
  courseId: null,
  tag: '知识点',
  title: '',
  content: '',
  isAnonymous: false
})

const rules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  tag: [{ required: true, message: '请选择标签', trigger: 'change' }],
  title: [{ required: true, message: '请输入问题标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入问题内容', trigger: 'blur' }]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    await publishQuestion(form)
    ElMessage.success('发布成功')
    router.push('/qa/list')
  })
}

onMounted(async () => {
  try {
    const res = await getMyCourses()
    // /selection/my 返回的是分页结构，课程在 records 中
    courseList.value = res.data?.records || []
  } catch (e) {}
})
</script>
