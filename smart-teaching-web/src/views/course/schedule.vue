<template>
  <div class="schedule-page">
    <SearchForm
      :model-value="searchParams"
      :fields="searchFields"
      @update:model-value="Object.assign(searchParams, $event)"
      @search="fetchList"
      @reset="fetchList"
    />

    <div class="page-card">
      <div class="page-header">
        <span class="page-title">排课管理</span>
        <div>
          <el-button :icon="Download" @click="handleExport">导出课表</el-button>
          <el-button type="primary" :icon="Plus" @click="handleAdd">手动排课</el-button>
          <el-button type="success" :icon="Grid" @click="handleBatch">批量排课</el-button>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column label="课程" min-width="140">
          <template #default="{ row }">{{ courseNameText(row) }}</template>
        </el-table-column>
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="day" label="日期" width="80">
          <template #default="{ row }">{{ weekdayText(row.day) }}</template>
        </el-table-column>
        <el-table-column prop="lesson" label="节次" width="100">
          <template #default="{ row }">{{ sectionText(row.lesson) }}</template>
        </el-table-column>
        <el-table-column prop="room" label="教室" width="100" />
        <el-table-column prop="week" label="周次" width="100" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-model:page="searchParams.pageNum"
        v-model:page-size="searchParams.pageSize"
        :total="total"
        @change="fetchList"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑排课' : '新增排课'" width="550px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" filterable :teleported="false" style="width: 100%">
            <el-option v-for="c in courseList" :key="c.id" :label="courseOptionLabel(c)" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherId">
          <el-select v-model="form.teacherId" filterable :teleported="false" style="width: 100%">
            <el-option v-for="t in teacherList" :key="t.id" :label="teacherOptionLabel(t)" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select v-model="form.classId" filterable :teleported="false" style="width: 100%">
            <el-option v-for="c in classList" :key="c.id" :label="c.name || c.className" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="weekday">
          <el-select v-model="form.weekday" :teleported="false" style="width: 100%">
            <el-option v-for="i in 7" :key="i" :label="weekdayText(i)" :value="i" />
          </el-select>
        </el-form-item>
        <el-form-item label="节次" prop="lesson">
          <el-select v-model="form.lesson" :teleported="false" style="width: 100%">
            <el-option v-for="item in sectionOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="教室" prop="room">
          <el-input v-model="form.room" />
        </el-form-item>
        <el-form-item label="周次" prop="week">
          <el-input v-model="form.week" placeholder="如：1-16" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量排课对话框 -->
    <el-dialog v-model="batchDialogVisible" title="批量排课" width="900px">
      <div style="display:flex; gap:16px;">
        <el-form :model="batchForm" label-width="100px" style="flex: 1;">
          <el-form-item label="课程">
            <el-select v-model="batchForm.courseId" filterable :teleported="false" style="width: 100%">
              <el-option v-for="c in courseList" :key="c.id" :label="courseOptionLabel(c)" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="授课教师">
            <el-select v-model="batchForm.teacherId" filterable :teleported="false" style="width: 100%">
              <el-option v-for="t in teacherList" :key="t.id" :label="teacherOptionLabel(t)" :value="t.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="班级">
            <el-select v-model="batchForm.classIds" multiple filterable :teleported="false" style="width: 100%">
              <el-option v-for="c in classList" :key="c.id" :label="c.name || c.className" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="星期">
            <el-checkbox-group v-model="batchForm.weekdays">
              <el-checkbox v-for="i in 7" :key="i" :label="i">{{ weekdayText(i) }}</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="节次">
            <el-checkbox-group v-model="batchForm.lessons">
              <el-checkbox v-for="item in sectionOptions" :key="item.value" :label="item.value">{{ item.label }}</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="周次">
            <el-input v-model="batchForm.weeks" placeholder="如：1-16 或 1,3,5,7 或 1-8,10-16" />
          </el-form-item>
          <el-form-item label="教室(可选)">
            <el-input v-model="batchForm.room" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="generateBatch">生成排课预览</el-button>
          </el-form-item>
        </el-form>
        <div style="flex: 1; max-height: 420px; overflow:auto;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; gap:12px;">
            <div style="display:flex; gap:12px; align-items:center;">
              <div>预览 (共 <strong>{{ combinedPreview.length }}</strong> 条)</div>
              <el-button size="small" @click="openFullPreview">全览</el-button>
            </div>
            <div style="display:flex; gap:8px; align-items:center;">
              <el-upload
                :show-file-list="false"
                :before-upload="handleImportFile"
                accept=".csv,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel"
              >
                <el-button size="small">导入课表</el-button>
              </el-upload>
              <el-button type="primary" size="small" :disabled="!canConfirmImport" @click="confirmImport">确认导入</el-button>
            </div>
          </div>

          <el-table :data="combinedPreview" size="small" stripe border>
            <el-table-column prop="courseName" label="课程" />
            <el-table-column prop="teacherName" label="教师" width="110" />
            <el-table-column prop="className" label="班级" width="110" />
            <el-table-column prop="weekday" label="星期" width="80">
              <template #default="{row}">{{ weekdayText(row.weekday) }}</template>
            </el-table-column>
            <el-table-column prop="lesson" label="节次" width="80">
              <template #default="{row}">{{ sectionText(row.lesson) }}</template>
            </el-table-column>
            <el-table-column prop="room" label="教室" width="100" />
            <el-table-column prop="weekStr" label="周次" width="120" />
            <el-table-column label="状态" width="200">
              <template #default="{row}">
                <el-tag type="danger" v-if="row.conflict">{{ row.conflict }}</el-tag>
                <el-tag type="warning" v-else-if="row.mappingError">映射缺失</el-tag>
                <el-tag type="success" v-else>可导入</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 全览弹窗 -->
    <el-dialog v-model="fullPreviewDialogVisible" title="批量排课全览" width="90%" :modal-append-to-body="true">
      <div style="max-height:70vh; overflow:auto;">
        <el-table :data="combinedPreview" stripe border style="width:100%">
          <el-table-column prop="courseName" label="课程" />
          <el-table-column prop="teacherName" label="教师" width="140" />
          <el-table-column prop="className" label="班级" width="140" />
          <el-table-column prop="weekday" label="星期" width="100">
            <template #default="{row}">{{ weekdayText(row.weekday) }}</template>
          </el-table-column>
          <el-table-column prop="lesson" label="节次" width="100">
            <template #default="{row}">{{ sectionText(row.lesson) }}</template>
          </el-table-column>
          <el-table-column prop="room" label="教室" width="120" />
          <el-table-column prop="weekStr" label="周次" width="140" />
          <el-table-column label="状态" width="200">
            <template #default="{row}">
              <el-tag type="danger" v-if="row.conflict">{{ row.conflict }}</el-tag>
              <el-tag type="warning" v-else-if="row.mappingError">映射缺失</el-tag>
              <el-tag type="success" v-else>可导入</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="importErrors.length" style="margin-top:12px;">
          <div style="font-weight:600; margin-bottom:6px;">导入映射错误 (示例 { 行号, item })</div>
          <el-table :data="importErrors" size="small" style="width:100%">
            <el-table-column prop="row" label="行号" width="80" />
            <el-table-column label="问题行说明">
              <template #default="{row}">
                <pre style="white-space:pre-wrap; margin:0">{{ JSON.stringify(row.item, null, 2) }}</pre>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="fullPreviewDialogVisible = false">关闭</el-button>
        <el-button type="primary" :disabled="!canConfirmImport" @click="confirmImport">确认导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Grid } from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { getCourseList, getScheduleList, addSchedule, updateSchedule, deleteSchedule, checkScheduleConflict, exportSchedule } from '@/api/course'
