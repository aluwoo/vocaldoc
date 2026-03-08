"""Frontend tests for VocalDoc."""

import { describe, it, expect, vi, beforeEach } from 'vitest'

describe('API Module', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  describe('Auth API', () => {
    it('should have sendSms function', async () => {
      const { sendSms } = await import('../src/api/auth')
      expect(sendSms).toBeDefined()
      expect(typeof sendSms).toBe('function')
    })

    it('should have login function', async () => {
      const { login } = await import('../src/api/auth')
      expect(login).toBeDefined()
      expect(typeof login).toBe('function')
    })
  })

  describe('Request Utility', () => {
    it('should export request function', async () => {
      const request = await import('../src/utils/request')
      expect(request.default).toBeDefined()
    })
  })
})

describe('Store', () => {
  it('should have user store', async () => {
    const store = await import('../src/store')
    expect(store).toBeDefined()
  })
})

describe('Router', () => {
  it('should have router configured', async () => {
    const router = await import('../src/router')
    expect(router).toBeDefined()
  })
})
