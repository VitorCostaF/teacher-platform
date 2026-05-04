/// <reference lib="webworker" />
import { cleanupOutdatedCaches, precacheAndRoute } from 'workbox-precaching'
import { registerRoute } from 'workbox-routing'
import { NetworkFirst, NetworkOnly } from 'workbox-strategies'
import { ExpirationPlugin } from 'workbox-expiration'

declare const self: ServiceWorkerGlobalScope

cleanupOutdatedCaches()
precacheAndRoute(self.__WB_MANIFEST)

// Cache de API com NetworkFirst (exceto auth e sessões de prova)
registerRoute(
  ({ url }) =>
    url.pathname.startsWith('/api/') &&
    !url.pathname.startsWith('/api/auth/') &&
    !/^\/api\/provas\/\d+\/sessoes/.test(url.pathname),
  new NetworkFirst({
    cacheName: 'api-cache',
    plugins: [new ExpirationPlugin({ maxEntries: 50, maxAgeSeconds: 300 })],
  })
)

// Tokens nunca cacheados
registerRoute(
  ({ url }) => url.pathname.startsWith('/api/auth/'),
  new NetworkOnly()
)

// Push notification handler
self.addEventListener('push', (event) => {
  const data = event.data?.json() ?? {}
  event.waitUntil(
    self.registration.showNotification(data.titulo ?? 'Teacher Platform', {
      body: data.corpo ?? '',
      data: { url: data.url ?? '/' },
      icon: '/icons/icon-192.svg',
    })
  )
})

self.addEventListener('notificationclick', (event) => {
  event.notification.close()
  event.waitUntil(
    clients.openWindow(event.notification.data?.url ?? '/')
  )
})
