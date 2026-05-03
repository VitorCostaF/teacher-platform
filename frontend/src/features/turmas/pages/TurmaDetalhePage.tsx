import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { Button } from '@/components/ui/Button'
import { Skeleton } from '@/components/feedback/Skeleton'
import { ConfirmationModal } from '@/components/ui/ConfirmationModal'
import { useConfirmationModal } from '@/hooks/useConfirmationModal'
import { AlunoListItem } from '../components/AlunoListItem'
import { AdicionarAlunoModal } from '../components/AdicionarAlunoModal'
import { ImportarAlunosModal } from '../components/ImportarAlunosModal'
import { TurmaTabNavigation } from '../components/TurmaTabNavigation'
import { useAlunosTurma } from '../hooks/useAlunosTurma'
import { useTurmaDetalhe } from '../hooks/useTurmaDetalhe'
import { turmasService } from '../services/turmas.service'

export function TurmaDetalhePage() {
  const { turmaId } = useParams<{ turmaId: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const id = Number(turmaId)

  const { data: turma, isLoading: loadingTurma } = useTurmaDetalhe(id)
  const { data: alunos = [], isLoading: loadingAlunos } = useAlunosTurma(id)

  const [showAdicionar, setShowAdicionar] = useState(false)
  const [showImportar, setShowImportar] = useState(false)
  const { open: openRemover, close: closeRemover, modalProps } = useConfirmationModal()

  const refetchAlunos = () => {
    queryClient.invalidateQueries({ queryKey: ['turma-alunos', id] })
  }

  const handleRemover = (alunoId: string, nomeAluno: string) => {
    openRemover({
      title: `Remover ${nomeAluno}`,
      description: `O aluno será removido desta turma. O histórico de frequência e atividades será mantido, mas o aluno perderá o acesso.`,
      nivel: 'alto',
      confirmLabel: 'Remover aluno',
      onConfirm: () => turmasService.removerAluno(id, alunoId).then(() => refetchAlunos()),
    })
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="border-b border-gray-200 bg-white">
        <div className="mx-auto max-w-7xl px-4 py-5 sm:px-6 lg:px-8">
          <div className="flex items-start justify-between gap-4">
            <div>
              <button
                onClick={() => navigate('/professor/turmas')}
                className="mb-1 flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700"
              >
                <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" /></svg>
                Minhas Turmas
              </button>
              {loadingTurma ? (
                <>
                  <Skeleton className="mb-2 h-7 w-56" />
                  <Skeleton className="h-4 w-32" />
                </>
              ) : (
                <>
                  <h1 className="text-xl font-bold text-gray-900">{turma?.nome}</h1>
                  <p className="text-sm text-gray-500">{turma?.disciplina}{turma?.serie ? ` · ${turma.serie}` : ''}</p>
                </>
              )}
            </div>

            <Button onClick={() => navigate(`/professor/turmas/${id}/frequencia`)}>
              Lançar Frequência
            </Button>
          </div>

          <div className="mt-4">
            <TurmaTabNavigation turmaId={id} />
          </div>
        </div>
      </div>

      {/* Conteúdo da aba Alunos */}
      <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
        <div className="rounded-xl border border-gray-200 bg-white shadow-sm">
          <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
            <h2 className="text-sm font-semibold text-gray-900">
              Alunos{!loadingAlunos && ` (${alunos.length})`}
            </h2>
            <div className="flex gap-2">
              <Button variant="secondary" onClick={() => setShowImportar(true)}>
                Importar via planilha
              </Button>
              <Button onClick={() => setShowAdicionar(true)}>
                Adicionar aluno
              </Button>
            </div>
          </div>

          {loadingAlunos ? (
            <div className="divide-y divide-gray-100 px-6">
              {Array.from({ length: 5 }).map((_, i) => (
                <div key={i} className="flex items-center gap-3 py-3">
                  <Skeleton className="h-10 w-10 rounded-full" />
                  <div className="flex-1 space-y-1.5">
                    <Skeleton className="h-4 w-40" />
                    <Skeleton className="h-3.5 w-52" />
                  </div>
                </div>
              ))}
            </div>
          ) : alunos.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-16 text-center">
              <svg className="mb-3 h-12 w-12 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M17 20H7m10 0v-2a4 4 0 00-4-4H7a4 4 0 00-4 4v2m14-10a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
              <p className="mb-3 text-sm text-gray-500">Nenhum aluno nesta turma ainda</p>
              <Button onClick={() => setShowAdicionar(true)}>Adicionar primeiro aluno</Button>
            </div>
          ) : (
            <ul className="divide-y divide-gray-100 px-6">
              {alunos.map(aluno => (
                <AlunoListItem
                  key={aluno.id}
                  aluno={aluno}
                  onRemover={alunoId => handleRemover(alunoId, aluno.nome)}
                />
              ))}
            </ul>
          )}
        </div>
      </div>

      {/* Modais */}
      <AdicionarAlunoModal
        isOpen={showAdicionar}
        turmaId={id}
        onClose={() => setShowAdicionar(false)}
        onAdicionado={refetchAlunos}
      />

      <ImportarAlunosModal
        isOpen={showImportar}
        turmaId={id}
        onClose={() => setShowImportar(false)}
        onImportado={refetchAlunos}
      />

      <ConfirmationModal {...modalProps} />
    </div>
  )
}
