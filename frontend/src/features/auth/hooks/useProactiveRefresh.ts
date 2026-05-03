import { authService } from '../services/auth.service'
import { setAuth, clearAuth, getCurrentUser } from '@/store/authStore'
import { toastEmitter } from '@/lib/toastEmitter'
import { authEmitter } from '@/lib/authEmitter'

const REFRESH_MARGIN_MS = 5 * 60 * 1000

let timer: ReturnType<typeof setTimeout> | null = null

function scheduleProactiveRefresh(expiresIn: number) {
  if (timer) clearTimeout(timer)

  const msUntilRefresh = Math.max(0, expiresIn * 1000 - REFRESH_MARGIN_MS)

  timer = setTimeout(async () => {
    try {
      const data = await authService.refresh()
      const user = getCurrentUser()!
      setAuth(data.accessToken, user, data.expiresIn)
      scheduleProactiveRefresh(data.expiresIn)
    } catch {
      clearAuth()
      toastEmitter.emit('warning', 'Sua sessão expirou. Faça login para continuar.')
      authEmitter.emit('session-expired')
    }
  }, msUntilRefresh)
}

function cancelProactiveRefresh() {
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
}

export function useProactiveRefresh() {
  return { scheduleRefresh: scheduleProactiveRefresh, cancelRefresh: cancelProactiveRefresh }
}
