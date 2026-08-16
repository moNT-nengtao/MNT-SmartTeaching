<template>
  <div class="notice-page">
    <SearchForm v-model="searchParams" :fields="searchFields" @search="fetchList" @reset="fetchList" />

    <div class="page-card">
      <div class="page-header">
        <span class="page-title">通知公告</span>
        <el-button v-if="canPublish" type="primary" :icon="Edit" @click="$router.push('/notice/publish')">发布公告</el-button>
      </div>
      <el-table :data="noticeList" v-loading="loading" border stripe @row-click="handleRead">
        <el-table-column label="标题" min-width="200">
          <template #default="{ row }">
            <span class="notice-title" :class="{ unread: !row.isRead }">
              <el-tag v-if="row.isTop" type="danger" size="small" effect="dark">置顶</el-tag>
              {{ row.title }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="100" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'system' ? 'danger' : row.type === 'course' ? 'warning' : 'info'" size="small">
              {{ row.type === 'system' ? '全校' : row.type === 'course' ? '课程' : '通知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-dot v-if="!row.isRead" type="danger" />
            <span v-else style="color: var(--text-secondary)">已读</span>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="170" />
      </el-table>
      <Pagination v-model:page="searchParams.page" v-model:page-size="searchParams.pageSize" :total="total" @change="fetchList" />
    </div>

    <el-dialog v-model="detailVisible" :title="currentNotice?.title" width="600px">
      <div class="notice-detail">
        <div class="notice-meta">
          <span>发布人：{{ currentNotice?.publisherName }}</span>
          <span>发布时间：{{ currentNotice?.publishTime }}</span>
        </div>
        <el-divider />
        <div class="notice-content" v-html="currentNotice?.content" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Edit } from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { getNoticeList, markNoticeRead } from '@/api/notice'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const canPublish = computed(() => ['admin', 'teacher'].includes(userStore.role))

const loading = ref(false)
const noticeList = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentNotice = ref(null)

const searchParams = reactive({ page: 1, pageSize: 10, keyword: '', isRead: '' })
const searchFields = [
  { prop: 'keyword', label: '关键词', type: 'input' },
  {
    prop: 'isRead',
    label: '状态',
    type: 'select',
    options: [
      { label: '全部', value: '' },
      { label: '未读', value: '0' },
      { label: '已读', value: '1' }
    ]
  }
]

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getNoticeList(searchParams)
    noticeList.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleRead = async (row) => {
  currentNotice.value = row
  detailVisible.value = true
  if (!row.isRead) {
    await markNoticeRead(row.id)
    row.isRead = true
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.notice-title {
  cursor: pointer;
}

.notice-title.unread {
  font-weight: 600;
  color: var(--text-primary);
}

.notice-meta {
  display: flex;
  justify-content: space-between;
  color: var(--text-secondary);
  font-size: 13px;
}

.notice-content {
  line-height: 1.8;
  color: var(--text-regular);
}
</style>
