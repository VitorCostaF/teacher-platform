import type { StatusFrequencia } from '../types'

interface FrequenciaToggleProps {
  value: StatusFrequencia | null
  onChange: (v: StatusFrequencia) => void
  disabled?: boolean
}

const options: { value: StatusFrequencia; label: string; active: string; inactive: string }[] = [
  {
    value: 'PRESENTE',
    label: 'Presente',
    active: 'bg-green-600 text-white border-green-600',
    inactive: 'border-green-300 text-green-700 hover:bg-green-50',
  },
  {
    value: 'AUSENTE',
    label: 'Falta',
    active: 'bg-red-600 text-white border-red-600',
    inactive: 'border-red-300 text-red-700 hover:bg-red-50',
  },
  {
    value: 'JUSTIFICADO',
    label: 'F. Justificada',
    active: 'bg-yellow-500 text-white border-yellow-500',
    inactive: 'border-yellow-400 text-yellow-700 hover:bg-yellow-50',
  },
]

export function FrequenciaToggle({ value, onChange, disabled }: FrequenciaToggleProps) {
  return (
    <div className="flex gap-1" role="group" aria-label="Status de frequência">
      {options.map(opt => (
        <button
          key={opt.value}
          type="button"
          onClick={() => onChange(opt.value)}
          disabled={disabled}
          aria-pressed={value === opt.value}
          className={[
            'min-h-11 flex-1 rounded-lg border px-2 py-2 text-xs font-medium transition-colors',
            'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-1',
            'disabled:cursor-not-allowed disabled:opacity-50',
            value === opt.value ? opt.active : opt.inactive,
          ].join(' ')}
        >
          {opt.label}
        </button>
      ))}
    </div>
  )
}
