import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Skeleton } from '@/components/feedback/Skeleton'
import { responsavelService } from '../services/responsavel.service'
import { AlunoSelector } from '../components/AlunoSelector'
import { PainelCardsResponsavel } from '../components/PainelCardsResponsavel'
import { BoletimTable } from '../components/BoletimTable'
import { FrequenciaCalendario } from '../components/FrequenciaCalendario'
import { CalendarioProvas } from '../components/CalendarioProvas'

type Tab = 'painel' | 'boletim' | 'frequencia' | 'calendario'

const TABS: { id: Tab; label: string }[] = [
  { id: 'painel', label: 'Painel' },
  { id: 'boletim', label: 'Boletim' },
  { id: 'frequencia', label: 'Frequência' },
  { id: 'calendario', label: 'Calendário' },
]

function CardSkeleton() {
  return (
    <div className="flex flex-col gap-3">
      <div className="grid grid-cols-3 gap-3">
        <Skeleton className="h-20" />
        <Skeleton className="h-20" />
        <Skeleton className="h-20" />
      </div>
      <Skeleton className="h-32 w-full rounded-xl" />
    </div>
  )
}

export function PainelResponsavelPage() {
  const [searchParams] = useSearchParams()
  const tabParam = (searchParams.get('tab') ?? 'painel') as Tab
  const [activeTab, setActiveTab] = useState<Tab>(
    TABS.some(t => t.id === tabParam) ? tabParam : 'painel'
  )
  const [alunoId, setAlunoId] = useState('')
  const [periodo, setPeriodo] = useState('anual')

  const { data: alunos, isLoading: loadingAlunos } = useQuery({
    queryKey: ['responsavel', 'alunos'],
    queryFn: responsavelService.getAlunos,
  })

  const { data: painel, isLoading: loadingPainel } = useQuery({
    queryKey: ['responsavel', 'painel', alunoId],
    queryFn: () => responsavelService.getPainel(alunoId),
    enabled: !!alunoId && activeTab === 'painel',
  })

  const { data: boletim, isLoading: loadingBoletim } = useQuery({
    queryKey: ['responsavel', 'boletim', alunoId, periodo],
    queryFn: () => responsavelService.getBoletim(alunoId, periodo),
    enabled: !!alunoId && activeTab === 'boletim',
  })

  const { data: frequencia, isLoading: loadingFrequencia } = useQuery({
    queryKey: ['responsavel', 'frequencia', alunoId],
    queryFn: () => responsavelService.getFrequencia(alunoId),
    enabled: !!alunoId && activeTab === 'frequencia',
  })

  const { data: calendario, isLoading: loadingCalendario } = useQuery({
    queryKey: ['responsavel', 'calendario', alunoId],
    queryFn: () => responsavelService.getCalendario(alunoId),
    enabled: !!alunoId && activeTab === 'calendario',
  })

  return (
    <div className="flex min-h-screen flex-col bg-gray-50 pb-6">
      {/* Header */}
      <header className="border-b border-gray-200 bg-white px-4 py-4">
        <h1 className="text-base font-bold text-gray-900">Acompanhamento</h1>
        <p className="mt-0.5 text-xs text-gray-500">Desempenho e frequência do(a) aluno(a)</p>

        {!loadingAlunos && alunos && (
          <div className="mt-3">
            <AlunoSelector alunos={alunos} alunoId={alunoId} onChange={setAlunoId} />
          </div>
        )}
      </header>

      {/* Tabs */}
      <div className="sticky top-0 z-10 flex border-b border-gray-200 bg-white">
        {TABS.map(tab => (
          <button
            key={tab.id}
            type="button"
            onClick={() => setActiveTab(tab.id)}
            className={[
              'flex-1 py-3 text-xs font-semibold transition-colors',
              activeTab === tab.id
                ? 'border-b-2 border-blue-600 text-blue-600'
                : 'text-gray-500 hover:text-gray-700',
            ].join(' ')}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Conteúdo */}
      <main className="flex flex-1 flex-col gap-5 px-4 py-4">
        {!alunoId && !loadingAlunos && (
          <p className="text-center text-sm text-gray-500">Selecione um aluno para ver os dados.</p>
        )}

        {/* Painel */}
        {activeTab === 'painel' && alunoId && (
          <>
            {loadingPainel && <CardSkeleton />}
            {painel && <PainelCardsResponsavel alunoId={alunoId} data={painel} />}
          </>
        )}

        {/* Boletim */}
        {activeTab === 'boletim' && alunoId && (
          <>
            {loadingBoletim && <Skeleton className="h-64 w-full rounded-xl" />}
            {boletim && (
              <BoletimTable boletim={boletim} periodo={periodo} onPeriodoChange={setPeriodo} />
            )}
          </>
        )}

        {/* Frequência */}
        {activeTab === 'frequencia' && alunoId && (
          <>
            {loadingFrequencia && <Skeleton className="h-64 w-full rounded-xl" />}
            {frequencia && <FrequenciaCalendario frequencias={frequencia} />}
          </>
        )}

        {/* Calendário */}
        {activeTab === 'calendario' && alunoId && (
          <>
            {loadingCalendario && <Skeleton className="h-64 w-full rounded-xl" />}
            {calendario && <CalendarioProvas provas={calendario} />}
          </>
        )}
      </main>
    </div>
  )
}
