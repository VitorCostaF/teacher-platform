import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Skeleton } from '@/components/feedback/Skeleton'
import type { AlertaItem } from '../types'

const STORAGE_KEY = 'dashboard_alertas_descartados'

function loadDescartados(): Set<number> {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    return raw ? new Set(JSON.parse(raw) as number[]) : new Set()
  } catch {
    return new Set()
  }
}

function saveDescartados(ids: Set<number>) {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify([...ids]))
}

interface Props {
  alertas: AlertaItem[]
  isLoading: boolean
}

export function AlertasSection({ alertas, isLoading }: Props) {
  const navigate = useNavigate()
  const [descartados, setDescartados] = useState<Set<number>>(loadDescartados)

  useEffect(() => {
    saveDescartados(descartados)
  }, [descartados])

  function descartar(id: number, e: React.MouseEvent) {
    e.stopPropagation()
    setDescartados(prev => new Set([...prev, id]))
  }

  if (isLoading) {
    return (
      <div className="flex flex-col gap-2">
        <Skeleton className="h-14 w-full rounded-xl" />
        <Skeleton className="h-14 w-full rounded-xl" />
        <Skeleton className="h-14 w-full rounded-xl" />
      </div>
    )
  }

  const visiveis = alertas.filter(a => !descartados.has(a.id))

  if (visiveis.length === 0) {
    return (
      <div className="rounded-xl border border-gray-200 bg-gray-50 px-4 py-6 text-center text-sm text-gray-500">
        Nenhuma pendência no momento.
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-2">
      {visiveis.map(alerta => (
        <div
          key={alerta.id}
          onClick={() => navigate(alerta.referenciaUrl)}
          className={[
            'flex cursor-pointer items-center justify-between gap-3 rounded-xl border px-4 py-3',
            alerta.tipo === 'urgente'
              ? 'border-red-200 bg-red-50 hover:bg-red-100'
              : 'border-yellow-200 bg-yellow-50 hover:bg-yellow-100',
          ].join(' ')}
        >
          <div className="flex items-center gap-3">
            <span className="text-lg">{alerta.tipo === 'urgente' ? '🔴' : '⚠️'}</span>
            <p className="text-sm text-gray-800">{alerta.descricao}</p>
          </div>
          <button
            type="button"
            onClick={(e) => descartar(alerta.id, e)}
            className="shrink-0 rounded-full p-1 text-gray-400 hover:bg-black/10 hover:text-gray-600"
            aria-label="Descartar alerta"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      ))}
    </div>
  )
}
