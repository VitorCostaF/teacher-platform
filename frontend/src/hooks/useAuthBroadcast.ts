import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { clearAuth } from '@/store/authStore'
import { authEmitter } from '@/lib/authEmitter'
import { listenForLogout } from '@/lib/broadcastAuth'

export function useAuthBroadcast() {
  const navigate = useNavigate()

  useEffect(() => {
    const onSessionExpired = () => {
      navigate('/login', { replace: true })
    }

    const onBroadcastLogout = () => {
      clearAuth()
      navigate('/login', { replace: true })
    }

    authEmitter.on('session-expired', onSessionExpired)
    const unsubscribeBroadcast = listenForLogout(onBroadcastLogout)

    return () => {
      authEmitter.off('session-expired', onSessionExpired)
      unsubscribeBroadcast()
    }
  }, [navigate])
}
