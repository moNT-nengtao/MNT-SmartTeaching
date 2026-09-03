<template>
  <div class="ai-page">
    <div class="ai-container page-card">
      <!-- 左侧：模式切换 + 会话列表 -->
      <div class="ai-sidebar">
        <div class="ai-title">
          <el-icon :size="22" color="#409eff"><Cpu /></el-icon>
          <span>AI 助教</span>
        </div>

        <!-- 模式切换 -->
        <div class="mode-group">
          <div class="mode-label">功能模式</div>
          <el-menu :default-active="mode" @select="handleModeChange" class="mode-menu">
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
        </div>

        <!-- 会话列表 -->
        <div class="session-group">
          <div class="session-header">
            <span class="mode-label">历史会话</span>
            <el-button link type="primary" :icon="Plus" @click="handleNewSession">新建</el-button>
          </div>
          <div class="session-list" v-loading="sessionLoading">
            <div
              v-for="session in sessions"
              :key="session.id"
              class="session-item"
              :class="{ active: currentSessionId === session.id }"
              @click="selectSession(session)"
            >
              <span class="session-title">{{ session.title || 'AI对话' }}</span>
              <el-icon class="session-delete" @click.stop="handleDeleteSession(session)"><Delete /></el-icon>
            </div>
            <el-empty v-if="!sessionLoading && !sessions.length" description="暂无会话" :image-size="50" />
          </div>
        </div>

        <!-- 剩余次数 -->
        <div class="remaining-count">
          <el-icon><MagicStick /></el-icon>
          今日剩余：<strong>{{ remainingCount }}</strong> 次
        </div>
      </div>

      <!-- 右侧：对话区 -->
      <div class="ai-chat">
        <!-- 学业分析：学生选择 -->
        <div v-if="mode === 'analysis'" class="analysis-select">
          <span class="analysis-label">选择学生：</span>
          <el-select
            v-model="selectedStudentId"
            placeholder="请选择要分析的学生"
            filterable
            style="width: 280px"
            @change="handleStudentChange"
          >
            <el-option
              v-for="stu in students"
              :key="stu.id"
              :label="`${stu.realName || stu.username}（${stu.username}）`"
              :value="stu.id"
            />
          </el-select>
        </div>

        <!-- 消息流 -->
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
            :placeholder="inputPlaceholder"
            :maxlength="500"
            resize="none"
            @keydown.enter.exact="handleSend"
          />
          <el-button type="primary" :loading="loading" @click="handleSend">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Cpu, Reading, EditPen, TrendCharts, MagicStick, Loading, Plus, Delete } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import {
  aiAnswer, aiGenerateComment, aiAnalysis, aiChat,
  getAiSessions, getAiSessionMessages, deleteAiSession,
  getRemainingCount, getAiStudents
} from '@/api/ai'

const userStore = useUserStore()

// ==================== 基础状态 ====================
const mode = ref('student')
const sessions = ref([])
const sessionLoading = ref(false)
const currentSessionId = ref(null)
const messages = ref([
  { role: 'ai', content: '你好！我是 AI 助教，可以帮你解答知识点问题、生成作业评语或分析学业情况。请问有什么可以帮你的？' }
])
const inputText = ref('')
const loading = ref(false)
const remainingCount = ref(10)
const messagesRef = ref(null)

// 学业分析学生
const students = ref([])
const selectedStudentId = ref(null)

const quickQuestions = [
  '什么是闭包？',
  '解释一下 TCP 三次握手',
  '快速排序的时间复杂度是多少？',
  '什么是 RESTful API？'
]

const inputPlaceholder = computed(() => {
  const map = {
    student: '输入你想了解的知识点问题，回车发送...',
    teacher: '描述学生作业情况，生成个性化评语...',
    analysis: '输入分析重点，如：该生哪些科目需要加强？'
  }
  return map[mode.value] || '输入你的问题...'
})

const getModeWelcome = (m) => {
  const welcomes = {
    student: '你好！我是 AI 知识点答疑助手，请输入你想了解的知识点。',
    teacher: '你好！我可以帮你生成作业评语，请描述学生作业情况。',
    analysis: '你好！请先选择要分析的学生，再输入你的分析诉求。'
  }
  return welcomes[m] || '你好！'
}

