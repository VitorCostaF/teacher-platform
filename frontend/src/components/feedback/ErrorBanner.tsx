interface ErrorBannerProps {
  message: string
  variant?: 'error' | 'warning'
  onDismiss?: () => void
}

const variantClasses = {
  error: 'bg-red-50 border-red-400 text-red-800',
  warning: 'bg-yellow-50 border-yellow-400 text-yellow-800',
}

export function ErrorBanner({ message, variant = 'error', onDismiss }: ErrorBannerProps) {
  return (
    <div
      role="alert"
      className={[
        'flex items-start gap-3 rounded-lg border px-4 py-3 text-sm',
        variantClasses[variant],
      ].join(' ')}
    >
      <p className="flex-1">{message}</p>
      {onDismiss && (
        <button
          type="button"
          aria-label="Fechar"
          onClick={onDismiss}
          className="text-current opacity-50 hover:opacity-100 transition-opacity"
        >
          ✕
        </button>
      )}
    </div>
  )
}
