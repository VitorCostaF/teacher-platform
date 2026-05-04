interface Props {
  hora: string | null
  onFechar: () => void
}

export function AbaInativaModal({ hora, onFechar }: Props) {
  if (!hora) return null

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center px-4 pb-6 sm:items-center">
      {/* Backdrop leve — não bloqueia a prova */}
      <div className="absolute inset-0 bg-black/20" onClick={onFechar} />

      <div className="relative w-full max-w-sm rounded-2xl border border-orange-200 bg-white p-5 shadow-xl">
        <div className="flex items-start gap-3">
          <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-orange-100 text-base">
            ⚠️
          </span>
          <div className="flex flex-col gap-1">
            <p className="text-sm font-semibold text-gray-900">Saída de janela detectada</p>
            <p className="text-sm text-gray-600">
              Você saiu desta janela às <strong>{hora}</strong>. Isso foi registrado.
            </p>
          </div>
        </div>

        <button
          type="button"
          onClick={onFechar}
          autoFocus
          className="mt-4 w-full rounded-xl bg-orange-500 py-2.5 text-sm font-semibold text-white hover:bg-orange-600"
        >
          Entendi
        </button>
      </div>
    </div>
  )
}
