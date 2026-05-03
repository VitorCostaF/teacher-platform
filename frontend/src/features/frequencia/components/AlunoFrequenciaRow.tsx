import { useState } from 'react'
import type { AlunoTurma } from '@/features/turmas/types'
import type { FrequenciaAluno, StatusFrequencia } from '../types'
import { FrequenciaToggle } from './FrequenciaToggle'

interface AlunoFrequenciaRowProps {
  aluno: AlunoTurma
  frequencia: FrequenciaAluno
  onStatusChange: (alunoId: string, status: StatusFrequencia) => void
  onObservacaoChange: (alunoId: string, observacao: string) => void
  disabled?: boolean
}

function AvatarFallback({ nome }: { nome: string }) {
  const initials = nome.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase()
  return (
    <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-blue-100 text-sm font-semibold text-blue-700">
      {initials}
    </div>
  )
}

export function AlunoFrequenciaRow({
  aluno,
  frequencia,
  onStatusChange,
  onObservacaoChange,
  disabled,
}: AlunoFrequenciaRowProps) {
  const [expanded, setExpanded] = useState(false)
  const isPreenchido = frequencia.status !== null

  return (
    <li className="border-b border-gray-100 last:border-0">
      <div className="flex items-center gap-3 px-4 py-3">
        {/* Avatar */}
        <div className="relative shrink-0">
          {aluno.avatarUrl ? (
            <img src={aluno.avatarUrl} alt={aluno.nome} className="h-10 w-10 rounded-full object-cover" />
          ) : (
            <AvatarFallback nome={aluno.nome} />
          )}
          {isPreenchido && (
            <span className="absolute -right-0.5 -top-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-green-500 text-white">
              <svg className="h-2.5 w-2.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
              </svg>
            </span>
          )}
        </div>

        {/* Nome */}
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-medium text-gray-900">{aluno.nome}</p>
        </div>

        {/* Toggle + expandir */}
        <div className="flex items-center gap-2">
          <div className="w-56 sm:w-64">
            <FrequenciaToggle
              value={frequencia.status}
              onChange={status => onStatusChange(aluno.id, status)}
              disabled={disabled}
            />
          </div>
          <button
            type="button"
            onClick={() => setExpanded(v => !v)}
            aria-expanded={expanded}
            aria-label={expanded ? 'Fechar observação' : 'Adicionar observação'}
            className="shrink-0 rounded-lg p-2 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-gray-400"
          >
            <svg
              className={['h-4 w-4 transition-transform', expanded ? 'rotate-180' : ''].join(' ')}
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
            </svg>
          </button>
        </div>
      </div>

      {/* Observação expandida */}
      {expanded && (
        <div className="px-4 pb-3 pl-17">
          <textarea
            value={frequencia.observacao}
            onChange={e => onObservacaoChange(aluno.id, e.target.value)}
            placeholder="Observação (opcional)..."
            disabled={disabled}
            rows={2}
            className="w-full resize-none rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900 placeholder-gray-400 outline-none transition-colors focus:border-blue-500 focus:ring-2 focus:ring-blue-100 disabled:opacity-50"
          />
        </div>
      )}
    </li>
  )
}
