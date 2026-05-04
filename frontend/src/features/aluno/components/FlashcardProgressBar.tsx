interface Props {
  respondidos: number
  total: number
}

export function FlashcardProgressBar({ respondidos, total }: Props) {
  const pct = total === 0 ? 0 : Math.round((respondidos / total) * 100)

  return (
    <div className="flex flex-col gap-1.5">
      <div className="flex items-center justify-between text-xs text-gray-500">
        <span>{respondidos} de {total} revisados</span>
        <span className="font-medium text-gray-700">{pct}%</span>
      </div>
      <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200">
        <div
          className="h-full rounded-full bg-blue-500 transition-all duration-300"
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  )
}
