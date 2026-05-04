import type { RecomendacaoItem } from '../types'

interface Props {
  item: RecomendacaoItem
  onClick?: () => void
}

export function RecomendacaoCard({ item, onClick }: Props) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="flex w-full flex-col gap-2 rounded-xl border border-blue-100 bg-blue-50 p-4 text-left transition-shadow hover:shadow-md"
    >
      <span className="inline-block self-start rounded-full bg-blue-600 px-2.5 py-0.5 text-xs font-semibold text-white">
        Sugerido para você
      </span>
      <p className="text-sm font-semibold text-gray-900 leading-snug">{item.titulo}</p>
      <p className="text-xs text-gray-600 leading-relaxed">{item.explicacao}</p>
    </button>
  )
}