// ==================== 滚动 ====================
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// ==================== 会话管理 ====================
const refreshSessions = async () => {
  try {
    const res = await getAiSessions()
    sessions.value = res.data || []
  } catch (e) {
    console.error('获取会话列表失败', e)
  }
}

const refreshRemaining = async () => {
  try {
    const res = await getRemainingCount()
    remainingCount.value = res.data || 0
  } catch (e) {
    console.error('获取剩余次数失败', e)
  }
}

const fetchStudents = async () => {
  if (!userStore.isTeacher && !userStore.isAdmin) return
  try {
    const res = await getAiStudents()
    students.value = res.data || []
  } catch (e) {
    console.error('获取学生列表失败', e)
  }
}

const handleModeChange = (val) => {
  if (loading.value) {
    ElMessage.warning('AI 正在回答中，请稍候')
    return
  }
  mode.value = val
  currentSessionId.value = null
  selectedStudentId.value = null
  messages.value = [{ role: 'ai', content: getModeWelcome(val) }]
}

const handleNewSession = () => {
  if (loading.value) {
    ElMessage.warning('AI 正在回答中，请稍候')
    return
  }
  currentSessionId.value = null
  selectedStudentId.value = null
  messages.value = [{ role: 'ai', content: getModeWelcome(mode.value) }]
}

const selectSession = async (session) => {
  if (loading.value) {
    ElMessage.warning('AI 正在回答中，请稍候')
    return
  }
  currentSessionId.value = session.id
  loading.value = true
  try {
    const res = await getAiSessionMessages(session.id)
    const list = res.data || []
    messages.value = list.map((m) => ({
      role: m.sender === 0 ? 'user' : 'ai',
      content: m.content
    }))
    if (!messages.value.length) {
      messages.value = [{ role: 'ai', content: getModeWelcome(mode.value) }]
    }
    scrollToBottom()
  } catch (e) {
    ElMessage.error('加载会话消息失败')
  } finally {
    loading.value = false
  }
}

const handleDeleteSession = (session) => {
  ElMessageBox.confirm(`确定删除会话"${session.title || 'AI对话'}"吗？删除后不可恢复。`, '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAiSession(session.id)
      ElMessage.success('删除成功')
      if (currentSessionId.value === session.id) {
        currentSessionId.value = null
        messages.value = [{ role: 'ai', content: getModeWelcome(mode.value) }]
      }
      refreshSessions()
    } catch (e) {
      console.error('删除会话失败', e)
    }
  }).catch(() => {})
}

const handleStudentChange = () => {
  // 切换学生时清空当前分析上下文，重新开始
  currentSessionId.value = null
  messages.value = [{ role: 'ai', content: getModeWelcome('analysis') }]
}

// ==================== SSE 流式对话 ====================
const parseSSEBlock = (block, aiMsg) => {
  const lines = block.split('\n')
  let eventName = 'message'
  const dataParts = []
  for (const line of lines) {
    if (line.startsWith('event:')) eventName = line.slice(6).trim()
    else if (line.startsWith('data:')) dataParts.push(line.slice(5).trim())
  }
  const data = dataParts.join('\n')
  if (eventName === 'session' && data) {
    try {
      const obj = JSON.parse(data)
      if (obj.sessionId) {
        currentSessionId.value = obj.sessionId
      }
    } catch (e) {
      console.error('解析会话ID失败', e)
    }
  } else if (eventName === 'message') {
    aiMsg.content += data
    scrollToBottom()
  } else if (eventName === 'error') {
    aiMsg.content = data || 'AI 服务暂时不可用'
  }
  // done / connected 事件无需处理
}

