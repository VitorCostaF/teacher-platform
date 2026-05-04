import type { ConteudoItem } from '../types'

interface Props {
  item: ConteudoItem
  onClick?: () => void
}

const ICONE: Record<ConteudoItem['tipo'], string> = {
  texto: '📄',
  video: '🎥',
  pdf: '📋',
}

const LABEL: Record<ConteudoItem['tipo'], string> = {
  texto: 'Texto',
  video: 'Vídeo',
  pdf: 'PDF',
}

export function ConteudoCard({ item, onClick }: Props) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="flex w-full items-center gap-3 rounded-xl border border-gray-200 bg-white p-3.5 text-left transition-shadow hover:shadow-md"
    >
      <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-gray-50 text-xl">
        {ICONE[item.tipo]}
      </span>
      <div className="flex min-w-0 flex-col gap-0.5">
        <p className="truncate text-sm font-medium text-gray-900">{item.titulo}</p>
        <div className="flex items-center gap-2 text-xs text-gray-500">
          <span>{LABEL[item.tipo]}</span>
          <span>·</span>
          <span>{item.tempoLeituraMinutos} min</span>
        </div>
      </div>
      <svg className="ml-auto h-4 w-4 shrink-0 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
      </svg>
    </button>
  )
}