import { getUserList } from '@/api/user'
import { getClassList } from '@/api/org'
import * as XLSX from 'xlsx'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const courseList = ref([])
const teacherList = ref([])
const classList = ref([])

const sectionOptions = [
  { label: '第1节', value: 1 },
  { label: '第2节', value: 2 },
  { label: '第3节', value: 3 },
  { label: '第4节', value: 4 },
  { label: '第5节', value: 5 },
  { label: '第6节', value: 6 }
]

const searchParams = reactive({
  pageNum: 1,
  pageSize: 10,
  courseId: ''
})

const searchFields = [
  { prop: 'courseId', label: '课程', type: 'select', options: [] }
]

const form = reactive({
  id: null,
  courseId: null,
  teacherId: null,
  classId: null,
  weekday: 1,
  lesson: null,
  room: '',
  week: '1-16'
})

const rules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  weekday: [{ required: true, message: '请选择星期', trigger: 'change' }],
  lesson: [{ required: true, message: '请选择节次', trigger: 'change' }]
}

// 批量排课（前端生成）状态
const batchDialogVisible = ref(false)
const fullPreviewDialogVisible = ref(false)
const batchForm = reactive({
  courseId: null,
  teacherId: null,
  classIds: [],
  weekdays: [1],
  lessons: [],
  weeks: '1-16',
  room: ''
})
const batchPreview = ref([])
// 从导入文件解析出来的数据
const importedPreview = ref([])
const importFileName = ref('')
const importErrors = ref([])
// 两者合并的视图
const combinedPreview = ref([])


