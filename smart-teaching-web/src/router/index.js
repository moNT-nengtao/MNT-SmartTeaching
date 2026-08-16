import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

// 公开路由
const publicRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  }
]

// 管理员路由
const adminRoutes = [
  {
    path: '/dashboard/admin',
    name: 'AdminDashboard',
    component: () => import('@/views/dashboard/admin.vue'),
    meta: { title: '管理驾驶舱', roles: ['admin'] }
  },
  {
    path: '/user',
    name: 'UserManage',
    component: () => import('@/views/user/index.vue'),
    meta: { title: '用户管理', roles: ['admin'] }
  },
  {
    path: '/org',
    name: 'OrgManage',
    component: () => import('@/views/org/index.vue'),
    meta: { title: '组织架构', roles: ['admin'] }
  },
  {
    path: '/course/list',
    name: 'CourseList',
    component: () => import('@/views/course/list.vue'),
    meta: { title: '课程管理', roles: ['admin'] }
  },
  {
    path: '/course/schedule',
    name: 'CourseSchedule',
    component: () => import('@/views/course/schedule.vue'),
    meta: { title: '排课管理', roles: ['admin'] }
  },
  {
    path: '/selection/manage',
    name: 'SelectionManage',
    component: () => import('@/views/selection/manage.vue'),
    meta: { title: '选课管理', roles: ['admin'] }
  },
  {
    path: '/score/stats',
    name: 'ScoreStats',
    component: () => import('@/views/score/stats.vue'),
    meta: { title: '成绩统计', roles: ['admin'] }
  },
  {
    path: '/notice/publish',
    name: 'NoticePublish',
    component: () => import('@/views/notice/publish.vue'),
    meta: { title: '发布公告', roles: ['admin', 'teacher'] }
  },
  {
    path: '/warning/report',
    name: 'WarningReport',
    component: () => import('@/views/warning/report.vue'),
    meta: { title: '预警报告', roles: ['admin', 'teacher'] }
  },
  {
    path: '/evaluation/ranking',
    name: 'EvaluationRanking',
    component: () => import('@/views/evaluation/ranking.vue'),
    meta: { title: '教师评分榜', roles: ['admin'] }
  }
]

// 教师路由
const teacherRoutes = [
  {
    path: '/dashboard/teacher',
    name: 'TeacherDashboard',
    component: () => import('@/views/dashboard/teacher.vue'),
    meta: { title: '教师工作台', roles: ['teacher'] }
  },
  {
    path: '/score/entry',
    name: 'ScoreEntry',
    component: () => import('@/views/score/entry.vue'),
    meta: { title: '成绩录入', roles: ['teacher'] }
  },
  {
    path: '/attendance/teacher',
    name: 'AttendanceTeacher',
    component: () => import('@/views/attendance/teacher.vue'),
    meta: { title: '课堂签到', roles: ['teacher'] }
  },
  {
    path: '/evaluation/teacher',
    name: 'EvaluationTeacher',
    component: () => import('@/views/evaluation/teacher.vue'),
    meta: { title: '教学评价', roles: ['teacher'] }
  }
]

// 学生路由
const studentRoutes = [
  {
    path: '/dashboard/student',
    name: 'StudentDashboard',
    component: () => import('@/views/dashboard/student.vue'),
    meta: { title: '学业中心', roles: ['student'] }
  },
  {
    path: '/schedule',
    name: 'MySchedule',
    component: () => import('@/views/schedule/index.vue'),
    meta: { title: '我的课表', roles: ['student'] }
  },
  {
    path: '/selection/select',
    name: 'CourseSelection',
    component: () => import('@/views/selection/select.vue'),
    meta: { title: '选课大厅', roles: ['student'] }
  },
  {
    path: '/selection/my',
    name: 'MySelection',
    component: () => import('@/views/selection/my.vue'),
    meta: { title: '我的选课', roles: ['student'] }
  },
  {
    path: '/score/query',
    name: 'ScoreQuery',
    component: () => import('@/views/score/query.vue'),
    meta: { title: '成绩查询', roles: ['student'] }
  },
  {
    path: '/attendance/student',
    name: 'AttendanceStudent',
    component: () => import('@/views/attendance/student.vue'),
    meta: { title: '我的考勤', roles: ['student'] }
  },
  {
    path: '/evaluation/student',
    name: 'EvaluationStudent',
    component: () => import('@/views/evaluation/student.vue'),
    meta: { title: '课程评价', roles: ['student'] }
  }
]

