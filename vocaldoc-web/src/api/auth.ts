import request from '@/utils/request'
import type { User } from '@/types'

export interface LoginParams {
  phone: string
  code: string
}

export interface LoginResult {
  token: string
  user: User
}

export interface SendCodeParams {
  phone: string
}

export function sendCode(data: SendCodeParams) {
  return request({
    url: '/api/auth/sendCode',
    method: 'POST',
    data
  })
}

export function login(data: LoginParams) {
  return request<LoginResult>({
    url: '/api/auth/login',
    method: 'POST',
    data
  })
}
