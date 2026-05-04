import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Skeleton } from '@/components/feedback/Skeleton'
import { alunoService } from '../services/aluno.service'
import { ResultadoHeader } from '../components/ResultadoHeader'
import { GabaritoQuestaoCard } from '../components/GabaritoQuestaoCard'
import { AnaliseIASection } from '../components/AnaliseIASection'

function ResultadoSkeleton() {
  return (
    <div className="flex flex-col gap-4 px-4 py-4">
      <Skeleton className="h-40 w-full rounded-2xl" />
      <Skeleton className="h-4 w-32" />
      <Skeleton className="h-28 w-full rounded-xl" />
      <Skeleton className="h-28 w-full rounded-xl" />
      <Skeleton className="h-28 w-full rounded-xl" />
    </div>
  )
}

export function ResultadoPage() {
  const { entregaId } = useParams<{ entregaId: string }>()
  const navigate = useNavigate()

  const { data, isLoading, isError } = useQuery({
    queryKey: ['resultado', entregaId],
    queryFn: () => alunoService.getResultado(Number(entregaId)),
    enabled: !!entregaId,
  })

  return (
    <div className="flex min-h-screen flex-col bg-gray-50 pb-8">
      {/* Header */}
      <header className="flex items-center gap-3 border-b border-gray-200 bg-white px-4 py-4">
        <button
          type="button"
          onClick={() => navigate('/aluno/feed')}
          className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100"
          aria-label="Voltar ao feed"
        >
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <div>
          <h1 className="text-base font-bold text-gray-900">Resultado</h1>
          <p className="text-xs text-gray-500">Veja seu desempenho detalhado</p>
        </div>
      </header>

      {isLoading && <ResultadoSkeleton />}

      {isError && (
        <div className="mx-4 mt-4 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          Erro ao carregar resultado. Tente novamente.
        </div>
      )}

      {data && (
        <main className="flex flex-1 flex-col gap-5 px-4 py-4">
          {/* Nota e status do gabarito */}
          <ResultadoHeader resultado={data} />

          {/* Gabarito */}
          {data.gabaritoDisponivel && data.gabarito && data.gabarito.length > 0 && (
            <section className="flex flex-col gap-3">
              <h2 className="text-sm font-semibold text-gray-700">Gabarito</h2>
              {data.gabarito.map(q => (
                <GabaritoQuestaoCard key={q.questaoId} questao={q} />
              ))}
            </section>
          )}

          {/* Análise IA */}
          {data.analiseTopicos && data.analiseTopicos.length > 0 && (
            <AnaliseIASection analise={data.analiseTopicos} />
          )}

          {/* Botão voltar */}
          <button
            type="button"
            onClick={() => navigate('/aluno/feed')}
            className="w-full rounded-xl border border-gray-300 bg-white py-3 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            Voltar ao feed
          </button>
        </main>
      )}
    </div>
  )
}
