import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Input } from '@/components/ui/Input'
import { Skeleton } from '@/components/feedback/Skeleton'
import { adminService } from '../services/admin.service'
import { ProfessorDetalhesModal } from './ProfessorDetalhesModal'
import type { ProfessorAdmin } from '../types'

interface Props {
  onImportar: () => void
  onConvidar: () => void
}

export function ProfessoresTable({ onImportar, onConvidar }: Props) {
  const [busca, setBusca] = useState('')
  const [filtroAtivo, setFiltroAtivo] = useState<boolean | undefined>(undefined)
  const [pagina, setPagina] = useState(0)
  const [selecionado, setSelecionado] = useState<ProfessorAdmin | null>(null)

  const { data, isLoading } = useQuery({
    queryKey: ['admin', 'professores', busca, filtroAtivo, pagina],
    queryFn: () => adminService.listarProfessores({
      nome: busca || undefined,
      ativo: filtroAtivo,
      page: pagina,
      size: 20,
    }),
  })

  const content = data?.content ?? []
  const totalPaginas = data?.totalPages ?? 0

  return (
    <>
      <div className="flex flex-col gap-3">
        <div className="flex flex-wrap items-center gap-3">
          <div className="w-64">
            <Input
              placeholder="Buscar por nome..."
              value={busca}
              onChange={e => { setBusca(e.target.value); setPagina(0) }}
            />
          </div>
          <div className="flex gap-2">
            {([
              { label: 'Todos', value: undefined },
              { label: 'Ativos', value: true },
              { label: 'Inativos', value: false },
            ] as const).map(opt => (
              <button
                key={String(opt.value)}
                type="button"
                onClick={() => { setFiltroAtivo(opt.value); setPagina(0) }}
                className={[
                  'rounded-full px-3 py-1 text-xs font-medium transition-colors',
                  filtroAtivo === opt.value
                    ? 'bg-gray-900 text-white'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200',
                ].join(' ')}
              >
                {opt.label}
              </button>
            ))}
          </div>
        </div>

        <div className="overflow-hidden rounded-xl border border-gray-200 bg-white">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50">
                <th className="px-4 py-3 text-left font-medium text-gray-600">Nome</th>
                <th className="px-4 py-3 text-left font-medium text-gray-600">E-mail</th>
                <th className="px-4 py-3 text-center font-medium text-gray-600">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {isLoading && Array.from({ length: 5 }).map((_, i) => (
                <tr key={i}>
                  <td className="px-4 py-3"><Skeleton className="h-4 w-40 rounded" /></td>
                  <td className="px-4 py-3"><Skeleton className="h-4 w-48 rounded" /></td>
                  <td className="px-4 py-3 text-center"><Skeleton className="mx-auto h-5 w-16 rounded-full" /></td>
                </tr>
              ))}
              {!isLoading && content.length === 0 && (
                <tr>
                  <td colSpan={3} className="px-4 py-8 text-center text-gray-400">
                    Nenhum professor encontrado.
                  </td>
                </tr>
              )}
              {!isLoading && content.map(p => (
                <tr
                  key={p.id}
                  onClick={() => setSelecionado(p)}
                  className="cursor-pointer hover:bg-gray-50 transition-colors"
                >
                  <td className="px-4 py-3 font-medium text-gray-900">{p.nome}</td>
                  <td className="px-4 py-3 text-gray-500">{p.email}</td>
                  <td className="px-4 py-3 text-center">
                    <span className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${p.ativo ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                      {p.ativo ? 'Ativo' : 'Inativo'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {totalPaginas > 1 && (
          <div className="flex items-center justify-between">
            <p className="text-xs text-gray-500">
              {data?.totalElements ?? 0} professores
            </p>
            <div className="flex items-center gap-2">
              <button type="button" onClick={() => setPagina(p => Math.max(0, p - 1))} disabled={pagina === 0}
                className="rounded-lg border border-gray-200 px-2.5 py-1 text-xs text-gray-600 hover:bg-gray-50 disabled:opacity-40">
                ←
              </button>
              <span className="text-xs text-gray-500">{pagina + 1} / {totalPaginas}</span>
              <button type="button" onClick={() => setPagina(p => Math.min(totalPaginas - 1, p + 1))} disabled={pagina >= totalPaginas - 1}
                className="rounded-lg border border-gray-200 px-2.5 py-1 text-xs text-gray-600 hover:bg-gray-50 disabled:opacity-40">
                →
              </button>
            </div>
          </div>
        )}
      </div>

      <ProfessorDetalhesModal professor={selecionado} onClose={() => setSelecionado(null)} />
    </>
  )
}
