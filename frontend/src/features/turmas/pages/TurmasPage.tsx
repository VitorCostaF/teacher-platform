import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { getCurrentUser } from '@/store/authStore'
import { TurmaCardSkeleton } from '../components/TurmaCardSkeleton'
import { TurmaGrid } from '../components/TurmaGrid'
import { usePeriodosLetivos, useTurmas } from '../hooks/useTurmas'

export function TurmasPage() {
  const [periodoId, setPeriodoId] = useState<number | undefined>(undefined)
  const [busca, setBusca] = useState('')

  const { data: periodos = [] } = usePeriodosLetivos()
  const { data: turmas = [], isLoading } = useTurmas(periodoId)

  const isAdmin = getCurrentUser()?.perfil === 'admin' || getCurrentUser()?.perfil === 'coordenador'

  useEffect(() => {
    if (periodos.length > 0 && periodoId === undefined) {
      const ativo = periodos.find(p => p.ativo)
      if (ativo) setPeriodoId(ativo.id)
    }
  }, [periodos, periodoId])

  const turmasFiltradas = busca.trim()
    ? turmas.filter(t => t.nome.toLowerCase().includes(busca.toLowerCase()))
    : turmas

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="mb-6 flex items-center justify-between">
          <h1 className="text-2xl font-bold text-gray-900">Minhas Turmas</h1>
          {isAdmin && (
            <Button onClick={() => {/* TODO: abrir modal ou navegar para criação */}}>
              Nova Turma
            </Button>
          )}
        </div>

        <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center">
          {periodos.length > 0 && (
            <select
              value={periodoId ?? ''}
              onChange={e => setPeriodoId(e.target.value ? Number(e.target.value) : undefined)}
              className="rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 outline-none transition-colors focus:border-blue-500 focus:ring-2 focus:ring-blue-100 sm:w-56"
              aria-label="Período letivo"
            >
              <option value="">Todos os períodos</option>
              {periodos.map(p => (
                <option key={p.id} value={p.id}>
                  {p.nome}
                </option>
              ))}
            </select>
          )}

          <div className="flex-1 sm:max-w-xs">
            <Input
              placeholder="Buscar turma..."
              value={busca}
              onChange={e => setBusca(e.target.value)}
              aria-label="Buscar turma"
            />
          </div>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {Array.from({ length: 6 }).map((_, i) => (
              <TurmaCardSkeleton key={i} />
            ))}
          </div>
        ) : busca.trim() && turmasFiltradas.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <p className="text-sm text-gray-500">Nenhuma turma encontrada para "{busca}"</p>
          </div>
        ) : (
          <TurmaGrid turmas={turmasFiltradas} />
        )}
      </div>
    </div>
  )
}
