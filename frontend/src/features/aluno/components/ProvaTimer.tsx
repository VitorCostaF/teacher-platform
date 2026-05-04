import { useEffect, useRef, useState } from 'react'

interface Props {
  iniciadaEm: string
  duracaoMinutos: number
  onExpire: () => void
}

function calcularRestante(iniciadaEm: string, duracaoMinutos: number): number {
  const inicio = new Date(iniciadaEm).getTime() / 1000
  const agora = Date.now() / 1000
  const total = duracaoMinutos * 60
  return Math.max(0, Math.round(total - (agora - inicio)))
}

function formatar(segundos: number): string {
  const m = Math.floor(segundos / 60)
  const s = segundos % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

export function ProvaTimer({ iniciadaEm, duracaoMinutos, onExpire }: Props) {
  const [restante, setRestante] = useState(() => calcularRestante(iniciadaEm, duracaoMinutos))
  const onExpireRef = useRef(onExpire)
  useEffect(() => { onExpireRef.current = onExpire }, [onExpire])

  const vibradoRef = useRef(false)

  useEffect(() => {
    const id = setInterval(() => {
      setRestante(prev => {
        const novo = calcularRestante(iniciadaEm, duracaoMinutos)
        if (novo <= 0) {
          clearInterval(id)
          onExpireRef.current()
          return 0
        }
        // vibração única ao entrar nos 5 minutos
        if (novo <= 300 && !vibradoRef.current) {
          vibradoRef.current = true
          try { navigator.vibrate?.([200, 100, 200]) } catch { /* não suportado */ }
        }
        return novo
      })
    }, 1000)
    return () => clearInterval(id)
  }, [iniciadaEm, duracaoMinutos])

  const pouco = restante <= 300
  const critico = restante <= 60

  return (
    <div
      className={[
        'flex items-center gap-2 rounded-xl px-4 py-2 font-mono text-lg font-bold tabular-nums',
        critico
          ? 'animate-pulse bg-red-100 text-red-700'
          : pouco
            ? 'bg-orange-100 text-orange-700'
            : 'bg-gray-100 text-gray-800',
      ].join(' ')}
      aria-label={`Tempo restante: ${formatar(restante)}`}
      role="timer"
    >
      <svg className="h-4 w-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      {formatar(restante)}
    </div>
  )
}
