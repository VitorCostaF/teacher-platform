import { useQuery } from '@tanstack/react-query'
import { Skeleton } from '@/components/feedback/Skeleton'
import { adminService } from '../services/admin.service'
import { KPICards } from '../components/KPICards'
import { DesempenhoPorSerieChart } from '../components/DesempenhoPorSerieChart'
import { AlertasConsolidadosTable } from '../components/AlertasConsolidadosTable'
import { AtividadeRecenteFeed } from '../components/AtividadeRecenteFeed'

export function AdminVisaoGeralPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['admin', 'dashboard'],
    queryFn: adminService.getDashboard,
    refetchInterval: 60000,
  })

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Visão Geral</h1>
          <p className="text-sm text-gray-500">Indicadores consolidados da escola</p>
        </div>

        {isError && (
          <div className="mb-4 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            Erro ao carregar dados. Tente novamente.
          </div>
        )}

        {isLoading && (
          <div className="flex flex-col gap-6">
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
              {Array.from({ length: 5 }).map((_, i) => (
                <Skeleton key={i} className="h-20 w-full rounded-xl" />
              ))}
            </div>
            <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
              <Skeleton className="h-64 w-full rounded-xl" />
              <Skeleton className="h-64 w-full rounded-xl" />
            </div>
          </div>
        )}

        {data && (
          <div className="flex flex-col gap-6">
            <KPICards data={data} />

            <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
              <section className="flex flex-col gap-3">
                <h2 className="text-sm font-semibold text-gray-700">Desempenho por série</h2>
                <DesempenhoPorSerieChart dados={data.desempenhoPorSerie} />
              </section>

              <section className="flex flex-col gap-3">
                <h2 className="text-sm font-semibold text-gray-700">
                  Alertas
                  {data.alertasConsolidados.length > 0 && (
                    <span className="ml-2 rounded-full bg-red-100 px-2 py-0.5 text-xs font-medium text-red-600">
                      {data.alertasConsolidados.length}
                    </span>
                  )}
                </h2>
                <AlertasConsolidadosTable alertas={data.alertasConsolidados} />
              </section>
            </div>

            {data.atividadeRecente.length > 0 && (
              <section className="rounded-xl border border-gray-200 bg-white p-5">
                <h2 className="mb-4 text-sm font-semibold text-gray-700">Atividade recente</h2>
                <AtividadeRecenteFeed atividades={data.atividadeRecente} />
              </section>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
