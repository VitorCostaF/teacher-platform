type Listener = (message: string) => void

const listeners: Record<string, Listener[]> = {}

export const toastEmitter = {
  on(event: string, fn: Listener) {
    if (!listeners[event]) listeners[event] = []
    listeners[event].push(fn)
  },
  off(event: string, fn: Listener) {
    listeners[event] = (listeners[event] ?? []).filter((l) => l !== fn)
  },
  emit(event: string, message: string) {
    ;(listeners[event] ?? []).forEach((l) => l(message))
  },
}
