import request from '@/utils/request'
import type { User } from '@/types'

export interface LoginParams {
  phone: string
  code: string
}

export interface TokenResult {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export interface SendCodeParams {
  phone: string
}

export function sendCode(data: SendCodeParams) {
  return request({
    url: '/auth/sendCode',
    method: 'POST',
    data
  })
}

export function login(data: LoginParams) {
  return request<TokenResult>({
    url: '/auth/login',
    method: 'POST',
    data
  })
}

export function getUserInfo() {
  return request<User>({
    url: '/users/me',
    method: 'GET'
  })
}

export function register(data: { phone: string; code: string; name: string }) {
  return request({
    url: '/auth/register',
    method: 'POST',
    data
  })
}
