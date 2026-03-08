<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getComparison, getTrend } from '@/api/analysis'
import { getRecordList } from '@/api/voice'
import { useUserStore } from '@/store'
import type { VoiceRecord } from '@/types'
import { ElMessage } from 'element-plus'
import { DataLine, TrendCharts } from '@element-plus/icons-vue'

const userStore = useUserStore()
const records = ref<VoiceRecord[]>([])
const selectedRecords = ref<VoiceRecord[]>([])
const loading = ref(false)
const comparisonData = ref<any>(null)
const trendData = ref<any>(null)
const activeTab = ref('compare')

const recordTypes = [
  { value: 'SCALE', label: '音阶练习' },
  { value: 'ARPEGGIO', label: '琶音练习' },
  { value: 'SONG', label: '歌曲演唱' },
  { value: 'SCENE', label: '场景练习' }
]

async function loadRecords() {
  loading.value = true
  try {
    const res = await getRecordList({ current: 1, size: 100 })
    records.value = res.data?.records?.filter(r => r.status === 2) || []
  } catch (e) {
    console.error('Failed to load records:', e)
    records.value = []
  } finally {
    loading.value = false
  }
}

function handleRecordSelect(record: VoiceRecord) {
  const index = selectedRecords.value.findIndex(r => r.id === record.id)
  if (index > -1) {
    selectedRecords.value.splice(index, 1)
  } else {
    if (selectedRecords.value.length >= 4) {
      ElMessage.warning('最多选择4条录音进行对比')
      return
    }
    selectedRecords.value.push(record)
  }
}

async function handleCompare() {
  if (selectedRecords.value.length < 2) {
    ElMessage.warning('请至少选择2条录音进行对比')
    return
  }
  
  loading.value = true
  try {
    const recordIds = selectedRecords.value.map(r => r.id)
    const res = await getComparison(recordIds)
    comparisonData.value = res.data
    ElMessage.success('对比完成')
  } catch (e: any) {
    ElMessage.error(e.message || '对比失败')
  } finally {
    loading.value = false
  }
}

async function handleTrend() {
  if (!userStore.userInfo?.id) {
    ElMessage.warning('请先登录')
    return
  }
  
  loading.value = true
  try {
    const res = await getTrend(userStore.userInfo.id, 30)
    trendData.value = res.data
    ElMessage.success('趋势分析完成')
  } catch (e: any) {
    ElMessage.error(e.message || '趋势分析失败')
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

function getRecordTypeLabel(type: string) {
  return recordTypes.find(r => r.value === type)?.label || type
}

onMounted(() => {
  loadRecords()
})
</script>

<template>
  <div class="comparison-page">
    <div class="page-header">
      <h2 class="page-title">历史对比</h2>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="录音对比" name="compare">
        <el-row :gutter="20">
          <el-col :xs="24" :md="8">
            <el-card shadow="never">
              <template #header>
                <div class="card-header">
                  <span>选择录音</span>
                  <el-tag type="info">{{ selectedRecords.length }}/4</el-tag>
                </div>
              </template>
              <div v-loading="loading">
                <div v-if="records.length === 0" class="empty-tip">
                  暂无已分析的录音
                </div>
                <div 
                  v-for="record in records" 
                  :key="record.id"
                  class="record-item"
                  :class="{ active: selectedRecords.some(r => r.id === record.id) }"
                  @click="handleRecordSelect(record)"
                >
                  <el-checkbox 
                    :model-value="selectedRecords.some(r => r.id === record.id)"
                    @click.stop
                  />
                  <div class="record-info">
                    <div class="record-name">{{ record.fileName }}</div>
                    <div class="record-meta">
                      <el-tag size="small">{{ getRecordTypeLabel(record.recordType) }}</el-tag>
                      <span class="record-time">{{ formatDate(record.createdAt) }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <el-button 
                type="primary" 
                class="compare-btn"
                :disabled="selectedRecords.length < 2"
                @click="handleCompare"
              >
                开始对比
              </el-button>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :md="16">
            <el-card shadow="never">
              <template #header>
                <div class="card-header">
                  <span>对比结果</span>
                </div>
              </template>
              <div v-if="comparisonData">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="对比参数">
                    <el-tag v-for="key in Object.keys(comparisonData)" :key="key">
                      {{ key }}
                    </el-tag>
                  </el-descriptions-item>
                </el-descriptions>
                <pre class="comparison-result">{{ JSON.stringify(comparisonData, null, 2) }}</pre>
              </div>
              <el-empty v-else description="请选择录音并点击对比">
                <el-icon :size="48" color="#C0C4CC"><DataLine /></el-icon>
              </el-empty>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
      
      <el-tab-pane label="趋势分析" name="trend">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>30天嗓音变化趋势</span>
              <el-button type="primary" @click="handleTrend" :loading="loading">
                分析趋势
              </el-button>
            </div>
          </template>
          <div v-if="trendData">
            <pre class="trend-result">{{ JSON.stringify(trendData, null, 2) }}</pre>
          </div>
          <el-empty v-else description="点击上方按钮分析趋势">
            <el-icon :size="48" color="#C0C4CC"><TrendCharts /></el-icon>
          </el-empty>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.comparison-page {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.record-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  transition: all 0.2s;
}

.record-item:hover {
  background-color: #F5F7FA;
}

.record-item.active {
  background-color: #ECF5FF;
}

.record-info {
  flex: 1;
  min-width: 0;
}

.record-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.record-time {
  font-size: 12px;
  color: #909399;
}

.empty-tip {
  padding: 20px;
  text-align: center;
  color: #909399;
}

.compare-btn {
  width: 100%;
  margin-top: 16px;
}

.comparison-result,
.trend-result {
  background-color: #F5F7FA;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 13px;
  line-height: 1.6;
}
</style>
