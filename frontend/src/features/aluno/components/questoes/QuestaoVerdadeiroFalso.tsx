interface Props {
  resposta: number | null
  onChange: (valor: number) => void
}

const OPCOES = [
  { valor: 1, label: 'Verdadeiro', emoji: '✓', ativo: 'border-green-400 bg-green-50 text-green-800' },
  { valor: 0, label: 'Falso', emoji: '✗', ativo: 'border-red-400 bg-red-50 text-red-800' },
]

export function QuestaoVerdadeiroFalso({ resposta, onChange }: Props) {
  return (
    <div className="flex gap-3">
      {OPCOES.map(op => (
        <button
          key={op.valor}
          type="button"
          onClick={() => onChange(op.valor)}
          className={[
            'flex flex-1 items-center justify-center gap-2 rounded-xl border-2 py-4 text-sm font-semibold transition-colors',
            resposta === op.valor
              ? op.ativo
              : 'border-gray-200 bg-white text-gray-600 hover:border-gray-300',
          ].join(' ')}
        >
          <span className="text-base">{op.emoji}</span>
          {op.label}
        </button>
      ))}
    </div>
  )
}
