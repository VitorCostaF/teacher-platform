interface Props {
  situacao: string
}

type Config = { bg: string; border: string; text: string; icon: string }

const configMap: Record<string, Config> = {
  'Aprovado em Andamento': {
    bg: 'bg-green-50', border: 'border-green-200', text: 'text-green-800', icon: '✅',
  },
  'Em Risco': {
    bg: 'bg-yellow-50', border: 'border-yellow-200', text: 'text-yellow-800', icon: '⚠️',
  },
  'Reprovado por Falta': {
    bg: 'bg-red-50', border: 'border-red-200', text: 'text-red-800', icon: '🔴',
  },
}

const defaultConfig: Config = {
  bg: 'bg-gray-50', border: 'border-gray-200', text: 'text-gray-700', icon: '📋',
}

export function SituacaoBanner({ situacao }: Props) {
  const cfg = configMap[situacao] ?? defaultConfig
  return (
    <div className={`flex items-center gap-3 rounded-xl border px-4 py-3 ${cfg.bg} ${cfg.border}`}>
      <span className="text-xl">{cfg.icon}</span>
      <p className={`text-sm font-semibold ${cfg.text}`}>{situacao}</p>
    </div>
  )
}
