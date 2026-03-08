<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getStatistics } from '@/api/admin'
import { getRecordList } from '@/api/voice'
import type { Statistics } from '@/types'
import { VideoCamera, User, Document, TrendCharts } from '@element-plus/icons-vue'

const stats = ref<Statistics>({
  teacherCount: 0,
  studentCount: 0,
  totalUsers: 0,
  totalRecords: 0,
  analyzedRecords: 0
})
const loading = ref(false)

const statCards = [
  { title: '教师数量', value: () => stats.value.teacherCount, icon: User, color: '#409EFF' },
  { title: '学生数量', value: () => stats.value.studentCount, icon: User, color: '#67C23A' },
  { title: '录音总数', value: () => stats.value.totalRecords || 0, icon: VideoCamera, color: '#E6A23C' },
  { title: '已分析', value: () => stats.value.analyzedRecords || 0, icon: TrendCharts, color: '#F56C6C' }
]

async function loadData() {
  loading.value = true
  try {
    const [statData, recordData] = await Promise.all([
      getStatistics().catch(() => ({ data: { teacherCount: 0, studentCount: 0, totalUsers: 0 } })),
      getRecordList({ current: 1, size: 1 }).catch(() => ({ data: { total: 0 } }))
    ])
    
    stats.value = {
      teacherCount: statData.data?.teacherCount || 0,
      studentCount: statData.data?.studentCount || 0,
      totalUsers: statData.data?.totalUsers || 0,
      totalRecords: recordData.data?.total || 0,
      analyzedRecords: 0
    }
  } catch (e) {
    console.error('Failed to load statistics:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="dashboard">
    <h2 class="page-title">数据概览</h2>
    
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6" v-for="card in statCards" :key="card.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: card.color }">
              <el-icon :size="24"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ card.value() }}</div>
              <div class="stat-label">{{ card.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>系统概览</span>
            </div>
          </template>
          <div class="overview-content">
            <p>欢迎使用声乐课堂嗓音档案管理系统</p>
            <p class="sub-text">请从左侧菜单选择功能</p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快速开始</span>
            </div>
          </template>
          <div class="quick-start">
            <el-steps direction="vertical" :space="60">
              <el-step title="上传录音" description="在嗓音档案页面点击上传按钮" />
              <el-step title="自动分析" description="系统自动进行嗓音分析" />
              <el-step title="查看结果" description="查看详细分析报告" />
              <el-step title="历史对比" description="对比不同时期的嗓音变化" />
            </el-steps>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 0;
}

.page-title {
  margin-bottom: 20px;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
}

.overview-content {
  color: #606266;
  line-height: 2;
}

.sub-text {
  color: #909399;
  font-size: 14px;
}

.quick-start {
  padding: 10px 0;
}
</style>
