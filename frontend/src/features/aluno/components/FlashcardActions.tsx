interface Props {
  isFlipped: boolean
  onResponder: (sabia: boolean) => void
  isLoading: boolean
}

export function FlashcardActions({ isFlipped, onResponder, isLoading }: Props) {
  if (!isFlipped) {
    return (
      <p className="text-center text-sm text-gray-400">
        Vire o card para avaliar
      </p>
    )
  }

  return (
    <div className="flex gap-3">
      <button
        type="button"
        onClick={() => onResponder(false)}
        disabled={isLoading}
        className="flex flex-1 items-center justify-center gap-2 rounded-xl border-2 border-red-200 bg-red-50 py-3 text-sm font-semibold text-red-700 transition-colors hover:bg-red-100 disabled:opacity-50"
      >
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
        Não sabia
      </button>

      <button
        type="button"
        onClick={() => onResponder(true)}
        disabled={isLoading}
        className="flex flex-1 items-center justify-center gap-2 rounded-xl border-2 border-green-200 bg-green-50 py-3 text-sm font-semibold text-green-700 transition-colors hover:bg-green-100 disabled:opacity-50"
      >
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
        </svg>
        Sabia
      </button>
    </div>
  )
}
