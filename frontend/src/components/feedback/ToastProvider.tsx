import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react'
import { Toast, ToastItem, ToastVariant } from './Toast'
import { toastEmitter } from '@/lib/toastEmitter'

interface ToastContextValue {
  addToast: (message: string, variant?: ToastVariant) => void
}

const ToastContext = createContext<ToastContextValue | null>(null)

export function useToast() {
  const ctx = useContext(ToastContext)
  if (!ctx) throw new Error('useToast must be used within ToastProvider')
  return ctx
}

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([])
  const counterRef = useRef(0)

  const addToast = useCallback((message: string, variant: ToastVariant = 'success') => {
    const id = String(++counterRef.current)
    setToasts((prev) => [...prev, { id, message, variant }])
  }, [])

  const dismiss = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }, [])

  useEffect(() => {
    const onError = (msg: string) => addToast(msg, 'error')
    const onSuccess = (msg: string) => addToast(msg, 'success')
    const onWarning = (msg: string) => addToast(msg, 'warning')

    toastEmitter.on('error', onError)
    toastEmitter.on('success', onSuccess)
    toastEmitter.on('warning', onWarning)

    return () => {
      toastEmitter.off('error', onError)
      toastEmitter.off('success', onSuccess)
      toastEmitter.off('warning', onWarning)
    }
  }, [addToast])

  return (
    <ToastContext.Provider value={{ addToast }}>
      {children}
      <div className="fixed top-4 right-4 z-50 flex w-80 flex-col gap-2">
        {toasts.map((toast) => (
          <Toast key={toast.id} {...toast} onDismiss={dismiss} />
        ))}
      </div>
    </ToastContext.Provider>
  )
}
