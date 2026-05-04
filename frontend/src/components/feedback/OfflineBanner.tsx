import { useState, useEffect } from 'react'

export function OfflineBanner() {
  const [status, setStatus] = useState<'offline' | 'reconnected' | null>(
    navigator.onLine ? null : 'offline'
  )

  useEffect(() => {
    let reconnectTimer: ReturnType<typeof setTimeout>

    function handleOffline() {
      clearTimeout(reconnectTimer)
      setStatus('offline')
    }

    function handleOnline() {
      setStatus('reconnected')
      reconnectTimer = setTimeout(() => setStatus(null), 3000)
    }

    window.addEventListener('offline', handleOffline)
    window.addEventListener('online', handleOnline)

    return () => {
      window.removeEventListener('offline', handleOffline)
      window.removeEventListener('online', handleOnline)
      clearTimeout(reconnectTimer)
    }
  }, [])

  if (status === null) return null

  if (status === 'offline') {
    return (
      <div
        role="status"
        aria-live="assertive"
        className="fixed inset-x-0 top-0 z-50 bg-orange-500 px-4 py-2 text-center text-sm font-medium text-white shadow-md"
      >
        Você está sem conexão. Algumas funções podem não funcionar.
      </div>
    )
  }

  return (
    <div
      role="status"
      aria-live="polite"
      className="fixed inset-x-0 top-0 z-50 bg-green-500 px-4 py-2 text-center text-sm font-medium text-white shadow-md"
    >
      Conexão restaurada!
    </div>
  )
}
