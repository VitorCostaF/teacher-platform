import { useState, useMemo } from 'react'
import type { MapaCalorItem } from '../types'

function erroParaCor(pct: number): string {
  if (pct < 30) return '#bbf7d0'
  if (pct < 60) return '#fde68a'
  return '#fca5a5'
}

function erroParaTextoCor(pct: number): string {
  if (pct < 30) return '#166534'
  if (pct < 60) return '#92400e'
  return '#991b1b'
}

interface TooltipState {
  x: number
  y: number
  item: MapaCalorItem
}

interface Props {
  dados: MapaCalorItem[]
}

export function MapaCalorChart({ dados }: Props) {
  const [tooltip, setTooltip] = useState<TooltipState | null>(null)

  const turmas = useMemo(() => [...new Set(dados.map(d => d.turma))], [dados])
  const topicos = useMemo(() => [...new Set(dados.map(d => d.topico))], [dados])

  const celulas = useMemo(() => {
    const mapa = new Map<string, number>()
    dados.forEach(d => mapa.set(`${d.turma}|${d.topico}`, d.percentualErros))
    return mapa
  }, [dados])

  const MAX_CELULAS = 100
  const topicosVisiveis = topicos.length * turmas.length > MAX_CELULAS
    ? topicos.slice(0, Math.floor(MAX_CELULAS / turmas.length))
    : topicos

  if (dados.length === 0) {
    return (
      <div className="rounded-xl border border-gray-200 bg-gray-50 px-4 py-8 text-center text-sm text-gray-500">
        Sem dados de erros por tópico disponíveis.
      </div>
    )
  }

  return (
    <div className="relative overflow-x-auto rounded-xl border border-gray-200 bg-white p-4">
      {/* Legenda */}
      <div className="mb-3 flex items-center gap-4 text-xs text-gray-500">
        <div className="flex items-center gap-1.5">
          <div className="h-3 w-5 rounded" style={{ backgroundColor: '#bbf7d0' }} />
          <span>{'< 30%'}</span>
        </div>
        <div className="flex items-center gap-1.5">
          <div className="h-3 w-5 rounded" style={{ backgroundColor: '#fde68a' }} />
          <span>30–60%</span>
        </div>
        <div className="flex items-center gap-1.5">
          <div className="h-3 w-5 rounded" style={{ backgroundColor: '#fca5a5' }} />
          <span>{'> 60%'}</span>
        </div>
      </div>

      <div
        className="grid gap-1"
        style={{
          gridTemplateColumns: `minmax(120px,auto) repeat(${turmas.length}, minmax(64px,1fr))`,
        }}
      >
        {/* Cabeçalho turmas */}
        <div />
        {turmas.map(t => (
          <div key={t} className="px-1 pb-1 text-center text-xs font-semibold text-gray-600 truncate">
            {t}
          </div>
        ))}

        {/* Linhas de tópicos */}
        {topicosVisiveis.map(topico => (
          <>
            <div key={`label-${topico}`} className="flex items-center pr-2 text-xs text-gray-700 truncate">
              {topico}
            </div>
            {turmas.map(turma => {
              const pct = celulas.get(`${turma}|${topico}`)
              const bg = pct !== undefined ? erroParaCor(pct) : '#f9fafb'
              const fg = pct !== undefined ? erroParaTextoCor(pct) : '#9ca3af'

              return (
                <div
                  key={`${turma}-${topico}`}
                  className="flex cursor-default items-center justify-center rounded-md py-2 text-xs font-medium transition-opacity hover:opacity-80"
                  style={{ backgroundColor: bg, color: fg }}
                  onMouseEnter={e => {
                    if (pct !== undefined) {
                      const rect = (e.target as HTMLElement).getBoundingClientRect()
                      setTooltip({ x: rect.left + rect.width / 2, y: rect.top, item: { turma, topico, percentualErros: pct } })
                    }
                  }}
                  onMouseLeave={() => setTooltip(null)}
                >
                  {pct !== undefined ? `${pct.toFixed(0)}%` : '—'}
                </div>
              )
            })}
          </>
        ))}
      </div>

      {topicosVisiveis.length < topicos.length && (
        <p className="mt-2 text-center text-xs text-gray-400">
          Mostrando {topicosVisiveis.length} de {topicos.length} tópicos
        </p>
      )}

      {/* Tooltip */}
      {tooltip && (
        <div
          className="pointer-events-none fixed z-50 -translate-x-1/2 -translate-y-full rounded-lg border border-gray-200 bg-white px-3 py-2 shadow-lg text-xs"
          style={{ left: tooltip.x, top: tooltip.y - 8 }}
        >
          <p className="font-semibold text-gray-900">{tooltip.item.topico}</p>
          <p className="text-gray-500">{tooltip.item.turma}</p>
          <p className="mt-0.5 font-bold" style={{ color: erroParaTextoCor(tooltip.item.percentualErros) }}>
            {tooltip.item.percentualErros.toFixed(0)}% de erros
          </p>
        </div>
      )}
    </div>
  )
}
