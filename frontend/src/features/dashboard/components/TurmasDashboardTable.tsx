import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { TurmaDashboard } from '../types'

type Coluna = 'nome' | 'media' | 'percentualFrequencia' | 'alunosEmAlerta' | 'ultimaAtividade'

function formatarData(iso: string | null): string {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })
}

function mediaCor(media: number): string {
  if (media >= 7) return 'text-green-600 font-semibold'
  if (media >= 5) return 'text-yellow-600 font-semibold'
  return 'text-red-600 font-semibold'
}

interface Props {
  turmas: TurmaDashboard[]
}

export function TurmasDashboardTable({ turmas }: Props) {
  const navigate = useNavigate()
  const [ordem, setOrdem] = useState<{ coluna: Coluna; dir: 'asc' | 'desc' }>({
    coluna: 'nome',
    dir: 'asc',
  })

  function toggleOrdem(coluna: Coluna) {
    setOrdem(prev =>
      prev.coluna === coluna
        ? { coluna, dir: prev.dir === 'asc' ? 'desc' : 'asc' }
        : { coluna, dir: 'asc' }
    )
  }

  const sorted = [...turmas].sort((a, b) => {
    const va = a[ordem.coluna] ?? ''
    const vb = b[ordem.coluna] ?? ''
    const cmp = va < vb ? -1 : va > vb ? 1 : 0
    return ordem.dir === 'asc' ? cmp : -cmp
  })

  function HeaderCell({ col, label }: { col: Coluna; label: string }) {
    const ativo = ordem.coluna === col
    return (
      <th
        onClick={() => toggleOrdem(col)}
        className="cursor-pointer select-none whitespace-nowrap px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500 hover:text-gray-700"
      >
        <span className="flex items-center gap-1">
          {label}
          <span className="text-gray-300">
            {ativo ? (ordem.dir === 'asc' ? '↑' : '↓') : '↕'}
          </span>
        </span>
      </th>
    )
  }

  return (
    <div className="overflow-x-auto rounded-xl border border-gray-200">
      <table className="w-full text-sm">
        <thead className="bg-gray-50">
          <tr>
            <HeaderCell col="nome" label="Turma" />
            <HeaderCell col="media" label="Média" />
            <HeaderCell col="percentualFrequencia" label="% Freq." />
            <HeaderCell col="alunosEmAlerta" label="Em alerta" />
            <HeaderCell col="ultimaAtividade" label="Últ. atividade" />
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100 bg-white">
          {sorted.map(turma => (
            <tr
              key={turma.id}
              onClick={() => navigate(`/professor/turmas/${turma.id}`)}
              className="cursor-pointer hover:bg-gray-50"
            >
              <td className="px-4 py-3 font-medium text-gray-900">{turma.nome}</td>
              <td className={`px-4 py-3 ${mediaCor(turma.media)}`}>
                {turma.media.toFixed(1)}
              </td>
              <td className="px-4 py-3 text-gray-700">{turma.percentualFrequencia.toFixed(0)}%</td>
              <td className="px-4 py-3">
                {turma.alunosEmAlerta > 0 ? (
                  <span className="inline-flex items-center gap-1 rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-semibold text-red-700">
                    {turma.alunosEmAlerta} aluno{turma.alunosEmAlerta !== 1 ? 's' : ''}
                  </span>
                ) : (
                  <span className="text-gray-400">—</span>
                )}
              </td>
              <td className="px-4 py-3 text-gray-500">{formatarData(turma.ultimaAtividade)}</td>
            </tr>
          ))}
          {sorted.length === 0 && (
            <tr>
              <td colSpan={5} className="px-4 py-8 text-center text-sm text-gray-400">
                Nenhuma turma encontrada.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
