import type { DisciplinaDesempenho } from '../types'

interface Props {
  disciplina: DisciplinaDesempenho
}

const TENDENCIA = {
  UP: { icone: '↑', cor: 'text-green-600', bg: 'bg-green-50' },
  DOWN: { icone: '↓', cor: 'text-red-600', bg: 'bg-red-50' },
  STABLE: { icone: '→', cor: 'text-gray-500', bg: 'bg-gray-50' },
}

function barColor(media: number): string {
  if (media >= 7) return 'bg-green-500'
  if (media >= 5) return 'bg-yellow-500'
  return 'bg-red-500'
}

export function DisciplinaCard({ disciplina }: Props) {
  const t = TENDENCIA[disciplina.tendencia]
  const pct = Math.min(100, Math.round((disciplina.media / 10) * 100))

  return (
    <div className="flex flex-col gap-3 rounded-xl border border-gray-200 bg-white p-4">
      <div className="flex items-start justify-between gap-2">
        <p className="text-sm font-semibold text-gray-900">{disciplina.nome}</p>
        <span className={`flex items-center gap-0.5 rounded-full px-2 py-0.5 text-xs font-bold ${t.bg} ${t.cor}`}>
          {t.icone}
        </span>
      </div>

      {/* Barra de média */}
      <div className="flex flex-col gap-1">
        <div className="flex items-center justify-between text-xs">
          <span className="text-gray-500">Média</span>
          <span className="font-semibold text-gray-900">{disciplina.media.toFixed(1)}</span>
        </div>
        <div className="h-1.5 w-full overflow-hidden rounded-full bg-gray-100">
          <div
            className={`h-full rounded-full transition-all duration-500 ${barColor(disciplina.media)}`}
            style={{ width: `${pct}%` }}
          />
        </div>
      </div>

      {disciplina.proximaAvaliacao && (
        <p className="text-xs text-gray-400">
          Próxima avaliação:{' '}
          <span className="font-medium text-gray-600">
            {new Date(disciplina.proximaAvaliacao).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' })}
          </span>
        </p>
      )}
    </div>
  )
}
