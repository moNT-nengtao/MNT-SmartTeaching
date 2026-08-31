<template>
  <div class="evaluation-student-page">
    <div class="page-card">
      <div class="page-header">
        <span class="page-title">课程评价</span>
      </div>
      <el-table :data="courseList" v-loading="loading" border stripe>
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="semester" label="学期" width="140" />
        <el-table-column label="评价状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.isEvaluated" type="success" size="small">已评价</el-tag>
            <el-tag v-else-if="!row.canEvaluate" type="info" size="small">成绩未出</el-tag>
            <el-tag v-else type="warning" size="small">待评价</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :disabled="!row.canEvaluate"
              @click="handleEvaluate(row)"
            >
              {{ row.isEvaluated ? '已评价' : row.canEvaluate ? '去评价' : '成绩未出' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="courseList.length === 0 && !loading" description="暂无可评价的课程" />
    </div>

    <!-- 评价表单弹窗 -->
    <el-dialog v-model="dialogVisible" :title="`评价：${currentCourse?.courseName}`" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="授课能力" prop="teachingAbility">
          <el-rate v-model="form.teachingAbility" show-text :texts="rateTexts" />
        </el-form-item>
        <el-form-item label="课堂氛围" prop="classAtmosphere">
          <el-rate v-model="form.classAtmosphere" show-text :texts="rateTexts" />
        </el-form-item>
        <el-form-item label="知识点讲解" prop="knowledgeClarity">
          <el-rate v-model="form.knowledgeClarity" show-text :texts="rateTexts" />
        </el-form-item>
        <el-form-item label="作业批改" prop="homeworkFeedback">
          <el-rate v-model="form.homeworkFeedback" show-text :texts="rateTexts" />
        </el-form-item>
        <el-form-item label="答疑服务" prop="qaService">
          <el-rate v-model="form.qaService" show-text :texts="rateTexts" />
        </el-form-item>
        <el-form-item label="文字评价" prop="comment">
          <el-input
            v-model="form.comment"
            type="textarea"
            :rows="4"
            placeholder="请写下你对这门课程的评价和建议..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getEvaluableCourses, submitEvaluation } from '@/api/evaluation'

const loading = ref(false)
const courseList = ref([])
const dialogVisible = ref(false)
const currentCourse = ref(null)
const formRef = ref(null)

const rateTexts = ['很差', '较差', '一般', '较好', '很好']

const form = reactive({
  courseId: null,
  teachingAbility: 5,
  classAtmosphere: 5,
  knowledgeClarity: 5,
  homeworkFeedback: 5,
  qaService: 5,
  comment: ''
})

const rules = {
  teachingAbility: [{ required: true, message: '请评分', trigger: 'change' }],
  classAtmosphere: [{ required: true, message: '请评分', trigger: 'change' }],
  knowledgeClarity: [{ required: true, message: '请评分', trigger: 'change' }],
  homeworkFeedback: [{ required: true, message: '请评分', trigger: 'change' }],
  qaService: [{ required: true, message: '请评分', trigger: 'change' }],
  comment: [{ required: true, message: '请填写文字评价', trigger: 'blur' }]
}

const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await getEvaluableCourses()
    courseList.value = res.data || []
  } catch (e) {
    console.error('获取可评价课程失败:', e)
  } finally {
    loading.value = false
  }
}

const handleEvaluate = (row) => {
  currentCourse.value = row
  form.courseId = row.id
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await submitEvaluation(form)
      ElMessage.success('评价提交成功，感谢你的反馈！')
      dialogVisible.value = false
      fetchCourses()
    } catch (e) {
      ElMessage.error('提交失败，请重试')
    }
  })
}

onMounted(() => {
  fetchCourses()
})
</script>