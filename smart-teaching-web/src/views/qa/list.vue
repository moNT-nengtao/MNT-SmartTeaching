<template>
  <div class="qa-list-page">
    <el-row :gutter="20">
      <el-col :span="4">
        <div class="page-card course-sidebar">
          <h3>课程分区</h3>
          <el-menu :default-active="activeCourse" @select="handleCourseSelect">
            <el-menu-item index="all">
              <el-icon><Collection /></el-icon> 全部问题
            </el-menu-item>
            <el-menu-item v-for="c in courseList" :key="c.id" :index="c.id">
              <el-icon><Notebook /></el-icon> {{ c.name }}
            </el-menu-item>
          </el-menu>
        </div>
      </el-col>
      <el-col :span="20">
        <div class="page-card">
          <div class="page-header">
            <span class="page-title">答疑社区</span>
            <el-button v-if="userStore.isStudent" type="primary" :icon="Edit" @click="$router.push('/qa/publish')">发布问题</el-button>
          </div>

          <!-- 标签筛选 -->
          <div class="tag-filter mb-20">
            <el-tag
              v-for="tag in tagList"
              :key="tag"
              :type="activeTag === tag ? 'primary' : 'info'"
              :effect="activeTag === tag ? 'dark' : 'plain'"
              class="tag-item"
              @click="activeTag = activeTag === tag ? '' : tag; fetchList()"
            >
              {{ tag }}
            </el-tag>
          </div>

          <el-table :data="questionList" v-loading="loading" border stripe @row-click="goToDetail">
            <el-table-column label="问题标题" min-width="300">
              <template #default="{ row }">
                <div class="question-title">
                  <el-tag v-if="row.isTop" type="danger" size="small" effect="dark">置顶</el-tag>
                  <el-tag v-if="row.isAnonymous" type="info" size="small">匿名</el-tag>
                  <span>{{ row.title }}</span>
                </div>
                <div class="question-meta">
                  <span>{{ row.courseName }}</span>
                  <el-tag size="small" type="warning">{{ row.tag }}</el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="authorName" label="提问者" width="100" />
            <el-table-column label="回复数" width="80" align="center">
              <template #default="{ row }">
                <el-badge :value="row.replyCount" :max="99" />
              </template>
            </el-table-column>
            <el-table-column label="点赞" width="80" align="center">
              <template #default="{ row }">
                <el-icon><Star /></el-icon> {{ row.likeCount }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="提问时间" width="170" />
          </el-table>
          <Pagination v-model:page="searchParams.page" v-model:page-size="searchParams.pageSize" :total="total" @change="fetchList" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Edit, Collection, Notebook, Star } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getQuestionList, getTagList } from '@/api/qa'
import { getMyCourses } from '@/api/selection'
import { getCourseList } from '@/api/course'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const questionList = ref([])
const total = ref(0)
const courseList = ref([])
const tagList = ref(['知识点', '作业', '考试', '其他'])
const activeCourse = ref('all')
const activeTag = ref('')

const searchParams = reactive({
  page: 1,
  pageSize: 10,
  courseId: '',
  tag: '',
  keyword: ''
})

const handleCourseSelect = (index) => {
  activeCourse.value = index
  searchParams.courseId = index === 'all' ? '' : index
  fetchList()
}

const fetchList = async () => {
  loading.value = true
  try {
    searchParams.tag = activeTag.value
    const res = await getQuestionList(searchParams)
    questionList.value = res.data?.list ?? res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } finally {
    loading.value = false
  }
}

const goToDetail = (row) => {
  router.push(`/qa/detail/${row.id}`)
}

// 课程分区：学生看自己选的课，教师看自己授课的课，管理员看全部课程
const loadCourses = async () => {
  try {
    let list = []
    if (userStore.isStudent) {
      const res = await getMyCourses({ pageNum: 1, pageSize: 100 })
      list = (res.data?.records || []).map((c) => ({ id: c.courseId, name: c.courseName }))
    } else if (userStore.isTeacher) {
      const res = await getCourseList({
        pageNum: 1,
        pageSize: 100,
        status: 1,
        teacherId: userStore.userInfo?.id
      })
      list = (res.data?.records || []).map((c) => ({ id: c.id, name: c.name }))
    } else if (userStore.isAdmin) {
      const res = await getCourseList({ pageNum: 1, pageSize: 100, status: 1 })
      list = (res.data?.records || []).map((c) => ({ id: c.id, name: c.name }))
    }
    courseList.value = list
  } catch (e) {
    courseList.value = []
  }
}

onMounted(() => {
  fetchList()
  loadCourses()
})
</script>

<style scoped>
.course-sidebar {
  position: sticky;
  top: 0;
}

.question-title {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-weight: 500;
}

.question-meta {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: var(--text-secondary);
}

.tag-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  cursor: pointer;
}
</style>