const sendStream = async (text) => {
  const base = import.meta.env.VITE_APP_BASE_API || '/api'
  const token = userStore.token
  // 当前无会话 → 新建并流式；有会话 → 续聊
  const url = currentSessionId.value
    ? `${base}/ollama/chat/stream?sessionId=${currentSessionId.value}&message=${encodeURIComponent(text)}`
    : `${base}/ollama/chat/stream/new?message=${encodeURIComponent(text)}&mode=${mode.value}`

  const resp = await fetch(url, {
    headers: { Authorization: `Bearer ${token}` }
  })
  if (!resp.ok || !resp.body) {
    throw new Error('流式连接失败')
  }

  const reader = resp.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  const aiMsg = { role: 'ai', content: '' }
  messages.value.push(aiMsg)

  // 读取并解析 SSE 事件
  const readChunk = async () => {
    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      let idx
      while ((idx = buffer.indexOf('\n\n')) >= 0) {
        const block = buffer.slice(0, idx)
        buffer = buffer.slice(idx + 2)
        parseSSEBlock(block, aiMsg)
      }
    }
    // 处理末尾未闭合事件
    if (buffer.trim()) {
      parseSSEBlock(buffer, aiMsg)
    }
  }
  await readChunk()
}

// ==================== 发送 ====================
const sendMessage = (text) => {
  inputText.value = text
  handleSend()
}

const handleSend = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  // 学业分析必须选择学生
  if (mode.value === 'analysis' && !selectedStudentId.value) {
    ElMessage.warning('请先选择要分析的学生')
    return
  }

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true
  scrollToBottom()

  try {
    if (mode.value === 'analysis') {
      // 学业分析：后端组装成绩/考勤/作业数据后统一调用，非流式
      const res = await aiAnalysis(selectedStudentId.value, text)
      messages.value.push({ role: 'ai', content: res.data?.answer || '暂无分析结果' })
      // 记录会话ID，同一学生的分析可续聊上下文（切换学生时会重置）
      currentSessionId.value = res.data?.sessionId ?? currentSessionId.value
      remainingCount.value = res.data?.remainingCount ?? remainingCount.value
    } else {
      // 答疑/评语：SSE 流式输出
      await sendStream(text)
    }
    refreshSessions()
    refreshRemaining()
  } catch (e) {
    // 流式失败 → 非流式兜底
    if (mode.value !== 'analysis') {
      try {
        const res = await aiChat({
          message: text,
          mode: mode.value,
          sessionId: currentSessionId.value
        })
        messages.value.push({ role: 'ai', content: res.data?.answer || '暂无回答' })
        currentSessionId.value = res.data?.sessionId ?? currentSessionId.value
        remainingCount.value = res.data?.remainingCount ?? remainingCount.value
        refreshSessions()
        refreshRemaining()
      } catch (e2) {
        messages.value.push({ role: 'ai', content: '抱歉，AI 服务暂时不可用，请稍后重试。' })
      }
    } else {
      messages.value.push({ role: 'ai', content: '抱歉，AI 服务暂时不可用，请稍后重试。' })
    }
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchStudents()
  refreshSessions()
  refreshRemaining()
})
</script>

<style scoped>
.ai-container {
  display: flex;
  height: calc(100vh - 140px);
  padding: 0;
  overflow: hidden;
}

/* ========== 左侧栏 ========== */
.ai-sidebar {
  width: 260px;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.ai-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid var(--border-color);
}

.mode-group,
.session-group {
  padding: 12px 12px 0;
}

.mode-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  padding: 0 4px;
}

.mode-menu {
  border-right: none;
}

.session-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.session-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  margin-bottom: 4px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-regular);
  transition: background-color 0.2s;
}

.session-item:hover {
  background: #f5f7fa;
}

.session-item.active {
  background: var(--primary-color);
  color: #fff;
}

.session-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-delete {
  margin-left: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.session-item:hover .session-delete {
  opacity: 1;
}

.session-delete:hover {
  color: #f56c6c;
}

.remaining-count {
  padding: 14px 16px;
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 6px;
  border-top: 1px solid var(--border-color);
  flex-shrink: 0;
}

/* ========== 右侧对话区 ========== */
.ai-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.analysis-select {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-bottom: 1px solid var(--border-color);
  background: #fafafa;
}

.analysis-label {
  font-size: 13px;
  color: var(--text-regular);
  flex-shrink: 0;
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
  word-break: break-word;
  white-space: pre-wrap;
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
