import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Skeleton } from '@/components/feedback/Skeleton'
import { dashboardService } from '../services/dashboard.service'
import { HistogramaNotas } from '../components/HistogramaNotas'
import { RankingAlunosTable } from '../components/RankingAlunosTable'

function MetricaCard({ label, valor, sub }: { label: string; valor: string; sub?: string }) {
  return (
    <div className="flex flex-col gap-1 rounded-xl border border-gray-200 bg-white p-4 text-center">
      <p className="text-xs text-gray-500">{label}</p>
      <p className="text-2xl font-black text-gray-900">{valor}</p>
      {sub && <p className="text-xs text-gray-400">{sub}</p>}
    </div>
  )
}

export function DesempenhoTurmaPage() {
  const { turmaId } = useParams<{ turmaId: string }>()
  const navigate = useNavigate()
  const id = Number(turmaId)

  const { data, isLoading, isError } = useQuery({
    queryKey: ['desempenho-turma', id],
    queryFn: () => dashboardService.getDesempenhoTurma(id),
    enabled: !!id,
  })

  return (
    <div className="flex flex-col gap-6 px-4 py-6 sm:px-6">
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={() => navigate(`/professor/turmas/${turmaId}`)}
          className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100"
          aria-label="Voltar"
        >
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <div>
          <h1 className="text-lg font-bold text-gray-900">Desempenho da Turma</h1>
          <p className="text-xs text-gray-500">Métricas e ranking de alunos</p>
        </div>
      </div>

      {isError && (
        <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          Erro ao carregar desempenho. Tente novamente.
        </div>
      )}

      {isLoading && (
        <div className="flex flex-col gap-4">
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
            {Array.from({ length: 4 }).map((_, i) => (
              <Skeleton key={i} className="h-20 w-full rounded-xl" />
            ))}
          </div>
          <Skeleton className="h-56 w-full rounded-xl" />
          <Skeleton className="h-48 w-full rounded-xl" />
        </div>
      )}

      {data && (
        <>
          {/* Métricas */}
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
            <MetricaCard label="Média geral" valor={data.mediaGeral.toFixed(1)} />
            <MetricaCard label="Aprovação" valor={`${data.percentualAprovacao.toFixed(0)}%`} />
            <MetricaCard label="Frequência" valor={`${data.percentualFrequencia.toFixed(0)}%`} />
            <MetricaCard
              label="Notas"
              valor={`${data.maiorNota.toFixed(1)} / ${data.menorNota.toFixed(1)}`}
              sub="Maior / Menor"
            />
          </div>

          {/* Histograma */}
          <section className="flex flex-col gap-3">
            <h2 className="text-sm font-semibold text-gray-700">Distribuição de notas</h2>
            <HistogramaNotas histograma={data.histograma} />
          </section>

          {/* Ranking */}
          <section className="flex flex-col gap-3">
            <h2 className="text-sm font-semibold text-gray-700">Ranking de alunos</h2>
            <RankingAlunosTable ranking={data.ranking} turmaId={id} />
          </section>
        </>
      )}
    </div>
  )
}
