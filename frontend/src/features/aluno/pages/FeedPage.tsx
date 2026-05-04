import { useQuery } from '@tanstack/react-query'
import { Skeleton } from '@/components/feedback/Skeleton'
import { alunoService } from '../services/aluno.service'
import { AlunoHeader } from '../components/AlunoHeader'
import { BottomNavigation } from '../components/BottomNavigation'
import { FeedSection } from '../components/FeedSection'
import { FeedItemCard } from '../components/FeedItemCard'
import { ConteudoCard } from '../components/ConteudoCard'
import { RecomendacaoCard } from '../components/RecomendacaoCard'

function FeedSkeleton() {
  return (
    <div className="flex flex-col gap-4 px-4 py-4">
      <Skeleton className="h-24 w-full" />
      <Skeleton className="h-24 w-full" />
      <Skeleton className="h-24 w-full" />
    </div>
  )
}

export function FeedPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['aluno-feed'],
    queryFn: alunoService.getFeed,
  })

  return (
    <div className="flex min-h-screen flex-col bg-gray-50 pb-20">
      <AlunoHeader notificacoes={data?.urgentes?.length ?? 0} />

      <main className="flex flex-1 flex-col gap-5 px-4 py-4">
        {isLoading && <FeedSkeleton />}

        {isError && (
          <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            Erro ao carregar o feed. Tente novamente.
          </div>
        )}

        {data && (
          <>
            {/* Urgentes */}
            {data.urgentes.length > 0 && (
              <FeedSection title="⚠️ Urgente" emptyMessage="">
                {data.urgentes.map(item => (
                  <FeedItemCard key={item.id} item={item} />
                ))}
              </FeedSection>
            )}

            {/* Para Fazer */}
            <FeedSection
              title="Para fazer"
              emptyMessage="Nenhuma atividade pendente. Bom trabalho! 🎉"
              forceShow
            >
              {data.paraFazer.map(item => (
                <FeedItemCard key={item.id} item={item} />
              ))}
            </FeedSection>

            {/* Novos Conteúdos */}
            <FeedSection title="Novos conteúdos">
              {data.novosConteudos.map(item => (
                <ConteudoCard key={item.id} item={item} />
              ))}
            </FeedSection>

            {/* Recomendados */}
            <FeedSection title="Recomendados para você">
              {data.recomendados.map(item => (
                <RecomendacaoCard key={item.id} item={item} />
              ))}
            </FeedSection>
          </>
        )}
      </main>

      <BottomNavigation />
    </div>
  )
}
