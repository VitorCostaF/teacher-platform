interface Props {
  atual: number
  total: number
  respondidas: number
}

export function AtividadeProgressBar({ atual, total, respondidas }: Props) {
  const pct = total === 0 ? 0 : Math.round((respondidas / total) * 100)

  return (
    <div className="flex flex-col gap-1.5">
      <div className="flex items-center justify-between text-xs">
        <span className="text-gray-500">
          Questão <span className="font-semibold text-gray-900">{atual}</span> de{' '}
          <span className="font-semibold text-gray-900">{total}</span>
        </span>
        <span className="text-gray-500">
          <span className="font-semibold text-green-600">{respondidas}</span> respondidas ({pct}%)
        </span>
      </div>
      <div className="h-1.5 w-full overflow-hidden rounded-full bg-gray-200">
        <div
          className="h-full rounded-full bg-green-500 transition-all duration-300"
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  )
}
