import { useNavigate } from 'react-router-dom'

interface Props {
  topicos: Array<{ topico: string; percentual: number }>
}

function barCor(pct: number): string {
  if (pct < 40) return 'bg-red-500'
  if (pct < 70) return 'bg-yellow-500'
  return 'bg-green-500'
}

function textoCor(pct: number): string {
  if (pct < 40) return 'text-red-600'
  if (pct < 70) return 'text-yellow-600'
  return 'text-green-600'
}

export function TopicosAcertoList({ topicos }: Props) {
  const navigate = useNavigate()
  const sorted = [...topicos].sort((a, b) => a.percentual - b.percentual)

  return (
    <div className="flex flex-col gap-2">
      {sorted.map(({ topico, percentual }) => (
        <button
          key={topico}
          type="button"
          onClick={() => navigate('/aluno/flashcards')}
          className="flex flex-col gap-1.5 rounded-xl border border-gray-200 bg-white p-3 text-left hover:bg-gray-50"
        >
          <div className="flex items-center justify-between gap-2">
            <span className="text-sm text-gray-800">{topico}</span>
            <span className={`text-sm font-semibold ${textoCor(percentual)}`}>
              {percentual.toFixed(0)}%
            </span>
          </div>
          <div className="h-1.5 w-full overflow-hidden rounded-full bg-gray-100">
            <div
              className={`h-full rounded-full ${barCor(percentual)}`}
              style={{ width: `${percentual}%` }}
            />
          </div>
        </button>
      ))}
      {sorted.length === 0 && (
        <p className="text-sm text-gray-400">Nenhum dado de tópico disponível.</p>
      )}
    </div>
  )
}
