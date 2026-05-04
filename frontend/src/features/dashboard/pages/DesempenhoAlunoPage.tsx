import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useMutation } from '@tanstack/react-query'
import { Skeleton } from '@/components/feedback/Skeleton'
import { toastEmitter } from '@/lib/toastEmitter'
import { dashboardService } from '../services/dashboard.service'
import { SituacaoBanner } from '../components/SituacaoBanner'
import { EvolucaoNotasChart } from '../components/EvolucaoNotasChart'
import { FrequenciaMensalChart } from '../components/FrequenciaMensalChart'
import { TopicosAcertoList } from '../components/TopicosAcertoList'
import { ObservacoesSection } from '../components/ObservacoesSection'

export function DesempenhoAlunoPage() {
  const { turmaId, alunoId } = useParams<{ turmaId: string; alunoId: string }>()
  const navigate = useNavigate()
  const tId = Number(turmaId)

  const { data, isLoading, isError } = useQuery({
    queryKey: ['desempenho-aluno', tId, alunoId],
    queryFn: () => dashboardService.getDesempenhoAluno(tId, alunoId!),
    enabled: !!tId && !!alunoId,
  })

  const { mutate: exportar, isPending: exportando } = useMutation({
    mutationFn: () => dashboardService.exportarPDF('aluno', alunoId!),
    onSuccess: () => toastEmitter.emit('PDF gerado. Você será notificado quando estiver pronto.', 'success'),
    onError: () => toastEmitter.emit('Erro ao gerar PDF. Tente novamente.', 'error'),
  })

  return (
    <div className="flex flex-col gap-6 px-4 py-6 sm:px-6">
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={() => navigate(`/professor/turmas/${turmaId}/desempenho`)}
          className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100"
          aria-label="Voltar"
        >
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <div className="flex-1">
          <h1 className="text-lg font-bold text-gray-900">
            {data?.aluno.nome ?? 'Desempenho do Aluno'}
          </h1>
          <p className="text-xs text-gray-500">Análise individual</p>
        </div>
        <button
          type="button"
          onClick={() => exportar()}
          disabled={exportando || !data}
          className="flex items-center gap-1.5 rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50 disabled:opacity-50"
        >
          <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
          </svg>
          {exportando ? 'Gerando...' : 'Exportar PDF'}
        </button>
      </div>

      {isError && (
        <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          Erro ao carregar dados do aluno. Tente novamente.
        </div>
      )}

      {isLoading && (
        <div className="flex flex-col gap-4">
          <Skeleton className="h-12 w-full rounded-xl" />
          <Skeleton className="h-56 w-full rounded-xl" />
          <Skeleton className="h-48 w-full rounded-xl" />
        </div>
      )}

      {data && (
        <>
          <SituacaoBanner situacao={data.situacao} />

          {data.evolucaoNotas.length > 0 && (
            <section className="flex flex-col gap-3">
              <h2 className="text-sm font-semibold text-gray-700">Evolução das notas</h2>
              <EvolucaoNotasChart dados={data.evolucaoNotas} />
            </section>
          )}

          {data.frequenciaMensal.length > 0 && (
            <section className="flex flex-col gap-3">
              <h2 className="text-sm font-semibold text-gray-700">Frequência mensal</h2>
              <FrequenciaMensalChart dados={data.frequenciaMensal} />
            </section>
          )}

          {data.topicosAcerto.length > 0 && (
            <section className="flex flex-col gap-3">
              <h2 className="text-sm font-semibold text-gray-700">Desempenho por tópico</h2>
              <TopicosAcertoList topicos={data.topicosAcerto} />
            </section>
          )}

          <section className="flex flex-col gap-3">
            <h2 className="text-sm font-semibold text-gray-700">Observações</h2>
            <ObservacoesSection
              turmaId={tId}
              alunoId={alunoId!}
              observacoes={data.observacoes}
            />
          </section>
        </>
      )}
    </div>
  )
}
