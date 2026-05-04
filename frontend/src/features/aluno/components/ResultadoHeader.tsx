import type { ResultadoData } from '../types'

interface Props {
  resultado: ResultadoData
}

function notaCor(nota: number): string {
  if (nota >= 7) return 'text-green-600'
  if (nota >= 5) return 'text-yellow-600'
  return 'text-red-600'
}

export function ResultadoHeader({ resultado }: Props) {
  if (!resultado.gabaritoDisponivel) {
    return (
      <div className="flex flex-col items-center gap-3 rounded-2xl border border-gray-200 bg-white px-6 py-8 text-center">
        <div className="flex h-16 w-16 items-center justify-center rounded-full bg-gray-100">
          <svg className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div>
          <p className="text-base font-semibold text-gray-900">Entregue!</p>
          <p className="mt-1 text-sm text-gray-500">
            O professor irá liberar o gabarito em breve.
          </p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-col items-center gap-4 rounded-2xl border border-gray-200 bg-white px-6 py-8 text-center">
      {resultado.nota !== undefined ? (
        <>
          <p className="text-sm font-medium text-gray-500">Sua nota</p>
          <p className={`text-6xl font-black tabular-nums ${notaCor(resultado.nota)}`}>
            {resultado.nota.toFixed(1)}
          </p>
          {resultado.mediaTurma !== undefined && (
            <div className="flex items-center gap-2 rounded-full bg-gray-100 px-3 py-1">
              <span className="text-xs text-gray-500">Média da turma:</span>
              <span className="text-xs font-semibold text-gray-700">{resultado.mediaTurma.toFixed(1)}</span>
            </div>
          )}
          <div className="h-2 w-48 overflow-hidden rounded-full bg-gray-100">
            <div
              className={`h-full rounded-full ${resultado.nota >= 7 ? 'bg-green-500' : resultado.nota >= 5 ? 'bg-yellow-500' : 'bg-red-500'}`}
              style={{ width: `${(resultado.nota / 10) * 100}%` }}
            />
          </div>
        </>
      ) : (
        <p className="text-sm text-gray-500">Gabarito disponível — nota ainda não calculada.</p>
      )}
    </div>
  )
}
