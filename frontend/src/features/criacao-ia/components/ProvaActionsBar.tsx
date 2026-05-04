interface Props {
  totalQuestoes: number
  onPublicar: () => void
  onSalvarRascunho: () => void
  onDescartar: () => void
  isPublicando: boolean
}

export function ProvaActionsBar({
  totalQuestoes, onPublicar, onSalvarRascunho, onDescartar, isPublicando,
}: Props) {
  const podePublicar = totalQuestoes > 0 && !isPublicando

  return (
    <div className="flex items-center justify-between border-t border-gray-200 bg-white px-6 py-3">
      <button
        type="button"
        onClick={onDescartar}
        className="rounded-lg px-4 py-2 text-sm text-gray-500 hover:bg-gray-100 hover:text-gray-700"
      >
        Descartar
      </button>

      <div className="flex items-center gap-2">
        <span className="text-xs text-gray-400">
          {totalQuestoes} {totalQuestoes === 1 ? 'questão' : 'questões'}
        </span>
        <button
          type="button"
          onClick={onSalvarRascunho}
          disabled={totalQuestoes === 0}
          className="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-40"
        >
          Salvar rascunho
        </button>
        <button
          type="button"
          onClick={onPublicar}
          disabled={!podePublicar}
          className="flex items-center gap-2 rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isPublicando ? (
            <>
              <svg className="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
              </svg>
              Publicando...
            </>
          ) : 'Publicar prova'}
        </button>
      </div>
    </div>
  )
}
