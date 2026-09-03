<template>
  <div class="notice-publish-page">
    <div class="page-card">
      <div class="page-header">
        <span class="page-title">{{ isEdit ? '编辑公告' : '发布公告' }}</span>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width: 800px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="公告类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="system">全校公告</el-radio>
            <el-radio value="course" v-if="userStore.isTeacher">课程公告</el-radio>
            <el-radio value="notice">普通通知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="关联课程" v-if="form.type === 'course'" prop="courseId">
          <el-select v-model="form.courseId" filterable style="width: 300px">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否置顶" prop="isTop">
          <el-switch v-model="form.isTop" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="12"
            placeholder="请输入公告内容"
            maxlength="2000"
            show-word-limit
          />
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
import { publishNotice, updateNotice } from '@/api/notice'
import { getCourseList } from '@/api/course'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const isEdit = ref(false)
const courseList = ref([])

const form = reactive({
  id: null,
  title: '',
  type: 'system',
  courseId: null,
  isTop: false,
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (isEdit.value) {
      await updateNotice(form)
    } else {
      await publishNotice(form)
    }
    ElMessage.success('发布成功')
    router.push('/notice/list')
  })
}

// 课程公告需选择教师自己授课的课程
const loadCourses = async () => {
  if (!userStore.isTeacher) return
  try {
    const res = await getCourseList({
      pageNum: 1,
      pageSize: 100,
      status: 1,
      teacherId: userStore.userInfo?.id
    })
    // /course/list 返回字段为 id + name，统一转成前端需要的 id + courseName
    courseList.value = (res.data?.records || []).map((c) => ({
      id: c.id,
      courseName: c.name
    }))
  } catch (e) {
    courseList.value = []
  }
}

onMounted(() => {
  loadCourses()
})
</script>
