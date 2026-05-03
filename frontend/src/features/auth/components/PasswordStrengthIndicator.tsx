interface PasswordStrengthIndicatorProps {
  senha: string
}

function calcScore(senha: string): number {
  let score = 0
  if (senha.length >= 8) score++
  if (/[0-9]/.test(senha)) score++
  if (/[A-Z]/.test(senha)) score++
  if (/[a-z]/.test(senha)) score++
  if (/[^A-Za-z0-9]/.test(senha)) score++
  return score
}

const levels = [
  { label: 'Fraca', barClass: 'w-1/3 bg-red-500', textClass: 'text-red-600' },
  { label: 'Média', barClass: 'w-2/3 bg-yellow-400', textClass: 'text-yellow-600' },
  { label: 'Forte', barClass: 'w-full bg-green-500', textClass: 'text-green-600' },
]

export function PasswordStrengthIndicator({ senha }: PasswordStrengthIndicatorProps) {
  if (!senha) return null

  const score = calcScore(senha)
  const levelIndex = score <= 2 ? 0 : score <= 4 ? 1 : 2
  const level = levels[levelIndex]

  return (
    <div className="flex flex-col gap-1">
      <div className="h-1.5 w-full rounded-full bg-gray-200">
        <div
          className={['h-1.5 rounded-full transition-all duration-300', level.barClass].join(' ')}
        />
      </div>
      <p className={['text-xs', level.textClass].join(' ')}>Senha {level.label}</p>
    </div>
  )
}
