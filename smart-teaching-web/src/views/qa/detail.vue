<template>
  <div class="qa-detail-page">
    <div class="page-card mb-20">
      <el-button link :icon="ArrowLeft" @click="$router.back()">返回列表</el-button>
      <div class="question-header mt-10">
        <h2>
          <el-tag v-if="question.isTop" type="danger" size="small" effect="dark">置顶</el-tag>
          {{ question.title }}
        </h2>
        <div class="question-meta">
          <el-avatar :size="28">{{ question.authorName?.charAt(0) || 'U' }}</el-avatar>
          <span>{{ question.isAnonymous ? '匿名用户' : question.authorName }}</span>
          <el-tag size="small" type="warning">{{ question.tag }}</el-tag>
          <span>{{ question.createTime }}</span>
          <el-button link :icon="Star" @click="handleLike">{{ question.likeCount }}</el-button>
        </div>
      </div>
      <el-divider />
      <div class="question-content">{{ question.content }}</div>
      <div class="question-actions mt-20">
        <el-button v-if="userStore.isTeacher" type="primary" :icon="Top" @click="handleTop">
          {{ question.isTop ? '取消置顶' : '置顶' }}
        </el-button>
        <el-button type="success" :icon="Cpu" @click="askAI">AI 解答</el-button>
      </div>
    </div>

    <!-- 回复列表 -->
    <div class="page-card">
      <div class="page-header">
        <span class="page-title">全部回复（{{ replyList.length }}）</span>
      </div>
      <div v-for="reply in replyList" :key="reply.id" class="reply-item">
        <div class="reply-header">
          <el-avatar :size="28">{{ reply.authorName?.charAt(0) || 'U' }}</el-avatar>
          <span class="reply-author">{{ reply.authorName }}</span>
          <el-tag v-if="reply.isTeacher" type="primary" size="small">教师</el-tag>
          <span class="reply-time">{{ reply.createTime }}</span>
          <el-button link :icon="Star" @click="handleReplyLike(reply)">{{ reply.likeCount }}</el-button>
        </div>
        <div class="reply-content">{{ reply.content }}</div>
      </div>
      <el-empty v-if="replyList.length === 0" description="暂无回复，快来抢沙发吧" />

      <!-- 回复输入框 -->
      <div class="reply-input mt-20">
        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="3"
          placeholder="写下你的回复..."
          maxlength="500"
          show-word-limit
        />
        <div class="text-right mt-10">
          <el-button type="primary" @click="handleReply">提交回复</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Star, Top, Cpu } from '@element-plus/icons-vue'
import { getQuestionDetail, replyQuestion, likeQuestion, likeReply, toggleQuestionTop } from '@/api/qa'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()

const question = ref({})
const replyList = ref([])
const replyContent = ref('')

const fetchDetail = async () => {
  const res = await getQuestionDetail(route.params.id)
  question.value = res.data?.question || {}
  replyList.value = res.data?.replies || []
}

const handleLike = async () => {
  await likeQuestion(question.value.id)
  question.value.likeCount++
}

const handleReplyLike = async (reply) => {
  await likeReply(reply.id)
  reply.likeCount++
}

const handleTop = async () => {
  await toggleQuestionTop(question.value.id, !question.value.isTop)
  question.value.isTop = !question.value.isTop
  ElMessage.success('操作成功')
}

const handleReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  await replyQuestion({ questionId: route.params.id, content: replyContent.value })
  ElMessage.success('回复成功')
  replyContent.value = ''
  fetchDetail()
}

const askAI = () => {
  ElMessage.info('跳转 AI 助教解答功能待实现')
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.question-header h2 {
  font-size: 20px;
  margin-bottom: 12px;
}

.question-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-secondary);
  font-size: 13px;
}

.question-content {
  line-height: 1.8;
  color: var(--text-regular);
  white-space: pre-wrap;
}

.question-actions {
  display: flex;
  gap: 10px;
}

.reply-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.reply-author {
  font-weight: 500;
}

.reply-time {
  color: var(--text-secondary);
  font-size: 12px;
  flex: 1;
}

.reply-content {
  color: var(--text-regular);
  line-height: 1.6;
  padding-left: 36px;
}
</style>
