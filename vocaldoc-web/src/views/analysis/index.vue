<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getAnalysisList } from '@/api/analysis'
import { getRecordList } from '@/api/voice'
import type { VoiceAnalysis, VoiceRecord } from '@/types'
import { ElMessage } from 'element-plus'
import { Search, TrendCharts, Warning, Check } from '@element-plus/icons-vue'

const analysisList = ref<VoiceAnalysis[]>([])
const records = ref<VoiceRecord[]>([])
const loading = ref(false)
const selectedRecordId = ref<number | null>(null)
const currentAnalysis = ref<VoiceAnalysis | null>(null)

const analysisParams = [
  { key: 'meanF0', label: '平均基频 (Hz)', normal: '180-250', desc: '反映音高' },
  { key: 'minF0', label: '最低基频 (Hz)', normal: '80-150', desc: '最低音高' },
  { key: 'maxF0', label: '最高基频 (Hz)', normal: '300-500', desc: '最高音高' },
  { key: 'f0Range', label: '音域范围 (Hz)', normal: '200-350', desc: '可用音域' },
  { key: 'f1Mean', label: 'F1均值 (Hz)', normal: '200-800', desc: '第一共振峰' },
  { key: 'f2Mean', label: 'F2均值 (Hz)', normal: '800-2500', desc: '第二共振峰' },
  { key: 'meanDB', label: '平均响度 (dB)', normal: '50-80', desc: '音量水平' },
  { key: 'jitterLocal', label: 'Jitter (%)', normal: '<1.04', desc: '频率微扰' },
  { key: 'shimmerLocal', label: 'Shimmer (%)', normal: '<3.81', desc: '振幅微扰' },
  { key: 'hnr', label: 'HNR (dB)', normal: '>10', desc: '谐噪比' }
]

function isNormal(value: number, key: string): boolean {
  if (!value && value !== 0) return false
  
  const normalRanges: Record<string, [number, number]> = {
    meanF0: [180, 250],
    minF0: [80, 150],
    maxF0: [300, 500],
    f0Range: [200, 350],
    f1Mean: [200, 800],
    f2Mean: [800, 2500],
    meanDB: [50, 80],
    jitterLocal: [0, 1.04],
    shimmerLocal: [0, 3.81],
    hnr: [10, 100]
  }
  
  const range = normalRanges[key]
  if (!range) return true
  return value >= range[0] && value <= range[1]
}

function getValue(analysis: any, key: string): number {
  const value = analysis[key]
  return typeof value === 'number' ? value : parseFloat(value) || 0
}

async function loadRecords() {
  try {
    const res = await getRecordList({ current: 1, size: 100 })
    records.value = res.data?.records?.filter(r => r.status === 2) || []
  } catch (e) {
    console.error('Failed to load records:', e)
  }
}

async function loadAnalysis() {
  loading.value = true
  try {
    const res = await getAnalysisList({ current: 1, size: 50 })
    analysisList.value = res.data?.records || []
    
    if (selectedRecordId.value) {
      currentAnalysis.value = analysisList.value.find(
        a => a.recordId === selectedRecordId.value
      ) || null
    }
  } catch (e) {
    console.error('Failed to load analysis:', e)
    analysisList.value = []
  } finally {
    loading.value = false
  }
}

function handleRecordSelect(recordId: number) {
  selectedRecordId.value = recordId
  currentAnalysis.value = analysisList.value.find(
    a => a.recordId === recordId
  ) || null
}

function formatValue(value: number, key: string): string {
  if (!value && value !== 0) return '-'
  
  if (['jitterLocal', 'jitterRap', 'jitterPpq5', 'shimmerLocal', 'shimmerLocalDB', 'shimmerApq3'].includes(key)) {
    return value.toFixed(2) + '%'
  }
  if (['hnr', 'meanDB', 'maxDB'].includes(key)) {
    return value.toFixed(1) + ' dB'
  }
  return value.toFixed(1)
}

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  loadRecords()
  loadAnalysis()
})
</script>

<template>
  <div class="analysis-page">
    <div class="page-header">
      <h2 class="page-title">嗓音分析</h2>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="record-card">
          <template #header>
            <div class="card-header">
              <span>已分析录音</span>
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
              :class="{ active: selectedRecordId === record.id }"
              @click="handleRecordSelect(record.id)"
            >
              <div class="record-name">{{ record.fileName }}</div>
              <div class="record-time">{{ formatDate(record.createdAt) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="16">
        <el-card shadow="never" v-if="currentAnalysis">
          <template #header>
            <div class="card-header">
              <span>分析结果详情</span>
            </div>
          </template>
          
          <div class="analysis-grid">
            <div 
              v-for="param in analysisParams" 
              :key="param.key"
              class="analysis-item"
            >
              <div class="analysis-label">{{ param.label }}</div>
              <div class="analysis-value">
                <span :class="{ abnormal: !isNormal(getValue(currentAnalysis, param.key), param.key) }">
                  {{ formatValue(getValue(currentAnalysis, param.key), param.key) }}
                </span>
                <el-icon v-if="isNormal(getValue(currentAnalysis, param.key), param.key)" class="normal-icon">
                  <Check />
                </el-icon>
                <el-icon v-else class="abnormal-icon">
                  <Warning />
                </el-icon>
              </div>
              <div class="analysis-desc">{{ param.desc }}</div>
            </div>
          </div>
          
          <el-divider />
          
          <div class="analysis-time">
            分析时间：{{ formatDate(currentAnalysis.createdAt) }}
          </div>
        </el-card>
        
        <el-card shadow="never" v-else>
          <el-empty description="请选择一条录音查看分析结果">
            <el-button type="primary" @click="loadAnalysis">刷新</el-button>
          </el-empty>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.analysis-page {
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
  font-weight: 600;
  font-size: 16px;
}

.record-card {
  max-height: 600px;
  overflow-y: auto;
}

.record-item {
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
  border-left: 3px solid #409EFF;
}

.record-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.analysis-item {
  padding: 12px;
  background-color: #F5F7FA;
  border-radius: 8px;
}

.analysis-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.analysis-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.analysis-value .abnormal {
  color: #F56C6C;
}

.normal-icon {
  color: #67C23A;
}

.abnormal-icon {
  color: #F56C6C;
}

.analysis-desc {
  font-size: 12px;
  color: #C0C4CC;
  margin-top: 4px;
}

.analysis-time {
  text-align: right;
  color: #909399;
  font-size: 14px;
}

@media (max-width: 768px) {
  .analysis-grid {
    grid-template-columns: 1fr;
  }
}
</style>
