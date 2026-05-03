import { useState } from 'react'
import type { ComponentProps } from 'react'
import type { ConfirmationModal } from '@/components/ui/ConfirmationModal'

type ModalConfig = Omit<
  ComponentProps<typeof ConfirmationModal>,
  'isOpen' | 'isLoading' | 'error' | 'onCancel'
>

export function useConfirmationModal() {
  const [isOpen, setIsOpen] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [config, setConfig] = useState<ModalConfig | null>(null)

  const open = (cfg: ModalConfig) => {
    setConfig(cfg)
    setError(null)
    setIsLoading(false)
    setIsOpen(true)
  }

  const close = () => {
    if (isLoading) return
    setIsOpen(false)
    setError(null)
  }

  const handleConfirm = async () => {
    if (!config) return
    setIsLoading(true)
    setError(null)
    try {
      await config.onConfirm()
      setIsOpen(false)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Ocorreu um erro. Tente novamente.')
    } finally {
      setIsLoading(false)
    }
  }

  const modalProps: ComponentProps<typeof ConfirmationModal> = {
    ...(config ?? {
      title: '',
      description: '',
      nivel: 'medio',
      onConfirm: handleConfirm,
    }),
    isOpen,
    isLoading,
    error,
    onConfirm: handleConfirm,
    onCancel: close,
  }

  return { open, close, modalProps }
}
