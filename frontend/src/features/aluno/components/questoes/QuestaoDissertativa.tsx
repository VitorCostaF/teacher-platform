const MAX = 2000

interface Props {
  resposta: string
  onChange: (valor: string) => void
}

export function QuestaoDissertativa({ resposta, onChange }: Props) {
  const restante = MAX - resposta.length
  const pct = (resposta.length / MAX) * 100

  return (
    <div className="flex flex-col gap-2">
      <textarea
        value={resposta}
        onChange={e => onChange(e.target.value.slice(0, MAX))}
        rows={6}
        placeholder="Digite sua resposta aqui..."
        className="w-full resize-none rounded-xl border border-gray-300 px-4 py-3 text-sm text-gray-900 placeholder-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
      />
      <div className="flex items-center justify-between text-xs text-gray-400">
        <div className="h-1 w-24 overflow-hidden rounded-full bg-gray-200">
          <div
            className={`h-full rounded-full transition-all ${pct > 90 ? 'bg-red-400' : 'bg-blue-400'}`}
            style={{ width: `${pct}%` }}
          />
        </div>
        <span className={restante < 100 ? 'text-red-500' : ''}>
          {restante} caracteres restantes
        </span>
      </div>
    </div>
  )
}