// 解析周次字符串，支持 1-16,1,3,5 的格式，返回断开的周数数组
const parseWeeks = (str) => {
  if (!str) return []
  // 规范化：全角转半角、去除所有空格、中文逗号转英文逗号、中文连字符转英文连字符
  let s = String(str)
    .replace(/\s+/g, '')
    .replace(/，/g, ',')
    .replace(/[\u2010-\u2014\u2212\uFF0D]/g, '-')
    .replace(/周/g, '')

  const parts = s.split(',').map(s => s.trim()).filter(Boolean)
  const weeks = new Set()
  parts.forEach(p => {
    if (p.includes('-')) {
      const [start, end] = p.split('-', 2).map(x => parseInt(x, 10))
      if (!isNaN(start) && !isNaN(end)) {
        for (let i = start; i <= end; i++) weeks.add(i)
      }
    } else {
      const n = parseInt(p, 10)
      if (!isNaN(n)) weeks.add(n)
    }
  })
  return Array.from(weeks).sort((a, b) => a - b)
}


const weeksOverlap = (arr1, arr2) => {
  if (!arr1 || !arr2 || !arr1.length || !arr2.length) return false
  const s = new Set(arr1)
  return arr2.some(x => s.has(x))
}

const makeWeekStrFromArr = (arr) => {
  if (!arr || !arr.length) return ''
  return arr.join(',')
}

// 检查单条排课项与已有排课（tableData）或已生成预览是否冲突
const detectConflict = (entry, existingList) => {
  // existingList 中的每项应该包含: teacherId, classId, weekday, lesson, weeksArr, room(optional)
  for (const ex of existingList) {
    // 跳过自身id（仅在比较服务器已有时）
    if (entry.id && ex.id && entry.id === ex.id) continue
    const exWeeks = ex.weeksArr || parseWeeks(ex.week || ex.weekStr || '')
    const enWeeks = entry.weeksArr || parseWeeks(entry.weekStr || entry.week || '')
    if (!weeksOverlap(exWeeks, enWeeks)) continue
    // 同一节次同一班级冲突
    if (ex.classId === entry.classId && ex.weekday === entry.weekday && ex.lesson === entry.lesson) {
      return '班级冲突'
    }
    // 同一节次同一教师冲突
    if (ex.teacherId === entry.teacherId && ex.weekday === entry.weekday && ex.lesson === entry.lesson) {
      return '教师冲突'
    }
    // 同一节次同一教室冲突（若教室都有值）
    if (entry.room && ex.room && ex.room === entry.room && ex.weekday === entry.weekday && ex.lesson === entry.lesson) {
      return '教室冲突'
    }
  }
  return ''
}

