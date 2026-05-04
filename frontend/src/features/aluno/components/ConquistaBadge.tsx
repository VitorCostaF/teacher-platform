import { useState } from 'react'
import type { Conquista } from '../types'

interface Props {
  conquista: Conquista
}

export function ConquistaBadge({ conquista }: Props) {
  const [mostrandoTooltip, setMostrandoTooltip] = useState(false)

  const dataFormatada = new Date(conquista.obtidaEm).toLocaleDateString('pt-BR', {
    day: '2-digit', month: 'short', year: 'numeric',
  })

  return (
    <div className="relative flex flex-col items-center gap-1.5">
      <button
        type="button"
        aria-label={`Conquista: ${conquista.descricao}`}
        onClick={() => setMostrandoTooltip(v => !v)}
        onMouseEnter={() => setMostrandoTooltip(true)}
        onMouseLeave={() => setMostrandoTooltip(false)}
        className="flex h-14 w-14 items-center justify-center rounded-full border-2 border-yellow-200 bg-yellow-50 text-2xl shadow-sm transition-transform hover:scale-110"
      >
        {conquista.icone}
      </button>

      <span className="text-center text-xs font-medium text-gray-700 leading-tight max-w-[64px]">
        {conquista.tipo}
      </span>

      {/* Tooltip */}
      {mostrandoTooltip && (
        <div className="absolute bottom-full left-1/2 z-10 mb-2 w-44 -translate-x-1/2 rounded-xl border border-gray-200 bg-white p-3 shadow-lg">
          <p className="text-xs font-semibold text-gray-900">{conquista.descricao}</p>
          <p className="mt-1 text-xs text-gray-500">Obtida em {dataFormatada}</p>
          {/* seta */}
          <div className="absolute left-1/2 top-full h-2 w-2 -translate-x-1/2 -translate-y-1/2 rotate-45 border-b border-r border-gray-200 bg-white" />
        </div>
      )}
    </div>
  )
}
