import { Workbox } from 'workbox-window'
import { toastEmitter } from './toastEmitter'

export function registerPWA() {
  if (!('serviceWorker' in navigator)) return

  const wb = new Workbox('/sw.js')

  wb.addEventListener('waiting', () => {
    toastEmitter.emit(
      'warning',
      'Nova versão disponível — recarregue a página para atualizar.'
    )

    // Ativa automaticamente após 10s se o usuário não recarregar
    setTimeout(() => {
      wb.messageSkipWaiting()
    }, 10_000)
  })

  wb.register().catch(err => {
    console.error('[PWA] Falha ao registrar service worker:', err)
  })
}
