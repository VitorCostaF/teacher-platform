import { useEffect, useRef, useState } from 'react'
import { Button } from './Button'
import { Input } from './Input'

interface ConfirmationModalProps {
  isOpen: boolean
  title: string
  description: string
  nivel: 'medio' | 'alto' | 'critico'
  confirmationText?: string
  confirmLabel?: string
  isLoading?: boolean
  error?: string | null
  onConfirm: () => void | Promise<void>
  onCancel: () => void
}

const nivelBadge: Record<ConfirmationModalProps['nivel'], { label: string; className: string }> = {
  medio:   { label: 'Atenção',  className: 'bg-yellow-100 text-yellow-800' },
  alto:    { label: 'Cuidado',  className: 'bg-orange-100 text-orange-800' },
  critico: { label: 'Crítico',  className: 'bg-red-100 text-red-800' },
}

export function ConfirmationModal({
  isOpen,
  title,
  description,
  nivel,
  confirmationText,
  confirmLabel = 'Confirmar',
  isLoading = false,
  error,
  onConfirm,
  onCancel,
}: ConfirmationModalProps) {
  const dialogRef = useRef<HTMLDialogElement>(null)
  const [typedText, setTypedText] = useState('')

  useEffect(() => {
    const dialog = dialogRef.current
    if (!dialog) return
    if (isOpen) {
      if (!dialog.open) dialog.showModal()
    } else {
      if (dialog.open) dialog.close()
      setTypedText('')
    }
  }, [isOpen])

  useEffect(() => {
    const dialog = dialogRef.current
    if (!dialog) return

    const handleCancel = (e: Event) => {
      e.preventDefault()
      if (!isLoading) onCancel()
    }

    dialog.addEventListener('cancel', handleCancel)
    return () => dialog.removeEventListener('cancel', handleCancel)
  }, [isLoading, onCancel])

  const handleBackdropClick = (e: React.MouseEvent<HTMLDialogElement>) => {
    if (e.target === dialogRef.current && !isLoading) onCancel()
  }

  const isCritical = nivel === 'critico'
  const confirmDisabled = isLoading || (isCritical && typedText !== confirmationText)

  const badge = nivelBadge[nivel]

  return (
    <dialog
      ref={dialogRef}
      onClick={handleBackdropClick}
      aria-modal="true"
      role="alertdialog"
      aria-labelledby="modal-title"
      aria-describedby="modal-description"
      className="w-full max-w-md rounded-2xl border border-gray-200 bg-white p-0 shadow-xl backdrop:bg-black/40 open:flex open:flex-col"
    >
      <div className="flex flex-col gap-4 p-6">
        <div className="flex items-start gap-3">
          <span className={`mt-0.5 rounded-full px-2.5 py-0.5 text-xs font-semibold ${badge.className}`}>
            {badge.label}
          </span>
          <h2 id="modal-title" className="text-base font-semibold text-gray-900">
            {title}
          </h2>
        </div>

        <p id="modal-description" className="text-sm text-gray-600">
          {description}
        </p>

        {isCritical && confirmationText && (
          <div className="flex flex-col gap-1.5">
            <label className="text-sm text-gray-700">
              Digite <span className="font-mono font-semibold text-gray-900">{confirmationText}</span> para confirmar:
            </label>
            <Input
              value={typedText}
              onChange={(e) => setTypedText(e.target.value)}
              autoComplete="off"
              disabled={isLoading}
            />
          </div>
        )}

        {error && (
          <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}
          </p>
        )}
      </div>

      <div className="flex justify-end gap-3 border-t border-gray-100 px-6 py-4">
        <Button
          variant="secondary"
          onClick={onCancel}
          disabled={isLoading}
          autoFocus
        >
          Cancelar
        </Button>
        <Button
          variant="destructive"
          onClick={onConfirm}
          loading={isLoading}
          disabled={confirmDisabled}
        >
          {confirmLabel}
        </Button>
      </div>
    </dialog>
  )
}
