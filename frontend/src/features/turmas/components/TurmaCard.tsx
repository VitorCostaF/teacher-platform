import { useNavigate } from 'react-router-dom'
import type { TurmaResumo } from '../types'

interface TurmaCardProps {
  turma: TurmaResumo
}

export function TurmaCard({ turma }: TurmaCardProps) {
  const navigate = useNavigate()

  return (
    <div
      role="button"
      tabIndex={0}
      onClick={() => navigate(`/professor/turmas/${turma.id}`)}
      onKeyDown={e => e.key === 'Enter' && navigate(`/professor/turmas/${turma.id}`)}
      className="cursor-pointer rounded-xl border border-gray-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
    >
      <div className="mb-3">
        <h3 className="truncate text-base font-semibold text-gray-900">{turma.nome}</h3>
        <p className="text-sm text-gray-500">{turma.disciplina}</p>
      </div>

      <div className="mb-4 flex items-center gap-1 text-sm text-gray-600">
        <svg className="h-4 w-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20H7m10 0v-2a4 4 0 00-4-4H7a4 4 0 00-4 4v2m14-10a4 4 0 11-8 0 4 4 0 018 0z" />
        </svg>
        <span>{turma.totalAlunos} aluno{turma.totalAlunos !== 1 ? 's' : ''}</span>
      </div>

      {turma.proximaAula && (
        <p className="mb-3 text-xs text-gray-400">
          Próxima aula: {turma.proximaAula}
        </p>
      )}

      {(turma.pendencias.frequenciasNaoLancadas > 0 || turma.pendencias.atividadesNaoCorrigidas > 0) && (
        <div className="flex flex-wrap gap-2">
          {turma.pendencias.frequenciasNaoLancadas > 0 && (
            <span className="inline-flex items-center rounded-full bg-yellow-100 px-2.5 py-0.5 text-xs font-medium text-yellow-800">
              {turma.pendencias.frequenciasNaoLancadas} frequência{turma.pendencias.frequenciasNaoLancadas !== 1 ? 's' : ''} pendente{turma.pendencias.frequenciasNaoLancadas !== 1 ? 's' : ''}
            </span>
          )}
          {turma.pendencias.atividadesNaoCorrigidas > 0 && (
            <span className="inline-flex items-center rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-medium text-red-800">
              {turma.pendencias.atividadesNaoCorrigidas} atividade{turma.pendencias.atividadesNaoCorrigidas !== 1 ? 's' : ''} para corrigir
            </span>
          )}
        </div>
      )}
    </div>
  )
}
