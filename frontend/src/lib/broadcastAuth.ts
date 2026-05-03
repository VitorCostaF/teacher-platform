const channel = new BroadcastChannel('auth')

export function broadcastLogout() {
  channel.postMessage({ type: 'LOGOUT' })
}

export function listenForLogout(callback: () => void): () => void {
  const handler = (e: MessageEvent) => {
    if (e.data?.type === 'LOGOUT') callback()
  }
  channel.addEventListener('message', handler)
  return () => channel.removeEventListener('message', handler)
}
