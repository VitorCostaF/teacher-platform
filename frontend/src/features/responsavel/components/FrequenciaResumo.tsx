interface FrequenciaResumoProps {
  presencas: number
  totalAulas: number
  percentual: number
}

function barColor(pct: number) {
  if (pct >= 75) return 'bg-green-500'
  if (pct >= 50) return 'bg-yellow-500'
  return 'bg-red-500'
}

export function FrequenciaResumo({ presencas, totalAulas, percentual }: FrequenciaResumoProps) {
  return (
    <div className="flex flex-col gap-2 rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-gray-700">
          {presencas} / {totalAulas} aulas
        </p>
        <p className={`text-sm font-bold ${percentual >= 75 ? 'text-green-700' : 'text-red-700'}`}>
          {percentual.toFixed(1)}%
        </p>
      </div>
      <div className="relative h-3 w-full overflow-hidden rounded-full bg-gray-200">
        <div
          className={`h-full rounded-full transition-all ${barColor(percentual)}`}
          style={{ width: `${Math.min(percentual, 100)}%` }}
        />
        {/* Linha de risco em 75% */}
        <div
          className="absolute inset-y-0 w-0.5 bg-red-600"
          style={{ left: '75%' }}
          title="Mínimo: 75%"
        />
      </div>
      <p className="text-xs text-gray-500">Mínimo exigido: 75%</p>
    </div>
  )
}