// 将 preview + importedPreview 合并并向后端请求批量冲突检测（若后端未提供该接口则回退到本地检查）
const refreshCombinedAndCheck = async () => {
  // 合并preview数据（优先采用 batchPreview 的字段）
  const merged = []
  const src = batchPreview.value.concat(importedPreview.value)
  for (const it of src) merged.push(Object.assign({}, it))
  combinedPreview.value = merged

  // 如果没有条目就跳过
  if (!merged.length) return

  // 构造提交到后端的 payload（后端期望字段: courseId、teacherId、classId、day、lesson、room、week）
  const payloadItems = merged.map(it => ({
    tempId: it.id,
    courseId: it.courseId,
    teacherId: it.teacherId,
    classId: it.classId,
    day: it.weekday,
    lesson: it.lesson,
    room: it.room,
    week: it.weekStr || it.week || it.week
  }))

  // 优先使用后端单条冲突检查接口（checkScheduleConflict）逐条校验，避免依赖不存在的批量接口
  const existing = tableData.value.map(item => ({
    id: item.id,
    teacherId: item.teacherId,
    classId: item.classId,
    weekday: item.day || item.weekday,
    lesson: item.lesson,
    room: item.room,
    week: item.week || item.weekStr || item.week
  }))

  // 清除旧冲突标记
  for (const it of combinedPreview.value) delete it.conflict

  // 先用本地快速检测，过滤绝大多数冲突（降低后端调用量）
  const combinedSoFar = existing.slice()
  for (const it of combinedPreview.value) {
    const conflict = detectConflict(it, combinedSoFar)
    if (conflict) it.conflict = conflict
    combinedSoFar.push({
      teacherId: it.teacherId,
      classId: it.classId,
      weekday: it.weekday,
      lesson: it.lesson,
      room: it.room,
      week: it.weekStr
    })
  }

  // 对仍未被本地检测标记为冲突的项，调用后端逐条验证（使用 checkScheduleConflict）以覆盖更复杂的后端校验逻辑
  const toCheck = combinedPreview.value.filter(it => !it.conflict)
  if (!toCheck.length) return
  const checks = await Promise.allSettled(toCheck.map(it => checkScheduleConflict({
    courseId: it.courseId,
    teacherId: it.teacherId,
    classId: it.classId,
    day: it.weekday,
    lesson: it.lesson,
    room: it.room,
    week: it.weekStr
  })))
  for (let i = 0; i < checks.length; i++) {
    const res = checks[i]
    const item = toCheck[i]
    if (res.status === 'rejected') {
      // 后端以抛错形式返回冲突信息或状态码，尝试提取信息
      const err = res.reason
      // 可能的错误信息结构：err.response.data.message 或 err.message
      let msg = '后端冲突'
      if (err && err.response && err.response.data && err.response.data.message) msg = err.response.data.message
      else if (err && err.message) msg = err.message
      item.conflict = msg
    } else {
      // 后端返回成功表示无冲突
      delete item.conflict
    }
  }
}


