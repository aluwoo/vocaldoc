import request from '@/utils/request'
import type { VoiceRecord, PageResult } from '@/types'

export interface RecordListParams {
  current?: number
  size?: number
  recordType?: string
  status?: number
}

export function getRecordList(params: RecordListParams) {
  return request<PageResult<VoiceRecord>>({
    url: '/api/v1/records',
    method: 'GET',
    params
  })
}

export function getRecordById(id: number) {
  return request<VoiceRecord>({
    url: `/api/v1/records/${id}`,
    method: 'GET'
  })
}

export function uploadRecord(data: FormData) {
  return request<VoiceRecord>({
    url: '/api/v1/records/upload',
    method: 'POST',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

export function deleteRecord(id: number) {
  return request({
    url: `/api/v1/records/${id}`,
    method: 'DELETE'
  })
}
