import { useEffect } from 'react'

export type ToastVariant = 'success' | 'error' | 'warning'

export interface ToastItem {
  id: string
  message: string
  variant: ToastVariant
}

interface ToastProps extends ToastItem {
  onDismiss: (id: string) => void
}

const variantClasses: Record<ToastVariant, string> = {
  success: 'bg-green-50 border-green-400 text-green-800',
  error: 'bg-red-50 border-red-400 text-red-800',
  warning: 'bg-yellow-50 border-yellow-400 text-yellow-800',
}

export function Toast({ id, message, variant, onDismiss }: ToastProps) {
  useEffect(() => {
    const timer = setTimeout(() => onDismiss(id), 5000)
    return () => clearTimeout(timer)
  }, [id, onDismiss])

  return (
    <div
      role="alert"
      className={[
        'flex items-start gap-3 rounded-lg border px-4 py-3 shadow-md',
        variantClasses[variant],
      ].join(' ')}
    >
      <p className="flex-1 text-sm">{message}</p>
      <button
        type="button"
        aria-label="Fechar notificação"
        onClick={() => onDismiss(id)}
        className="text-current opacity-50 hover:opacity-100 transition-opacity"
      >
        ✕
      </button>
    </div>
  )
}
