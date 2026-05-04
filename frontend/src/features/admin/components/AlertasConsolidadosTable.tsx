import { useState } from 'react'
import type { AlertaConsolidado } from '../types'

interface Props {
  alertas: AlertaConsolidado[]
}

const TIPOS = ['Todos', 'RISCO_REPROVACAO', 'ATIVIDADE_CORRIGIR']

const tipoLabel: Record<string, string> = {
  RISCO_REPROVACAO: 'Risco de reprovação',
  ATIVIDADE_CORRIGIR: 'Atividade pendente',
}

const tipoCor: Record<string, string> = {
  RISCO_REPROVACAO: 'bg-red-100 text-red-700',
  ATIVIDADE_CORRIGIR: 'bg-yellow-100 text-yellow-700',
}

const PAGE_SIZE = 8

export function AlertasConsolidadosTable({ alertas }: Props) {
  const [filtroTipo, setFiltroTipo] = useState('Todos')
  const [pagina, setPagina] = useState(0)

  const filtrados = filtroTipo === 'Todos'
    ? alertas
    : alertas.filter(a => a.tipo === filtroTipo)

  const totalPaginas = Math.ceil(filtrados.length / PAGE_SIZE)
  const visiveis = filtrados.slice(pagina * PAGE_SIZE, (pagina + 1) * PAGE_SIZE)

  return (
    <div className="flex flex-col gap-3">
      <div className="flex flex-wrap gap-2">
        {TIPOS.map(t => (
          <button
            key={t}
            type="button"
            onClick={() => { setFiltroTipo(t); setPagina(0) }}
            className={[
              'rounded-full px-3 py-1 text-xs font-medium transition-colors',
              filtroTipo === t
                ? 'bg-gray-900 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200',
            ].join(' ')}
          >
            {t === 'Todos' ? 'Todos' : tipoLabel[t] ?? t}
          </button>
        ))}
      </div>

      {visiveis.length === 0 ? (
        <p className="py-6 text-center text-sm text-gray-400">Nenhum alerta no momento.</p>
      ) : (
        <ul className="flex flex-col divide-y divide-gray-100 rounded-xl border border-gray-200 bg-white overflow-hidden">
          {visiveis.map((a, i) => (
            <li key={i} className="flex items-start gap-3 px-4 py-3">
              <span className={`mt-0.5 shrink-0 rounded-full px-2 py-0.5 text-xs font-medium ${tipoCor[a.tipo] ?? 'bg-gray-100 text-gray-600'}`}>
                {tipoLabel[a.tipo] ?? a.tipo}
              </span>
              <p className="flex-1 text-sm text-gray-700">{a.descricao}</p>
            </li>
          ))}
        </ul>
      )}

      {totalPaginas > 1 && (
        <div className="flex items-center justify-end gap-2">
          <button
            type="button"
            onClick={() => setPagina(p => Math.max(0, p - 1))}
            disabled={pagina === 0}
            className="rounded-lg border border-gray-200 px-2.5 py-1 text-xs text-gray-600 hover:bg-gray-50 disabled:opacity-40"
          >
            ←
          </button>
          <span className="text-xs text-gray-500">{pagina + 1} / {totalPaginas}</span>
          <button
            type="button"
            onClick={() => setPagina(p => Math.min(totalPaginas - 1, p + 1))}
            disabled={pagina >= totalPaginas - 1}
            className="rounded-lg border border-gray-200 px-2.5 py-1 text-xs text-gray-600 hover:bg-gray-50 disabled:opacity-40"
          >
            →
          </button>
        </div>
      )}
    </div>
  )
}