// 生成预览
const generateBatch = async () => {
  batchPreview.value = []
  importedPreview.value = [] // 保持旧导入不被覆盖，导入在 handleImportFile 中处理
  if (!batchForm.courseId || !batchForm.classIds?.length || !batchForm.lessons?.length || !batchForm.weekdays?.length) {
    ElMessage.warning('请先选择课程、班级、星期和节次')
    return
  }
  const weeksArr = parseWeeks(batchForm.weeks)
  if (!weeksArr.length) {
    ElMessage.warning('周次解析失败，请填写正确格式')
    return
  }
  const course = courseList.value.find(c => c.id === batchForm.courseId)
  const teacher = teacherList.value.find(t => t.id === batchForm.teacherId)

  const preview = []
  let idx = 0
  for (const classId of batchForm.classIds) {
    const cls = classList.value.find(c => c.id === classId) || {}
    for (const wd of batchForm.weekdays) {
      for (const ls of batchForm.lessons) {
        const item = {
          id: `temp_${Date.now()}_${idx++}`,
          courseId: batchForm.courseId,
          courseName: course ? (course.name || course.courseName || '') : '',
          teacherId: batchForm.teacherId,
          teacherName: teacher ? teacher.realName || teacher.name || '' : '',
          classId,
          className: cls.name || cls.className || '',
          weekday: wd,
          lesson: ls,
          room: batchForm.room || '',
          weeksArr: weeksArr,
          weekStr: batchForm.weeks
        }
        preview.push(item)
      }
    }
  }
  batchPreview.value = preview
  // 合并并提交到后端检查（后端批量检查接口由后端统一处理），若后端不可用则本地检查
  await refreshCombinedAndCheck()
}

// 确认导入（点击后会逐条调用新增排课接口 addSchedule）
const confirmImport = async () => {
  if (!combinedPreview.value.length) {
    ElMessage.warning('没有可导入的数据')
    return
  }
  // 不能存在冲突或映射错误
  const bad = combinedPreview.value.find(i => i.conflict || i.mappingError)
  if (bad) {
    ElMessage.warning('存在冲突或映射错误，无法导入，请先处理')
    return
  }

  const toImport = combinedPreview.value.slice()
  let success = 0
  for (const it of toImport) {
    const payload = {
      courseId: it.courseId,
      teacherId: it.teacherId,
      classId: it.classId,
      day: it.weekday,
      lesson: it.lesson,
      room: it.room,
      week: it.weekStr
    }
    try {
      await addSchedule(payload)
      success++
    } catch (e) {
      // 记录失败，但继续导入其余
      console.error('导入失败：', payload, e)
    }
  }
  ElMessage.success(`导入完成，成功 ${success}/${toImport.length} 条`)
  batchDialogVisible.value = false
  fetchList()
}

// 仅用于 UI：打开全览弹窗
const openFullPreview = () => {
  fullPreviewDialogVisible.value = true
}

// canConfirmImport 由 combinedPreview 的状态决定
const canConfirmImport = computed(() => {
  if (!combinedPreview.value.length) return false
  for (const it of combinedPreview.value) {
    if (it.conflict) return false
    if (it.mappingError) return false
  }
  return true
})

