import { Button } from '@/components/ui/Button'

interface FrequenciaStickyFooterProps {
  totalPreenchidos: number
  totalAlunos: number
  isSaving: boolean
  onSalvar: () => void
  onMarcarTodosPresentes: () => void
}

export function FrequenciaStickyFooter({
  totalPreenchidos,
  totalAlunos,
  isSaving,
  onSalvar,
  onMarcarTodosPresentes,
}: FrequenciaStickyFooterProps) {
  const todoPreenchido = totalPreenchidos === totalAlunos && totalAlunos > 0

  return (
    <div className="fixed bottom-0 left-0 right-0 z-30 border-t border-gray-200 bg-white shadow-lg">
      <div className="mx-auto flex max-w-3xl items-center justify-between gap-4 px-4 py-3 sm:px-6">
        <div className="flex items-center gap-3">
          <span className="text-sm text-gray-600">
            <span className={['font-semibold', todoPreenchido ? 'text-green-600' : 'text-gray-900'].join(' ')}>
              {totalPreenchidos}
            </span>{' '}
            de {totalAlunos} registrado{totalAlunos !== 1 ? 's' : ''}
          </span>
          {!todoPreenchido && totalAlunos > 0 && (
            <span className="hidden text-xs text-amber-600 sm:inline">
              {totalAlunos - totalPreenchidos} pendente{totalAlunos - totalPreenchidos !== 1 ? 's' : ''}
            </span>
          )}
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="secondary"
            onClick={onMarcarTodosPresentes}
            disabled={isSaving}
            className="hidden text-xs sm:inline-flex"
          >
            Marcar todos presentes
          </Button>
          <Button
            onClick={onSalvar}
            loading={isSaving}
            disabled={!todoPreenchido || isSaving}
          >
            Salvar frequência
          </Button>
        </div>
      </div>
    </div>
  )
}
