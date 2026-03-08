// Type definitions
export interface User {
  id: number
  name: string
  phone: string
  role: string
}

export interface VoiceRecord {
  id: number
  userId: number
  recordType: string
  fileName: string
  fileUrl: string
  fileSize: number
  duration: number
  description: string
  status: number
  recordAt: string
  createdAt: string
}

export interface VoiceAnalysis {
  id: number
  recordId: number
  meanF0: number
  minF0: number
  maxF0: number
  f0Range: number
  f1Mean: number
  f2Mean: number
  f3Mean: number
  f4Mean: number
  meanDB: number
  maxDB: number
  jitterLocal: number
  jitterRap: number
  jitterPpq5: number
  shimmerLocal: number
  shimmerLocalDB: number
  shimmerApq3: number
  hnr: number
  createdAt: string
}

export interface Statistics {
  teacherCount: number
  studentCount: number
  totalUsers: number
  totalRecords?: number
  analyzedRecords?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}
