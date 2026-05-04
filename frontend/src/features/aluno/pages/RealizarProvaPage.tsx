import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Skeleton } from '@/components/feedback/Skeleton'
import { ConfirmationModal } from '@/components/ui/ConfirmationModal'
import { alunoService } from '../services/aluno.service'
import { useProvaPlayer } from '../hooks/useProvaPlayer'
import { ProvaTimer } from '../components/ProvaTimer'
import { AbaInativaModal } from '../components/AbaInativaModal'
import { AtividadeProgressBar } from '../components/AtividadeProgressBar'
import { QuestaoNavigator } from '../components/QuestaoNavigator'
import { QuestaoRenderer } from '../components/QuestaoRenderer'
import type { AtividadeDetalhe } from '../types'

// Reutiliza AtividadeDetalhe como shape da prova — mesmos campos que o backend retorna
type ProvaDetalhe = AtividadeDetalhe

export function RealizarProvaPage() {
  const { id } = useParams<{ id: string }>()
  const provaId = Number(id)

  const { data: prova, isLoading, isError } = useQuery({
    queryKey: ['prova', provaId],
    queryFn: () => alunoService.getAtividade(provaId),
    enabled: !!provaId,
  })

  if (isLoading) {
    return (
      <div className="flex min-h-screen flex-col gap-4 bg-gray-50 px-4 py-4">
        <Skeleton className="h-14 w-full rounded-xl" />
        <Skeleton className="h-8 w-full" />
        <Skeleton className="h-64 w-full rounded-xl" />
      </div>
    )
  }

  if (isError || !prova) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <p className="text-sm text-red-600">Erro ao carregar a prova.</p>
      </div>
    )
  }

  return <ProvaPlayer prova={prova} provaId={provaId} />
}

function ProvaPlayer({ prova, provaId }: { prova: ProvaDetalhe; provaId: number }) {
  const navigate = useNavigate()
  const [modalConfirm, setModalConfirm] = useState(false)

  const {
    sessao, indiceAtual, setIndiceAtual, respostas, setResposta,
    isIniciando, isSubmitting, isOffline,
    horaAbaInativa, fecharModalAba,
    iniciarSessao, contarRespondidas, entregar, onTimerExpire,
  } = useProvaPlayer(provaId)

  // Inicia sessão automaticamente ao montar
  useEffect(() => { iniciarSessao() }, [iniciarSessao])

  const questaoAtual = prova.questoes[indiceAtual]
  const totalQuestoes = prova.questoes.length
  const respondidas = contarRespondidas(totalQuestoes)
  const todasRespondidas = respondidas === totalQuestoes

  if (isIniciando || !sessao) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-gray-50">
        <svg className="h-8 w-8 animate-spin text-blue-600" viewBox="0 0 24 24" fill="none">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
        </svg>
        <p className="text-sm text-gray-600">Iniciando sessão da prova...</p>
      </div>
    )
  }

  return (
    <div className="flex min-h-screen flex-col bg-gray-50">
      {/* Banner offline */}
      {isOffline && (
        <div className="bg-orange-500 px-4 py-2 text-center text-xs font-semibold text-white">
          Você está sem conexão. Suas respostas estão salvas localmente e serão sincronizadas ao reconectar.
        </div>
      )}

      {/* Header */}
      <header className="sticky top-0 z-10 border-b border-gray-200 bg-white px-4 py-3">
        <div className="flex items-center justify-between gap-3">
          <div className="min-w-0 flex-1">
            <p className="truncate text-xs text-gray-500">{prova.disciplina}</p>
            <h1 className="truncate text-sm font-bold text-gray-900">{prova.titulo}</h1>
          </div>

          <ProvaTimer
            iniciadaEm={sessao.iniciadaEm}
            duracaoMinutos={sessao.duracaoMinutos}
            onExpire={onTimerExpire}
          />

          <button
            type="button"
            onClick={() => navigate('/aluno/feed')}
            className="shrink-0 rounded-lg p-1.5 text-gray-400 hover:bg-gray-100"
            aria-label="Sair da prova"
          >
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </header>

      <main className="flex flex-1 flex-col gap-4 px-4 py-4">
        {/* Progresso */}
        <AtividadeProgressBar
          atual={indiceAtual + 1}
          total={totalQuestoes}
          respondidas={respondidas}
        />

        {/* Questão */}
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
          questoes={prova.questoes}
          indiceAtual={indiceAtual}
          respostas={respostas}
          onNavegar={setIndiceAtual}
        />
      </main>

      {/* Footer */}
      <div className="sticky bottom-0 border-t border-gray-200 bg-white px-4 py-3">
        <button
          type="button"
          onClick={() => setModalConfirm(true)}
          disabled={isSubmitting || !todasRespondidas}
          className="w-full rounded-xl bg-blue-600 py-3 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isSubmitting ? 'Entregando...' : 'Entregar prova'}
        </button>
        {!todasRespondidas && (
          <p className="mt-2 text-center text-xs text-gray-400">
            Responda todas as {totalQuestoes} questões para entregar
          </p>
        )}
      </div>

      {/* Modal de confirmação de entrega */}
      <ConfirmationModal
        isOpen={modalConfirm}
        title="Confirmar entrega da prova"
        description={`Você está prestes a entregar "${prova.titulo}". Após entregar não será possível alterar as respostas.`}
        nivel="medio"
        confirmLabel="Entregar"
        isLoading={isSubmitting}
        onConfirm={async () => { await entregar(); setModalConfirm(false) }}
        onCancel={() => setModalConfirm(false)}
      />

      {/* Modal de aba inativa */}
      <AbaInativaModal hora={horaAbaInativa} onFechar={fecharModalAba} />
    </div>
  )
}