const exportSchedules = (list, fileName = '课表导出.csv') => {
  if (!list || !list.length) {
    ElMessage.warning('没有可导出的数据')
    return
  }
  const headers = ['课程', '授课教师', '班级', '星期', '节次', '教室', '周次']
  const rows = [headers]
  for (const r of list) {
    const courseName = r.courseName || (courseList.value.find(c => c.id === r.courseId)?.name) || ''
    const teacherName = r.teacherName || (teacherList.value.find(t => t.id === r.teacherId)?.realName) || ''
    const className = r.className || (classList.value.find(c => c.id === r.classId)?.name) || ''
    const weekday = weekdayText(r.day ?? r.weekday)
    const lesson = sectionText(r.lesson)
    const room = r.room || ''
    const week = r.week || r.weekStr || ''
    rows.push([courseName, teacherName, className, weekday, lesson, room, week])
  }
  const csv = rows.map(r => r.map(f => `"${String(f ?? '').replace(/"/g, '""')}"`).join(',')).join('\n')
  // UTF-8 BOM
  const blob = new Blob(["\uFEFF", csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

const handleImportFile = async (file) => {
  importFileName.value = file.name
  importErrors.value = []
  importedPreview.value = []

  // 1. 基础文件校验
  try {
    const name = (file.name || '').toLowerCase()
    if (!(name.endsWith('.xlsx') || name.endsWith('.xls') || name.endsWith('.csv'))) {
      ElMessage.error('不支持的文件类型，请上传 Excel 或 CSV 文件 (.xlsx/.xls/.csv)')
      return false
    }
    const maxSize = 10 * 1024 * 1024 // 10MB限制
    if (file.size && file.size > maxSize) {
      ElMessage.error('文件过大，请上传小于 10MB 的文件')
      return false
    }
  } catch (e) {
    console.warn('文件基础校验失败', e)
  }

  // 2. 解析Excel/CSV文件
  try {
    const arrayBuffer = await file.arrayBuffer()
    const workbook = XLSX.read(arrayBuffer, { type: 'array' })
    const sheetName = workbook.SheetNames && workbook.SheetNames[0]
    if (!sheetName) {
      ElMessage.error('导入文件中未找到工作表，请检查文件')
      return false
    }
    const sheet = workbook.Sheets[sheetName]
    // 解析为JSON，空单元格默认填充空字符串
    const rows = XLSX.utils.sheet_to_json(sheet, { defval: '' })
    if (!rows || !rows.length) {
      ElMessage.error('导入文件内容为空或无法解析，请检查表头与内容格式')
      return false
    }

    const results = []
    let idx = 0
    // 3. 遍历每一行数据解析
    for (let rIndex = 0; rIndex < rows.length; rIndex++) {
      const row = rows[rIndex]
      // 过滤全空行
      if (Object.values(row).every(v => v === '')) continue

      // 3.1 字段匹配（兼容表头带空格、大小写差异）
      const getField = (row, keys) => {
        const normalizedKeys = keys.map(k => k.toLowerCase().replace(/\s+/g, ''))
        for (const [key, value] of Object.entries(row)) {
          const normalizedKey = key.toLowerCase().replace(/\s+/g, '')
          if (normalizedKeys.includes(normalizedKey)) return value
        }
        return ''
      }

      const courseName = getField(row, ['课程', '课程名称', 'course', 'coursename', 'courseName'])
      const teacherName = getField(row, ['授课教师', '教师', 'teacher', 'teachername', 'teacherName'])
      const className = getField(row, ['班级', 'class', 'classname', 'className', 'class_name'])
      const weekdayRaw = getField(row, ['星期', 'weekday', 'day', '周'])
      const lessonRaw = getField(row, ['节次', 'lesson', '节'])
      const room = getField(row, ['教室', 'room'])
      const weekStr = getField(row, ['周次', 'week', 'weeks'])

      // 3.2 解析星期、节次（容错处理，绝对不会出现undefined）
      let weekday = parseInt(weekdayRaw, 10)
      if (isNaN(weekday)) {
        const weekMap = { '周一': 1, '周二': 2, '周三': 3, '周四': 4, '周五': 5, '周六': 6, '周日': 7 }
        const cleanWeekdayRaw = (weekdayRaw || '').replace(/\s+/g, '')
        weekday = weekMap[cleanWeekdayRaw] || null
      }

      let lesson = parseInt(lessonRaw, 10)
      if (isNaN(lesson)) lesson = null

      // 3.3 名称匹配ID（增强容错）
      const normalize = (s) => (s || '').toString().replace(/\s+/g, '').toLowerCase()
      const normCourseName = normalize(courseName)
      const normTeacherName = normalize(teacherName)
      const normClassName = normalize(className)

      // 匹配函数
      const findCourse = () => {
        if (!courseList.value?.length) return null
        let m = courseList.value.find(c => normalize(c.name || c.courseName || '') === normCourseName || (c.code && `${c.code}` === courseName))
        if (m) return m
        if (normCourseName.length >= 2) {
          m = courseList.value.find(c => normalize(c.name || c.courseName || '').includes(normCourseName))
          if (m) return m
        }
        return null
      }
      const findTeacher = () => {
        if (!teacherList.value?.length) return null
        let m = teacherList.value.find(t => normalize(t.realName || t.name || t.username || '') === normTeacherName)
        if (m) return m
        if (normTeacherName.length >= 2) {
          m = teacherList.value.find(t => normalize(t.realName || t.name || t.username || '').includes(normTeacherName))
          if (m) return m
        }
        return null
      }
      const findClass = () => {
        if (!classList.value?.length) return null
        let m = classList.value.find(c => normalize(c.name || c.className || '') === normClassName)
        if (m) return m
        if (normClassName.length >= 2) {
          m = classList.value.find(c => normalize(c.name || c.className || '').includes(normClassName))
          if (m) return m
        }
        return null
      }

      const mappedCourse = findCourse()
      const mappedTeacher = findTeacher()
      const mappedClass = findClass()

      // 3.4 候选建议（用于提示用户）
      const suggest = (list, keyVal) => {
        if (!list?.length) return []
        const s = normalize(keyVal)
        if (!s) return []
        return list
          .map(x => ({ id: x.id, label: (x.name || x.courseName || x.realName || x.className || '').toString() }))
          .filter(x => x.label && normalize(x.label).includes(s))
          .slice(0, 3)
      }
      const courseCandidates = suggest(courseList.value, courseName)
      const teacherCandidates = suggest(teacherList.value, teacherName)
      const classCandidates = suggest(classList.value, className)

      // 3.5 自动采纳唯一匹配的候选
      let finalMappedCourse = mappedCourse
      let finalMappedTeacher = mappedTeacher
      let finalMappedClass = mappedClass
      if (!finalMappedCourse && courseCandidates.length === 1) {
        finalMappedCourse = courseList.value.find(c => c.id === courseCandidates[0].id) || finalMappedCourse
      }
      if (!finalMappedTeacher && teacherCandidates.length === 1) {
        finalMappedTeacher = teacherList.value.find(t => t.id === teacherCandidates[0].id) || finalMappedTeacher
      }
      if (!finalMappedClass && classCandidates.length === 1) {
        finalMappedClass = classList.value.find(c => c.id === classCandidates[0].id) || finalMappedClass
      }

      // 3.6 构建行数据（绝对保证weekday/lesson有值，不会undefined）
      const item = {
        id: `imp_${Date.now()}_${idx++}`,
        courseId: finalMappedCourse?.id || null,
        courseName: courseName || (finalMappedCourse ? (finalMappedCourse.name || finalMappedCourse.courseName) : ''),
        teacherId: finalMappedTeacher?.id || null,
        teacherName: teacherName || (finalMappedTeacher ? (finalMappedTeacher.realName || finalMappedTeacher.name) : ''),
        classId: finalMappedClass?.id || null,
        className: className || (finalMappedClass ? (finalMappedClass.name || finalMappedClass.className) : ''),
        weekday: weekday || 0, // 兜底为0，绝对不会undefined
        lesson: lesson || 0, // 兜底为0，绝对不会undefined
        room: room || '',
        weekStr: weekStr || '',
        suggestions: {
          courses: courseCandidates,
          teachers: teacherCandidates,
          classes: classCandidates
        }
      }

      // 3.7 标记映射错误
      item.mappingError = (!item.courseId || !item.teacherId || !item.classId || !item.weekday || !item.lesson)
      if (item.mappingError) {
        importErrors.value.push({ row: rIndex + 2, item }) // +2 因为Excel第一行是表头，数据从第2行开始
      }
      results.push(item)
    }

    // 4. 处理导入结果
    importedPreview.value = results
    if (importErrors.value.length) {
      fullPreviewDialogVisible.value = true
      ElMessage.error(`导入完成，发现 ${importErrors.value.length} 行映射错误，请在预览中查看并修正后再确认导入`)
    } else {
      ElMessage.success(`导入成功，共 ${results.length} 条记录`)
    }

    // 5. 冲突检测
    await refreshCombinedAndCheck()
    return false // 阻止el-upload自动上传
  } catch (e) {
    console.error('解析导入文件失败', e)
    const text = e?.message ? `：${e.message}` : ''
    ElMessage.error(`解析导入文件失败，请检查文件格式或内容${text}`)
    return false
  }
}


const weekdayText = (d) => ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日'][d] || d

const sectionText = (num) => {
  const found = sectionOptions.find(item => item.value === num)
  return found ? found.label : num
}

const courseOptionLabel = (course) => {
  if (course.code && course.name) return `${course.code} - ${course.name}`
  return course.name || course.courseName || `课程 ${course.id}`
}

const teacherOptionLabel = (teacher) => teacher.realName || teacher.real_name || teacher.name || teacher.username || `教师 ${teacher.id}`

const courseNameText = (row) => {
  if (row.courseName) return row.courseName
  const course = courseList.value.find((item) => item.id === row.courseId)
  return course ? (course.name || course.courseName || '-') : '-'
}

const fetchCourses = async () => {
  const res = await getCourseList({ pageNum: 1, pageSize: 1000 })
  courseList.value = res.data?.list ?? res.data?.records ?? []
  searchFields[0].options = courseList.value.map((course) => ({
    label: courseOptionLabel(course),
    value: course.id
  }))
}

const fetchTeachers = async () => {
  const res = await getUserList({ page: 1, pageSize: 1000, role: 'teacher', status: 1 })
  teacherList.value = res.data?.list ?? res.data?.records ?? []
}

const fetchClasses = async () => {
  const res = await getClassList(null, true)
  classList.value = res.data ?? []
}

const fetchList = async () => {
  loading.value = true
  try {
    const selectedCourse = courseList.value.find((course) => course.id === searchParams.courseId)
    const query = {
      ...searchParams,
      courseName: selectedCourse?.name || ''
    }
    const res = await getScheduleList(query)
    tableData.value = res.data?.list ?? res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, courseId: null, teacherId: null, classId: null, weekday: 1, lesson: null, room: '', week: '1-16' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    courseId: row.courseId,
    teacherId: row.teacherId,
    classId: row.classId,
    weekday: row.day,
    lesson: row.lesson,
    room: row.room,
    week: String(row.week)
  })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该排课记录吗？', '提示', { type: 'warning' })
    .then(async () => {
      await deleteSchedule(row.id)
      ElMessage.success('删除成功')
      fetchList()
    })
    .catch(() => {})
}

const handleBatch = () => {
  // 打开批量排课对话框（前端生成）
  batchDialogVisible.value = true
  // 清空之前的导入/预览
  batchPreview.value = []
  importedPreview.value = []
  combinedPreview.value = []
}

const handleExport = async () => {
  try {
    const selectedCourse = courseList.value.find((course) => course.id === searchParams.courseId)
    const payload = {
      courseId: searchParams.courseId || undefined,
      courseName: selectedCourse?.name || ''
    }
    
    // res 直接就是 Blob 对象
    const blob = await exportSchedule(payload)

    // 从响应头获取文件名（如果后端有返回）
    let filename = `课表导出_${new Date().toISOString().slice(0, 10)}.xlsx`
    
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (e) {
    console.error('导出失败:', e)
    // 错误处理：尝试解析错误信息
    if (e instanceof Blob) {
      try {
        const text = await e.text()
        const errorData = JSON.parse(text)
        ElMessage.error(`导出失败：${errorData.msg || '服务异常'}`)
      } catch {
        ElMessage.error('导出失败，请稍后重试')
      }
    } else {
      ElMessage.error('导出失败，请稍后重试')
    }
  }
}



const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    const submitData = {
      id: form.id,
      courseId: form.courseId,
      teacherId: form.teacherId,
      classId: form.classId,
      day: form.weekday,
      lesson: form.lesson,
      room: form.room,
      week: form.week
    }

    try {
      await checkScheduleConflict(submitData)
    } catch (e) {
      ElMessage.warning('存在排课冲突，请检查')
      return
    }

    if (isEdit.value) {
      await updateSchedule(submitData)
    } else {
      await addSchedule(submitData)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  })
}

onMounted(() => {
  Promise.all([fetchCourses(), fetchTeachers(), fetchClasses(), fetchList()])
})
</script>