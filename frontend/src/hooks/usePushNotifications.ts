import { apiClient } from '@/lib/api'

const PUSH_REGISTERED_KEY = 'push_device_registered'

async function getVapidKey(): Promise<string> {
  const res = await apiClient.get<{ publicKey: string }>('/notificacoes/vapid-public-key')
  return res.data.publicKey
}

function urlBase64ToUint8Array(base64String: string): Uint8Array {
  const padding = '='.repeat((4 - (base64String.length % 4)) % 4)
  const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/')
  const rawData = window.atob(base64)
  return Uint8Array.from([...rawData].map(c => c.charCodeAt(0)))
}

async function subscribeToPush(registration: ServiceWorkerRegistration): Promise<void> {
  const vapidKey = await getVapidKey()

  const subscription = await registration.pushManager.subscribe({
    userVisibleOnly: true,
    applicationServerKey: urlBase64ToUint8Array(vapidKey),
  })

  const json = subscription.toJSON()
  const keys = json.keys as { p256dh: string; auth: string }

  await apiClient.post('/notificacoes/registrar-device', {
    endpoint: json.endpoint,
    p256dh: keys.p256dh,
    auth: keys.auth,
  })

  localStorage.setItem(PUSH_REGISTERED_KEY, 'true')
}

export async function requestPushPermission(): Promise<void> {
  if (!('Notification' in window) || !('serviceWorker' in navigator)) return
  if (localStorage.getItem(PUSH_REGISTERED_KEY) === 'true') return

  const permission = await Notification.requestPermission()
  if (permission !== 'granted') return

  const registration = await navigator.serviceWorker.ready
  await subscribeToPush(registration)
}
