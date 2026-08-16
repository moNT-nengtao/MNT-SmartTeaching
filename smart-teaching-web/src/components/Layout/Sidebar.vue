<template>
  <div class="sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
    <div class="sidebar-logo">
      <el-icon :size="28" color="#fff"><Reading /></el-icon>
      <span v-show="!appStore.sidebarCollapsed" class="logo-text">智慧教学平台</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="appStore.sidebarCollapsed"
      :collapse-transition="false"
      background-color="#001529"
      text-color="#b7bdc6"
      active-text-color="#ffffff"
      router
      unique-opened
    >
      <template v-for="item in menuList" :key="item.path">
        <el-sub-menu v-if="item.children && item.children.length" :index="item.path">
          <template #title>
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </template>
          <el-menu-item
            v-for="child in item.children"
            :key="child.path"
            :index="child.path"
          >
            {{ child.title }}
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item v-else :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

// 根据角色动态生成菜单
const menuList = computed(() => {
  const role = userStore.role
  const menus = []

  // 仪表盘
  const dashboardMap = {
    admin: { path: '/dashboard/admin', title: '管理驾驶舱', icon: 'DataAnalysis' },
    teacher: { path: '/dashboard/teacher', title: '教师工作台', icon: 'DataAnalysis' },
    student: { path: '/dashboard/student', title: '学业中心', icon: 'DataAnalysis' }
  }
  if (dashboardMap[role]) {
    menus.push(dashboardMap[role])
  }

  // 管理员菜单
  if (role === 'admin') {
    menus.push(
      { path: '/user', title: '用户管理', icon: 'User' },
      { path: '/org', title: '组织架构', icon: 'OfficeBuilding' },
      {
        path: '/course',
        title: '课程管理',
        icon: 'Notebook',
        children: [
          { path: '/course/list', title: '课程列表' },
          { path: '/course/schedule', title: '排课管理' }
        ]
      },
      { path: '/selection/manage', title: '选课管理', icon: 'Tickets' },
      { path: '/score/stats', title: '成绩统计', icon: 'TrendCharts' },
      { path: '/evaluation/ranking', title: '教师评分榜', icon: 'Trophy' }
    )
  }

  // 教师菜单
  if (role === 'teacher') {
    menus.push(
      { path: '/score/entry', title: '成绩录入', icon: 'EditPen' },
      { path: '/attendance/teacher', title: '课堂签到', icon: 'CircleCheck' },
      { path: '/evaluation/teacher', title: '教学评价', icon: 'Star' }
    )
  }

  // 学生菜单
  if (role === 'student') {
    menus.push(
      { path: '/schedule', title: '我的课表', icon: 'Calendar' },
      {
        path: '/selection',
        title: '选课中心',
        icon: 'Tickets',
        children: [
          { path: '/selection/select', title: '选课大厅' },
          { path: '/selection/my', title: '我的选课' }
        ]
      },
      { path: '/score/query', title: '成绩查询', icon: 'Document' },
      { path: '/attendance/student', title: '我的考勤', icon: 'Clock' },
      { path: '/evaluation/student', title: '课程评价', icon: 'ChatDotRound' }
    )
  }

  // 通用菜单
  menus.push(
    { path: '/notice/list', title: '通知公告', icon: 'Bell' },
    { path: '/qa/list', title: '答疑社区', icon: 'QuestionFilled' },
    { path: '/ai', title: 'AI助教', icon: 'Cpu' },
    { path: '/warning/list', title: '学业预警', icon: 'Warning' }
  )

  return menus
})
</script>

<style scoped>
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: var(--sidebar-width);
  background-color: #001529;
  transition: width var(--transition-duration);
  overflow: hidden;
  z-index: 100;
}

.sidebar.collapsed {
  width: var(--sidebar-collapsed-width);
}

.sidebar-logo {
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background-color: #002140;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
}

.logo-text {
  letter-spacing: 1px;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item.is-active) {
  background-color: var(--primary-color) !important;
}
</style>
