import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Skeleton } from '@/components/feedback/Skeleton'
import { ConfirmationModal } from '@/components/ui/ConfirmationModal'
import { alunoService } from '../services/aluno.service'
import { useAtividadePlayer } from '../hooks/useAtividadePlayer'
import { AtividadeProgressBar } from '../components/AtividadeProgressBar'
import { QuestaoNavigator } from '../components/QuestaoNavigator'
import { QuestaoRenderer } from '../components/QuestaoRenderer'

export function RealizarAtividadePage() {
  const { id } = useParams<{ id: string }>()
  const atividadeId = Number(id)
  const navigate = useNavigate()
  const [modalAberto, setModalAberto] = useState(false)

  const { data: atividade, isLoading, isError } = useQuery({
    queryKey: ['atividade', atividadeId],
    queryFn: () => alunoService.getAtividade(atividadeId),
    enabled: !!atividadeId,
  })

  if (isLoading) {
    return (
      <div className="flex min-h-screen flex-col gap-4 bg-gray-50 px-4 py-4">
        <Skeleton className="h-14 w-full rounded-xl" />
        <Skeleton className="h-8 w-full" />
        <Skeleton className="h-64 w-full rounded-xl" />
        <Skeleton className="h-24 w-full rounded-xl" />
      </div>
    )
  }

  if (isError || !atividade) {
    return (
      <div className="flex min-h-screen items-center justify-center px-4">
        <p className="text-sm text-red-600">Erro ao carregar atividade.</p>
      </div>
    )
  }

  return <AtividadePlayer atividade={atividade} onSair={() => navigate('/aluno/feed')} />
}

function AtividadePlayer({
  atividade,
  onSair,
}: {
  atividade: import('../types').AtividadeDetalhe
  onSair: () => void
}) {
  const [modalAberto, setModalAberto] = useState(false)
  const {
    indiceAtual, setIndiceAtual, respostas, setResposta,
    isSubmitting, isAutoSaving, respondidas, isTodaRespondida,
    salvarRascunho, entregar,
  } = useAtividadePlayer(atividade)

  const questaoAtual = atividade.questoes[indiceAtual]
  const agora = new Date()
  const prazo = new Date(atividade.prazo)
  const prazoVencido = prazo < agora
  const bloqueado = prazoVencido && !atividade.permiteAtraso

  return (
    <div className="flex min-h-screen flex-col bg-gray-50">
      {/* Header */}
      <header className="sticky top-0 z-10 border-b border-gray-200 bg-white px-4 py-3">
        <div className="flex items-center justify-between gap-2">
          <div className="min-w-0">
            <p className="truncate text-xs text-gray-500">{atividade.disciplina}</p>
            <h1 className="truncate text-sm font-bold text-gray-900">{atividade.titulo}</h1>
          </div>
          <div className="flex items-center gap-2 shrink-0">
            {isAutoSaving && (
              <span className="text-xs text-gray-400 italic">Salvando...</span>
            )}
            <button
              type="button"
              onClick={onSair}
              className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
            >
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>
      </header>

      <main className="flex flex-1 flex-col gap-4 px-4 py-4">
        {/* Alertas de prazo */}
        {bloqueado && (
          <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
            O prazo desta atividade encerrou e ela não aceita entregas atrasadas.
          </div>
        )}
        {prazoVencido && !bloqueado && (
          <div className="rounded-xl border border-yellow-200 bg-yellow-50 p-3 text-sm text-yellow-800">
            ⚠️ Prazo encerrado — esta entrega será marcada como atrasada.
          </div>
        )}

        {/* Progress */}
        <AtividadeProgressBar
          atual={indiceAtual + 1}
          total={atividade.questoes.length}
          respondidas={respondidas}
        />

        {/* Enunciado */}
        {questaoAtual && (
          <div className="flex flex-col gap-4 rounded-xl border border-gray-200 bg-white p-4">
            <div className="flex items-start gap-3">
              <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-blue-600 text-xs font-bold text-white">
                {questaoAtual.numero}
              </span>
              <p className="text-sm font-medium text-gray-900 leading-relaxed">
                {questaoAtual.enunciado}
              </p>
            </div>

            <QuestaoRenderer
              questao={questaoAtual}
              respostas={respostas}
              onResposta={setResposta}
            />
          </div>
        )}

        {/* Navegação */}
        <QuestaoNavigator
          questoes={atividade.questoes}
          indiceAtual={indiceAtual}
          respostas={respostas}
          onNavegar={setIndiceAtual}
        />
      </main>

      {/* Footer de ações */}
      <div className="sticky bottom-0 border-t border-gray-200 bg-white px-4 py-3">
        <div className="flex gap-3">
          <button
            type="button"
            onClick={salvarRascunho}
            disabled={isSubmitting || bloqueado}
            className="flex-1 rounded-xl border border-gray-300 bg-white py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-40"
          >
            Salvar rascunho
          </button>
          <button
            type="button"
            onClick={() => setModalAberto(true)}
            disabled={isSubmitting || bloqueado || !isTodaRespondida()}
            className="flex-1 rounded-xl bg-blue-600 py-2.5 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {isSubmitting ? 'Entregando...' : 'Entregar'}
          </button>
        </div>
        {!isTodaRespondida() && !bloqueado && (
          <p className="mt-2 text-center text-xs text-gray-400">
            Responda todas as {atividade.questoes.length} questões para entregar
          </p>
        )}
      </div>

      <ConfirmationModal
        isOpen={modalAberto}
        title="Confirmar entrega"
        description={`Você está prestes a entregar "${atividade.titulo}". Após a entrega não será possível alterar as respostas.`}
        nivel="medio"
        confirmLabel="Entregar atividade"
        isLoading={isSubmitting}
        onConfirm={async () => { await entregar(); setModalAberto(false) }}
        onCancel={() => setModalAberto(false)}
      />
    </div>
  )
}
