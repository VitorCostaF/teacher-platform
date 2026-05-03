import type { InternalAxiosRequestConfig } from 'axios'
import { apiClient } from './api'
import { setAuth, clearAuth, getCurrentUser } from '@/store/authStore'
import { authService } from '@/features/auth/services/auth.service'
import { toastEmitter } from './toastEmitter'
import { authEmitter } from './authEmitter'
import { ApiError } from './errors'

type RetryConfig = InternalAxiosRequestConfig & { _retry?: boolean }

let isRefreshing = false
let failedQueue: Array<{ resolve: (token: string) => void; reject: (err: unknown) => void }> = []

function processQueue(error: unknown, token: string | null) {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error)
    else resolve(token!)
  })
  failedQueue = []
}

function handleSessionExpired() {
  clearAuth()
  sessionStorage.setItem('redirect_url', window.location.pathname + window.location.search)
  toastEmitter.emit('warning', 'Sua sessão expirou. Faça login para continuar.')
  authEmitter.emit('session-expired')
}

export function setupAuthInterceptor() {
  apiClient.interceptors.response.use(
    undefined,
    async (error) => {
      const originalRequest = error.config as RetryConfig

      // Não interceptar erros que não são 401
      if (error.response?.status !== 401) return Promise.reject(error)

      // Evitar loop: não tentar refresh se o refresh em si falhou
      if (originalRequest.url?.includes('/auth/refresh')) {
        handleSessionExpired()
        return Promise.reject(new ApiError(401, 'Sessão expirada'))
      }

      // Já tentou refresh para esta requisição
      if (originalRequest._retry) {
        handleSessionExpired()
        return Promise.reject(new ApiError(401, 'Sessão expirada'))
      }

      // Refresh já em andamento — enfileirar esta requisição
      if (isRefreshing) {
        return new Promise<string>((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then((token) => {
          originalRequest.headers = originalRequest.headers ?? {}
          originalRequest.headers.Authorization = `Bearer ${token}`
          return apiClient(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const data = await authService.refresh()
        const user = getCurrentUser()!
        setAuth(data.accessToken, user, data.expiresIn)
        processQueue(null, data.accessToken)
        originalRequest.headers = originalRequest.headers ?? {}
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`
        return apiClient(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        handleSessionExpired()
        return Promise.reject(new ApiError(401, 'Sessão expirada'))
      } finally {
        isRefreshing = false
      }
    },
  )
}
