import request from '@/utils/request'
import type { VoiceAnalysis, PageResult } from '@/types'

export interface AnalysisListParams {
  current?: number
  size?: number
  recordId?: number
}

export function analyzeVoice(recordId: number) {
  return request<VoiceAnalysis>({
    url: `/api/v1/analysis/save/${recordId}`,
    method: 'POST'
  })
}

export function getAnalysisByRecordId(recordId: number) {
  return request<VoiceAnalysis>({
    url: `/api/v1/analysis/get/${recordId}`,
    method: 'GET'
  })
}

export function getAnalysisList(params: AnalysisListParams) {
  return request<PageResult<VoiceAnalysis>>({
    url: '/api/v1/analysis/list',
    method: 'GET',
    params
  })
}

export function getComparison(recordIds: number[]) {
  return request({
    url: '/api/v1/analysis/compare',
    method: 'GET',
    params: {
      recordIds: recordIds.join(',')
    }
  })
}

export function getTrend(userId: number, days: number = 30) {
  return request({
    url: '/api/v1/analysis/trend',
    method: 'GET',
    params: { userId, days }
  })
}

export function getProgress(userId: number) {
  return request({
    url: '/api/v1/analysis/progress',
    method: 'GET',
    params: { userId }
  })
}
