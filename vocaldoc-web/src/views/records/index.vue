<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getRecordList, uploadRecord, deleteRecord } from '@/api/voice'
import { analyzeVoice } from '@/api/analysis'
import type { VoiceRecord } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoCamera, Delete, Refresh, Upload, Search } from '@element-plus/icons-vue'

const records = ref<VoiceRecord[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const uploadLoading = ref(false)
const dialogVisible = ref(false)
const selectedFile = ref<File | null>(null)
const recordDescription = ref('')
const recordType = ref('SCALE')

const recordTypes = [
  { value: 'VOWEL', label: '元音练习' },
  { value: 'DURATION', label: '声时长' },
  { value: 'SONG', label: '完整曲目' }
]

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '待处理', type: 'info' },
  1: { label: '处理中', type: 'warning' },
  2: { label: '已完成', type: 'success' },
  3: { label: '失败', type: 'danger' }
}

async function loadRecords() {
  loading.value = true
  try {
    const res = await getRecordList({ current: currentPage.value, size: pageSize.value })
    records.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('Failed to load records:', e)
    records.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadRecords()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  loadRecords()
}

function handleFileChange(file: any) {
  selectedFile.value = file.raw
  return false
}

async function handleUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请选择要上传的文件')
    return
  }
  
  uploadLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('description', recordDescription.value)
    formData.append('recordType', recordType.value)
    
    await uploadRecord(formData)
    ElMessage.success('上传成功')
    dialogVisible.value = false
    selectedFile.value = null
    recordDescription.value = ''
    loadRecords()
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  } finally {
    uploadLoading.value = false
  }
}

async function handleAnalyze(record: VoiceRecord) {
  try {
    await ElMessageBox.confirm('确定要分析这条录音吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await analyzeVoice(record.id)
    ElMessage.success('分析完成')
    loadRecords()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '分析失败')
    }
  }
}

async function handleDelete(record: VoiceRecord) {
  try {
    await ElMessageBox.confirm('确定要删除这条录音吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteRecord(record.id)
    ElMessage.success('删除成功')
    loadRecords()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

function formatDuration(seconds: number) {
  if (!seconds) return '-'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

function formatFileSize(bytes: number) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

onMounted(() => {
  loadRecords()
})
</script>

<template>
  <div class="records-page">
    <div class="page-header">
      <h2 class="page-title">嗓音档案</h2>
      <el-button type="primary" :icon="Upload" @click="dialogVisible = true">
        上传录音
      </el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="fileName" label="文件名" min-width="150" />
        <el-table-column prop="recordType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ recordTypes.find(r => r.value === row.recordType)?.label || row.recordType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="100">
          <template #default="{ row }">
            {{ formatDuration(row.duration) }}
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="大小" width="100">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'">
              {{ statusMap[row.status]?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="上传时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              :icon="Refresh"
              :disabled="row.status === 2"
              @click="handleAnalyze(row)"
            >
              分析
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              :icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="上传录音" width="500px">
      <el-form label-width="80px">
        <el-form-item label="录音类型">
          <el-select v-model="recordType" placeholder="请选择类型">
            <el-option
              v-for="item in recordTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择文件">
          <el-upload
            class="upload-demo"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            accept="audio/*"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持mp3、wav、flac、m4a格式</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="recordDescription"
            type="textarea"
            placeholder="请输入录音描述"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploadLoading" @click="handleUpload">
          上传
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.records-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.el-upload__tip {
  margin-top: 8px;
  color: #909399;
}
</style>
