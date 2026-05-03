import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ConfirmationModal } from '@/components/ui/ConfirmationModal'
import { useConfirmationModal } from '@/hooks/useConfirmationModal'
import { Skeleton } from '@/components/feedback/Skeleton'
import { useAlunosTurma } from '@/features/turmas/hooks/useAlunosTurma'
import { useTurmaDetalhe } from '@/features/turmas/hooks/useTurmaDetalhe'
import { AlunoFrequenciaRow } from '../components/AlunoFrequenciaRow'
import { FrequenciaDatePicker } from '../components/FrequenciaDatePicker'
import { FrequenciaStickyFooter } from '../components/FrequenciaStickyFooter'
import { useFrequencia } from '../hooks/useFrequencia'

export function LancamentoFrequenciaPage() {
  const { turmaId } = useParams<{ turmaId: string }>()
  const navigate = useNavigate()
  const id = Number(turmaId)

  const { data: turma } = useTurmaDetalhe(id)
  const { data: alunos = [], isLoading: loadingAlunos } = useAlunosTurma(id)

  const {
    selectedDate,
    frequenciaMap,
    existingId,
    isLoadingDate,
    isSaving,
    isDirty,
    totalPreenchidos,
    isTodoPreenchido,
    mudarData,
    setStatus,
    setObservacao,
    marcarTodosPresentes,
    salvar,
  } = useFrequencia(id, alunos)

  const [pendingDate, setPendingDate] = useState<Date | null>(null)
  const { open: openMudarData, close: closeMudarData, modalProps: mudarDataModalProps } = useConfirmationModal()

  const handleDateChange = (novaData: Date) => {
    if (isDirty) {
      setPendingDate(novaData)
      openMudarData({
        title: 'Trocar data',
        description: 'Há alterações não salvas nesta data. Ao continuar, as alterações serão perdidas.',
        nivel: 'medio',
        confirmLabel: 'Trocar mesmo assim',
        onConfirm: async () => {
          mudarData(novaData)
          setPendingDate(null)
        },
      })
    } else {
      mudarData(novaData)
    }
  }

  const isDisabled = isLoadingDate || isSaving || loadingAlunos

  return (
    <div className="min-h-screen bg-gray-50 pb-24">
      {/* Header */}
      <div className="sticky top-0 z-20 border-b border-gray-200 bg-white shadow-sm">
        <div className="mx-auto max-w-3xl px-4 py-4 sm:px-6">
          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate(`/professor/turmas/${id}`)}
              className="rounded-lg p-1.5 text-gray-500 hover:bg-gray-100 hover:text-gray-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-gray-400"
              aria-label="Voltar"
            >
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <div className="min-w-0">
              <h1 className="text-base font-semibold text-gray-900 sm:text-lg">
                {turma?.nome ?? 'Lançar Frequência'}
              </h1>
              {turma && (
                <p className="truncate text-xs text-gray-500">{turma.disciplina}</p>
              )}
            </div>
          </div>

          <div className="mt-4">
            <FrequenciaDatePicker
              selected={selectedDate}
              onChange={handleDateChange}
              hasRegistro={existingId !== null}
              disabled={isDisabled}
            />
          </div>
        </div>
      </div>

      {/* Lista de alunos */}
      <div className="mx-auto max-w-3xl px-0 py-4 sm:px-6">
        <div className="rounded-none border-y border-gray-200 bg-white sm:rounded-xl sm:border">
          {loadingAlunos || isLoadingDate ? (
            <ul className="divide-y divide-gray-100 px-4">
              {Array.from({ length: 5 }).map((_, i) => (
                <li key={i} className="flex items-center gap-3 py-3">
                  <Skeleton className="h-10 w-10 rounded-full" />
                  <div className="flex-1 space-y-2">
                    <Skeleton className="h-4 w-36" />
                  </div>
                  <Skeleton className="h-11 w-56 rounded-lg" />
                </li>
              ))}
            </ul>
          ) : alunos.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-16 text-center">
              <p className="text-sm text-gray-500">Nenhum aluno nesta turma</p>
            </div>
          ) : (
            <ul>
              {alunos.map(aluno => (
                <AlunoFrequenciaRow
                  key={aluno.id}
                  aluno={aluno}
                  frequencia={frequenciaMap[aluno.id] ?? { alunoId: aluno.id, status: null, observacao: '' }}
                  onStatusChange={setStatus}
                  onObservacaoChange={setObservacao}
                  disabled={isDisabled}
                />
              ))}
            </ul>
          )}
        </div>

        {existingId !== null && !isLoadingDate && (
          <p className="mt-3 px-4 text-xs text-gray-400 sm:px-0">
            Frequência já registrada para esta data. Salvar irá atualizar os registros existentes.
          </p>
        )}
      </div>

      {/* Footer sticky */}
      {!loadingAlunos && alunos.length > 0 && (
        <FrequenciaStickyFooter
          totalPreenchidos={totalPreenchidos}
          totalAlunos={alunos.length}
          isSaving={isSaving}
          onSalvar={salvar}
          onMarcarTodosPresentes={marcarTodosPresentes}
        />
      )}

      {/* Modal troca de data com alterações não salvas */}
      <ConfirmationModal {...mudarDataModalProps} />
    </div>
  )
}
