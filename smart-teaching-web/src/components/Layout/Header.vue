<template>
  <div class="header">
    <div class="header-left">
      <el-icon class="collapse-btn" :size="20" @click="appStore.toggleSidebar()">
        <Fold v-if="!appStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="header-right">
      <!-- 当前时间 -->
      <span class="current-time">{{ currentTime }}</span>

      <!-- 未读通知 -->
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notice-badge">
        <el-icon :size="20" class="header-icon" @click="goToNotice"><Bell /></el-icon>
      </el-badge>

      <!-- 用户下拉 -->
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" :src="avatarSrc" @error="handleAvatarError">
            <span v-if="avatarStage === 'initial'">
              {{ userStore.userInfo?.realName?.charAt(0) || 'U' }}
            </span>
          </el-avatar>
          <span class="username">{{ userStore.userInfo?.realName || '用户' }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon> 个人中心
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <el-icon><SwitchButton /></el-icon> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'
import { getUnreadCount } from '@/api/notice'
import { formatDate } from '@/utils/format'
import defaultAvatar from '../../assets/images/default.png'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const currentTitle = computed(() => route.meta.title || '')
const unreadCount = ref(0)
const currentTime = ref(formatDate(new Date(), 'YYYY-MM-DD HH:mm'))
const avatarStage = ref(userStore.userInfo?.avatar ? 'user' : 'default')
const avatarSrc = computed(() => {
  if (avatarStage.value === 'user') return userStore.userInfo?.avatar
  if (avatarStage.value === 'default') return defaultAvatar
  return undefined
})

watch(
  () => userStore.userInfo?.avatar,
  (avatar) => {
    avatarStage.value = avatar ? 'user' : 'default'
  },
  { immediate: true }
)

let timer = null

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (e) {
    // 忽略
  }
}

const goToNotice = () => {
  router.push('/notice/list')
}

const handleAvatarError = (event) => {
  if (avatarStage.value === 'user') {
    avatarStage.value = 'default'
  } else if (avatarStage.value === 'default') {
    avatarStage.value = 'initial'
  }
}

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/user/profile')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userStore.logout()
      router.push('/login')
    } catch (e) {
      // 取消
    }
  }
}

onMounted(() => {
  fetchUnreadCount()
  timer = setInterval(() => {
    currentTime.value = formatDate(new Date(), 'YYYY-MM-DD HH:mm')
  }, 60000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.header {
  height: var(--header-height);
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  position: relative;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  color: var(--text-regular);
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: var(--primary-color);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.current-time {
  color: var(--text-secondary);
  font-size: 13px;
}

.header-icon {
  cursor: pointer;
  color: var(--text-regular);
  transition: color 0.2s;
}

.header-icon:hover {
  color: var(--primary-color);
}

.notice-badge {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 0 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: var(--text-regular);
}
</style>
