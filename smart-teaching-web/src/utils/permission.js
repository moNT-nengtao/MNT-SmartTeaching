import { useUserStore } from '@/store/user'

/**
 * 判断当前用户是否拥有指定角色
 * @param {string|string[]} roles 角色或角色数组
 * @returns {boolean}
 */
export function hasRole(roles) {
  const userStore = useUserStore()
  const currentRole = userStore.role
  if (!currentRole) return false
  if (Array.isArray(roles)) {
    return roles.includes(currentRole)
  }
  return currentRole === roles
}

/**
 * 判断当前用户是否拥有指定菜单权限
 * @param {string} menu 菜单标识
 * @returns {boolean}
 */
export function hasPermission(menu) {
  const userStore = useUserStore()
  const menus = userStore.menus || []
  return menus.includes(menu)
}

/**
 * 角色常量
 */
export const ROLES = {
  ADMIN: 'admin',
  TEACHER: 'teacher',
  STUDENT: 'student'
}
