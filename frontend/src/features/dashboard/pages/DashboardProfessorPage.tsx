import { Skeleton } from '@/components/feedback/Skeleton'
import { useDashboardProfessor } from '../hooks/useDashboardProfessor'
import { AlertasSection } from '../components/AlertasSection'
import { TurmasDashboardTable } from '../components/TurmasDashboardTable'
import { MediasHistoricasChart } from '../components/MediasHistoricasChart'
import { MapaCalorChart } from '../components/MapaCalorChart'

function SectionSkeleton() {
  return <Skeleton className="h-48 w-full rounded-xl" />
}

export function DashboardProfessorPage() {
  const { data, isLoading, isError } = useDashboardProfessor()

  const semDados =
    !isLoading &&
    data &&
    data.turmas.length === 0 &&
    data.alertas.length === 0 &&
    data.mediasHistoricas.length === 0

  return (
    <div className="flex flex-col gap-6 px-4 py-6 sm:px-6">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-sm text-gray-500">Visão geral das suas turmas</p>
      </div>

      {isError && (
        <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          Erro ao carregar dados do dashboard. Tente recarregar a página.
        </div>
      )}

      {semDados && (
        <div className="flex flex-col items-center gap-3 rounded-2xl border border-gray-200 bg-white px-6 py-12 text-center">
          <span className="text-4xl">📊</span>
          <p className="text-base font-semibold text-gray-900">Nenhum dado disponível ainda</p>
          <p className="max-w-sm text-sm text-gray-500">
            Publique atividades e provas para começar a visualizar métricas das suas turmas.
          </p>
        </div>
      )}

      {/* Alertas */}
      <section className="flex flex-col gap-3">
        <h2 className="text-sm font-semibold text-gray-700">Pendências e alertas</h2>
        {isLoading ? (
          <SectionSkeleton />
        ) : (
          <AlertasSection alertas={data?.alertas ?? []} isLoading={false} />
        )}
      </section>

      {/* Turmas */}
      <section className="flex flex-col gap-3">
        <h2 className="text-sm font-semibold text-gray-700">Turmas</h2>
        {isLoading ? (
          <SectionSkeleton />
        ) : (
          <TurmasDashboardTable turmas={data?.turmas ?? []} />
        )}
      </section>

      {/* Gráfico de médias */}
      {(isLoading || (data?.mediasHistoricas && data.mediasHistoricas.length > 0)) && (
        <section className="flex flex-col gap-3">
          <h2 className="text-sm font-semibold text-gray-700">Evolução das médias</h2>
          {isLoading ? (
            <SectionSkeleton />
          ) : (
            <MediasHistoricasChart dados={data!.mediasHistoricas} />
          )}
        </section>
      )}

      {/* Mapa de calor */}
      {(isLoading || (data?.mapaCalor && data.mapaCalor.length > 0)) && (
        <section className="flex flex-col gap-3">
          <h2 className="text-sm font-semibold text-gray-700">Mapa de calor — erros por tópico</h2>
          {isLoading ? (
            <SectionSkeleton />
          ) : (
            <MapaCalorChart dados={data!.mapaCalor} />
          )}
        </section>
      )}
    </div>
  )
}
