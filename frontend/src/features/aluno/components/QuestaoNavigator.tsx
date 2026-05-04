import type { QuestaoAtividade, RespostasMap } from '../types'

interface Props {
  questoes: QuestaoAtividade[]
  indiceAtual: number
  respostas: RespostasMap
  onNavegar: (indice: number) => void
}

function estaRespondida(questaoId: number, respostas: RespostasMap): boolean {
  const r = respostas[questaoId]
  if (r === undefined || r === null) return false
  if (typeof r === 'string') return r.trim().length > 0
  if (typeof r === 'number') return true
  if (Array.isArray(r)) return r.length > 0
  return false
}

export function QuestaoNavigator({ questoes, indiceAtual, respostas, onNavegar }: Props) {
  return (
    <div className="flex flex-col gap-3">
      {/* Índice numérico */}
      <div className="flex flex-wrap gap-2">
        {questoes.map((q, i) => {
          const respondida = estaRespondida(q.id, respostas)
          const atual = i === indiceAtual
          return (
            <button
              key={q.id}
              type="button"
              onClick={() => onNavegar(i)}
              aria-label={`Questão ${q.numero}${respondida ? ' — respondida' : ''}`}
              className={[
                'flex h-8 w-8 items-center justify-center rounded-full text-xs font-semibold transition-colors',
                atual
                  ? 'bg-blue-600 text-white shadow-md'
                  : respondida
                    ? 'bg-green-100 text-green-700 hover:bg-green-200'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200',
              ].join(' ')}
            >
              {q.numero}
            </button>
          )
        })}
      </div>

      {/* Anterior / Próxima */}
      <div className="flex gap-3">
        <button
          type="button"
          onClick={() => onNavegar(indiceAtual - 1)}
          disabled={indiceAtual === 0}
          className="flex flex-1 items-center justify-center gap-2 rounded-xl border border-gray-300 bg-white py-2.5 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
          </svg>
          Anterior
        </button>
        <button
          type="button"
          onClick={() => onNavegar(indiceAtual + 1)}
          disabled={indiceAtual === questoes.length - 1}
          className="flex flex-1 items-center justify-center gap-2 rounded-xl border border-gray-300 bg-white py-2.5 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
        >
          Próxima
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </div>
    </div>
  )
}
