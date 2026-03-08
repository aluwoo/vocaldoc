import request from '@/utils/request'
import type { Statistics } from '@/types'

export function getStatistics() {
  return request<Statistics>({
    url: '/api/v1/admin/statistics',
    method: 'GET'
  })
}

export function getTeacherList() {
  return request({
    url: '/api/v1/admin/teachers',
    method: 'GET'
  })
}

export function getStudentList(teacherId: number) {
  return request({
    url: `/api/v1/admin/students/${teacherId}`,
    method: 'GET'
  })
}
