<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <div class="page-card text-center">
          <el-avatar :size="100" :src="userInfo?.avatar">
            {{ userInfo?.name?.charAt(0) || 'U' }}
          </el-avatar>
          <h2 class="mt-20">{{ userInfo?.name }}</h2>
          <el-tag :type="roleTagType" class="mt-10">{{ roleText }}</el-tag>
          <p class="mt-10" style="color: var(--text-secondary)">
            {{ userInfo?.collegeName }} / {{ userInfo?.majorName }}
          </p>
        </div>
      </el-col>
      <el-col :span="16">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="info">
            <el-form :model="infoForm" label-width="100px" class="page-card">
              <el-form-item label="账号">
                <el-input v-model="infoForm.username" disabled />
              </el-form-item>
              <el-form-item label="姓名">
                <el-input v-model="infoForm.name" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="infoForm.email" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="infoForm.phone" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSaveInfo">保存修改</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="修改密码" name="password">
            <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px" class="page-card">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleChangePwd">修改密码</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { changePassword } from '@/api/auth'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const activeTab = ref('info')
const pwdFormRef = ref(null)

const infoForm = reactive({
  username: '',
  name: '',
  email: '',
  phone: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const roleText = computed(() => {
  const map = { admin: '管理员', teacher: '教师', student: '学生' }
  return map[userStore.role] || userStore.role
})

const roleTagType = computed(() => {
  const map = { admin: 'danger', teacher: 'warning', student: 'primary' }
  return map[userStore.role] || 'info'
})

const handleSaveInfo = () => {
  ElMessage.success('保存成功（接口待对接）')
}

const handleChangePwd = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await changePassword({
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })
      ElMessage.success('密码修改成功，请重新登录')
      await userStore.logout()
      window.location.href = '/login'
    } catch (e) {
      // 错误已处理
    }
  })
}

onMounted(() => {
  if (userInfo.value) {
    infoForm.username = userInfo.value.username || ''
    infoForm.name = userInfo.value.name || ''
    infoForm.email = userInfo.value.email || ''
    infoForm.phone = userInfo.value.phone || ''
  }
})
</script>

<style scoped>
.profile-page {
  max-width: 1000px;
  margin: 0 auto;
}
</style>
