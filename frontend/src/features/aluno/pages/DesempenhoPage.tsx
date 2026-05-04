import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Skeleton } from '@/components/feedback/Skeleton'
import { alunoService } from '../services/aluno.service'
import { BottomNavigation } from '../components/BottomNavigation'
import { ResumoCardsAluno } from '../components/ResumoCardsAluno'
import { DisciplinaCard } from '../components/DisciplinaCard'
import { EvolucaoNotasAlunoChart } from '../components/EvolucaoNotasAlunoChart'
import { ConquistasSection } from '../components/ConquistasSection'

function DesempenhoSkeleton() {
  return (
    <div className="flex flex-col gap-5 px-4 py-4">
      <div className="grid grid-cols-3 gap-3">
        <Skeleton className="h-20" />
        <Skeleton className="h-20" />
        <Skeleton className="h-20" />
      </div>
      <div className="flex flex-col gap-3">
        <Skeleton className="h-4 w-32" />
        <Skeleton className="h-24 w-full" />
        <Skeleton className="h-24 w-full" />
      </div>
      <Skeleton className="h-48 w-full rounded-xl" />
    </div>
  )
}

export function DesempenhoPage() {
  const navigate = useNavigate()

  const { data, isLoading, isError } = useQuery({
    queryKey: ['aluno', 'desempenho'],
    queryFn: alunoService.getDesempenho,
  })

  // Tópico com menor acerto para atalho de flashcards
  const disciplinaFraca = data?.porDisciplina.reduce(
    (menor, d) => (d.media < (menor?.media ?? Infinity) ? d : menor),
    undefined as typeof data.porDisciplina[0] | undefined
  )

  return (
    <div className="flex min-h-screen flex-col bg-gray-50 pb-20">
      {/* Header */}
      <header className="border-b border-gray-200 bg-white px-4 py-4">
        <h1 className="text-base font-bold text-gray-900">Meu Desempenho</h1>
        <p className="mt-0.5 text-xs text-gray-500">Acompanhe sua evolução e conquistas</p>
      </header>

      {isLoading && <DesempenhoSkeleton />}

      {isError && (
        <div className="mx-4 mt-4 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          Erro ao carregar desempenho. Tente novamente.
        </div>
      )}

      {data && (
        <main className="flex flex-1 flex-col gap-5 px-4 py-4">
          {/* Resumo */}
          <ResumoCardsAluno data={data} />

          {/* Atalho flashcards */}
          {disciplinaFraca && (
            <button
              type="button"
              onClick={() => navigate('/aluno/flashcards')}
              className="flex items-center gap-3 rounded-xl border border-blue-200 bg-blue-50 p-3.5 text-left"
            >
              <span className="text-2xl">📚</span>
              <div className="flex flex-col">
                <p className="text-sm font-semibold text-blue-900">
                  Revisar {disciplinaFraca.nome}
                </p>
                <p className="text-xs text-blue-700">
                  Sua menor média — pratique com flashcards
                </p>
              </div>
              <svg className="ml-auto h-4 w-4 shrink-0 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
              </svg>
            </button>
          )}

          {/* Por disciplina */}
          {data.porDisciplina.length > 0 && (
            <section className="flex flex-col gap-3">
              <h2 className="text-sm font-semibold text-gray-700">Por Disciplina</h2>
              <div className="flex flex-col gap-3">
                {data.porDisciplina.map(d => (
                  <DisciplinaCard key={d.id} disciplina={d} />
                ))}
              </div>
            </section>
          )}

          {/* Gráfico */}
          <EvolucaoNotasAlunoChart evolucaoNotas={data.evolucaoNotas} />

          {/* Conquistas */}
          <ConquistasSection conquistas={data.conquistas} />
        </main>
      )}

      <BottomNavigation />
    </div>
  )
}
