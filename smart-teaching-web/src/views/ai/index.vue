<template>
  <div class="ai-page">
    <div class="ai-container page-card">
      <!-- 左侧角色切换 -->
      <div class="ai-sidebar">
        <div class="ai-title">
          <el-icon :size="24" color="#409eff"><Cpu /></el-icon>
          <span>AI 助教</span>
        </div>
        <el-menu :default-active="mode" @select="handleModeChange">
          <el-menu-item index="student">
            <el-icon><Reading /></el-icon> 知识点答疑
          </el-menu-item>
          <el-menu-item v-if="userStore.isTeacher || userStore.isAdmin" index="teacher">
            <el-icon><EditPen /></el-icon> 作业评语生成
          </el-menu-item>
          <el-menu-item v-if="userStore.isTeacher || userStore.isAdmin" index="analysis">
            <el-icon><TrendCharts /></el-icon> 学业分析
          </el-menu-item>
        </el-menu>
        <div class="remaining-count">
          <el-icon><MagicStick /></el-icon>
          今日剩余：<strong>{{ remainingCount }}</strong> 次
        </div>
      </div>

      <!-- 右侧对话区 -->
      <div class="ai-chat">
        <div class="chat-messages" ref="messagesRef">
          <div v-for="(msg, idx) in messages" :key="idx" class="chat-message" :class="msg.role">
            <div class="message-avatar">
              <el-avatar :size="32" :type="msg.role === 'user' ? 'primary' : 'success'">
                {{ msg.role === 'user' ? '我' : 'AI' }}
              </el-avatar>
            </div>
            <div class="message-bubble">
              <div class="message-content">{{ msg.content }}</div>
            </div>
          </div>
          <div v-if="loading" class="chat-message ai">
            <div class="message-avatar">
              <el-avatar :size="32" type="success">AI</el-avatar>
            </div>
            <div class="message-bubble">
              <el-icon class="is-loading"><Loading /></el-icon> 正在思考...
            </div>
          </div>
        </div>

        <!-- 快捷提问（学生模式） -->
        <div v-if="mode === 'student' && messages.length <= 1" class="quick-questions">
          <el-tag v-for="q in quickQuestions" :key="q" class="quick-tag" @click="sendMessage(q)">
            {{ q }}
          </el-tag>
        </div>

        <!-- 输入区 -->
        <div class="chat-input">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            placeholder="输入你的问题..."
            @keydown.enter.exact="handleSend"
            resize="none"
          />
          <el-button type="primary" :loading="loading" @click="handleSend">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { Cpu, Reading, EditPen, TrendCharts, MagicStick, Loading } from '@element-plus/icons-vue'
import { aiAnswer, aiGenerateComment, aiAnalysis, getRemainingCount } from '@/api/ai'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const mode = ref('student')
const messages = ref([
  { role: 'ai', content: '你好！我是 AI 助教，可以帮你解答知识点问题、生成作业评语或分析学业情况。请问有什么可以帮你的？' }
])
const inputText = ref('')
const loading = ref(false)
const remainingCount = ref(10)
const messagesRef = ref(null)

const quickQuestions = [
  '什么是闭包？',
  '解释一下 TCP 三次握手',
  '快速排序的时间复杂度是多少？',
  '什么是 RESTful API？'
]

const handleModeChange = (val) => {
  mode.value = val
  messages.value = [{ role: 'ai', content: getModeWelcome(val) }]
}

const getModeWelcome = (m) => {
  const welcomes = {
    student: '你好！我是 AI 知识点答疑助手，请输入你想了解的知识点。',
    teacher: '你好！我可以帮你生成作业评语，请描述学生作业情况。',
    analysis: '你好！我可以分析学生学业情况，请输入学生ID或选择学生。'
  }
  return welcomes[m] || '你好！'
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const sendMessage = (text) => {
  inputText.value = text
  handleSend()
}

const handleSend = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true
  scrollToBottom()

  try {
    let res
    if (mode.value === 'student') {
      res = await aiAnswer({ question: text })
    } else if (mode.value === 'teacher') {
      res = await aiGenerateComment({ description: text })
    } else {
      res = await aiAnalysis(text)
    }
    messages.value.push({ role: 'ai', content: res.data?.answer || res.data || '暂无回答' })
    remainingCount.value = Math.max(0, remainingCount.value - 1)
  } catch (e) {
    messages.value.push({ role: 'ai', content: '抱歉，AI 服务暂时不可用，请稍后重试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const fetchRemaining = async () => {
  try {
    const res = await getRemainingCount()
    remainingCount.value = res.data || 10
  } catch (e) {}
}

onMounted(() => {
  fetchRemaining()
})
</script>

<style scoped>
.ai-container {
  display: flex;
  height: calc(100vh - 140px);
  padding: 0;
  overflow: hidden;
}

.ai-sidebar {
  width: 220px;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
}

.ai-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 20px;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid var(--border-color);
}

.remaining-count {
  margin-top: auto;
  padding: 16px;
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 6px;
  border-top: 1px solid var(--border-color);
}

.ai-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.chat-message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.chat-message.user {
  flex-direction: row-reverse;
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
}

.chat-message.ai .message-bubble {
  background: #f0f9eb;
  color: var(--text-regular);
}

.chat-message.user .message-bubble {
  background: var(--primary-color);
  color: #fff;
}

.quick-questions {
  padding: 0 20px 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-tag {
  cursor: pointer;
}

.chat-input {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--border-color);
}

.chat-input .el-textarea {
  flex: 1;
}
</style>
