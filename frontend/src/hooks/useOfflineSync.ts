import { useEffect } from 'react'
import { apiClient } from '@/lib/api'
import { toastEmitter } from '@/lib/toastEmitter'

async function syncBackups() {
  const keys: string[] = []
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key && (key.startsWith('prova_backup_') || key.startsWith('atividade_rascunho_'))) {
      keys.push(key)
    }
  }

  if (keys.length === 0) return

  let synced = 0
  for (const key of keys) {
    const raw = localStorage.getItem(key)
    if (!raw) continue
    try {
      const payload = JSON.parse(raw)
      const endpoint = key.startsWith('prova_backup_')
        ? `/provas/sessoes/${payload.sessaoId}/autosave`
        : `/atividades/entregas/${payload.entregaId}/autosave`

      await apiClient.post(endpoint, payload)
      localStorage.removeItem(key)
      synced++
    } catch {
      // mantém no localStorage para próxima tentativa
    }
  }

  if (synced > 0) {
    toastEmitter.emit('success', 'Suas respostas foram sincronizadas.')
  }

  if (synced < keys.length) {
    toastEmitter.emit('warning', 'Alguns rascunhos não puderam ser sincronizados. Tentaremos novamente.')
  }
}

export function useOfflineSync() {
  useEffect(() => {
    function handleOnline() {
      syncBackups()
    }

    window.addEventListener('online', handleOnline)
    return () => window.removeEventListener('online', handleOnline)
  }, [])
}
