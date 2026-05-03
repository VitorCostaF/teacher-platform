import axios from 'axios'
import { getAccessToken } from '@/store/authStore'
import { ApiError, HTTP_ERROR_MESSAGES, TIMEOUT_MESSAGE } from './errors'
import { toastEmitter } from './toastEmitter'
import { loadingEmitter } from './loadingEmitter'

export const apiClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
  timeout: 15000,
})

let loadingCount = 0

apiClient.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  if (++loadingCount === 1) {
    loadingEmitter.emit('start')
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => {
    if (--loadingCount <= 0) {
      loadingCount = 0
      loadingEmitter.emit('done')
    }
    return response
  },
  (error) => {
    if (--loadingCount <= 0) {
      loadingCount = 0
      loadingEmitter.emit('done')
    }

    if (error.code === 'ECONNABORTED' || error.code === 'ERR_NETWORK') {
      toastEmitter.emit('error', TIMEOUT_MESSAGE)
      return Promise.reject(new ApiError(0, TIMEOUT_MESSAGE))
    }

    const status: number = error.response?.status

    if (status === 401) {
      return Promise.reject(error)
    }

    if (status === 400 || status === 422) {
      return Promise.reject(
        new ApiError(
          status,
          error.response.data?.message ?? HTTP_ERROR_MESSAGES[status],
          error.response.data?.errors,
        ),
      )
    }

    const message = HTTP_ERROR_MESSAGES[status] ?? HTTP_ERROR_MESSAGES[500]
    toastEmitter.emit('error', message)
    return Promise.reject(new ApiError(status, message))
  },
)