// 通用路由（所有登录用户）
const commonRoutes = [
  {
    path: '/notice/list',
    name: 'NoticeList',
    component: () => import('@/views/notice/list.vue'),
    meta: { title: '通知公告', roles: ['admin', 'teacher', 'student'] }
  },
  {
    path: '/qa/list',
    name: 'QaList',
    component: () => import('@/views/qa/list.vue'),
    meta: { title: '答疑社区', roles: ['admin', 'teacher', 'student'] }
  },
  {
    path: '/qa/detail/:id',
    name: 'QaDetail',
    component: () => import('@/views/qa/detail.vue'),
    meta: { title: '问题详情', roles: ['admin', 'teacher', 'student'], hidden: true }
  },
  {
    path: '/qa/publish',
    name: 'QaPublish',
    component: () => import('@/views/qa/publish.vue'),
    meta: { title: '发布问题', roles: ['student'] }
  },
  {
    path: '/ai',
    name: 'AiAssistant',
    component: () => import('@/views/ai/index.vue'),
    meta: { title: 'AI助教', roles: ['admin', 'teacher', 'student'] }
  },
  {
    path: '/warning/list',
    name: 'WarningList',
    component: () => import('@/views/warning/list.vue'),
    meta: { title: '学业预警', roles: ['admin', 'teacher', 'student'] }
  },
  {
    path: '/user/profile',
    name: 'UserProfile',
    component: () => import('@/views/user/profile.vue'),
    meta: { title: '个人中心', roles: ['admin', 'teacher', 'student'], hidden: true }
  }
]

const redirectMap = {
  admin: '/dashboard/admin',
  teacher: '/dashboard/teacher',
  student: '/dashboard/student'
}

// 布局路由（所有业务页面嵌套在 Layout 下）
const layoutRoutes = {
  path: '/',
  component: () => import('@/components/Layout/index.vue'),
  redirect: (to) => {
    const userStore = useUserStore()
    if (!userStore.token) {
      return '/login'
    }
    return redirectMap[userStore.role] || '/login'
  },
  children: [...adminRoutes, ...teacherRoutes, ...studentRoutes, ...commonRoutes]
}

const router = createRouter({
  history: createWebHistory(),
  routes: [...publicRoutes, layoutRoutes, { path: '/:pathMatch(.*)*', redirect: '/' }]
})

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  document.title = to.meta.title ? `${to.meta.title} - 智慧教学平台` : '智慧教学平台'

  // 公开路由直接放行
  if (to.meta.requiresAuth === false) {
    if (userStore.isLoggedIn && to.path === '/login') {
      try {
        if (!userStore.userInfo) {
          await userStore.fetchUserInfo()
        }
        if (userStore.role) {
          next(redirectMap[userStore.role] || '/')
          return
        }
      } catch (error) {
        userStore.resetState()
      }

      next('/login')
      return
    }
    next()
    return
  }

  // 未登录跳转登录页
  if (!userStore.token) {
    next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    return
  }

  // 没有用户信息则获取
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      userStore.resetState()
      next('/login')
      return
    }
  }

  // 角色权限校验
  const requiredRoles = to.meta.roles
  if (requiredRoles && !requiredRoles.includes(userStore.role)) {
    next('/403')
    return
  }

  next()
})

export default router
