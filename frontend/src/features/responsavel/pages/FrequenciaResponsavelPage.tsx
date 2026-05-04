import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Skeleton } from '@/components/feedback/Skeleton'
import { responsavelService } from '../services/responsavel.service'
import { FrequenciaCalendario } from '../components/FrequenciaCalendario'

export function FrequenciaResponsavelPage() {
  const { alunoId } = useParams<{ alunoId: string }>()
  const navigate = useNavigate()

  const { data, isLoading, isError } = useQuery({
    queryKey: ['responsavel', 'frequencia', alunoId],
    queryFn: () => responsavelService.getFrequencia(alunoId!),
    enabled: !!alunoId,
  })

  return (
    <div className="flex min-h-screen flex-col bg-gray-50 pb-6">
      <header className="flex items-center gap-3 border-b border-gray-200 bg-white px-4 py-4">
        <button
          type="button"
          onClick={() => navigate(-1)}
          aria-label="Voltar"
          className="rounded p-1 text-gray-500 hover:bg-gray-100"
        >
          ←
        </button>
        <div>
          <h1 className="text-base font-bold text-gray-900">Frequência</h1>
          <p className="text-xs text-gray-500">Presença por disciplina</p>
        </div>
      </header>

      <main className="flex flex-1 flex-col gap-4 px-4 py-4">
        {isLoading && <Skeleton className="h-64 w-full rounded-xl" />}

        {isError && (
          <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            Erro ao carregar frequência. Tente novamente.
          </div>
        )}

        {data && <FrequenciaCalendario frequencias={data} />}
      </main>
    </div>
  )
}
