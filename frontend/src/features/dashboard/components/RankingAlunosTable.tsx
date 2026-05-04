import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { DesempenhoTurmaData } from '../types'

type RankingItem = DesempenhoTurmaData['ranking'][number]
type Coluna = 'nome' | 'media' | 'frequencia' | 'tendencia'

function TendenciaIcon({ tendencia }: { tendencia: RankingItem['tendencia'] }) {
  if (tendencia === 'UP') return <span className="font-bold text-green-600">↑</span>
  if (tendencia === 'DOWN') return <span className="font-bold text-red-600">↓</span>
  return <span className="text-gray-400">→</span>
}

interface Props {
  ranking: RankingItem[]
  turmaId: number
}

export function RankingAlunosTable({ ranking, turmaId }: Props) {
  const navigate = useNavigate()
  const [ordem, setOrdem] = useState<{ coluna: Coluna; dir: 'asc' | 'desc' }>({
    coluna: 'media',
    dir: 'desc',
  })

  function toggleOrdem(coluna: Coluna) {
    setOrdem(prev =>
      prev.coluna === coluna
        ? { coluna, dir: prev.dir === 'asc' ? 'desc' : 'asc' }
        : { coluna, dir: 'asc' }
    )
  }

  const sorted = [...ranking].sort((a, b) => {
    let va: string | number
    let vb: string | number
    if (ordem.coluna === 'nome') { va = a.aluno.nome; vb = b.aluno.nome }
    else if (ordem.coluna === 'media') { va = a.media; vb = b.media }
    else if (ordem.coluna === 'frequencia') { va = a.frequencia; vb = b.frequencia }
    else { va = a.tendencia; vb = b.tendencia }
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
          <span className="text-gray-300">{ativo ? (ordem.dir === 'asc' ? '↑' : '↓') : '↕'}</span>
        </span>
      </th>
    )
  }

  return (
    <div className="overflow-x-auto rounded-xl border border-gray-200">
      <table className="w-full text-sm">
        <thead className="bg-gray-50">
          <tr>
            <HeaderCell col="nome" label="Aluno" />
            <HeaderCell col="media" label="Média" />
            <HeaderCell col="frequencia" label="Freq." />
            <HeaderCell col="tendencia" label="Tendência" />
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100 bg-white">
          {sorted.map(item => {
            const emRisco = item.media < 5 || item.frequencia < 75
            return (
              <tr
                key={item.aluno.id}
                onClick={() => navigate(`/professor/turmas/${turmaId}/alunos/${item.aluno.id}/desempenho`)}
                className={['cursor-pointer hover:bg-gray-50', emRisco ? 'bg-red-50' : ''].join(' ')}
              >
                <td className="px-4 py-3 font-medium text-gray-900">{item.aluno.nome}</td>
                <td className={`px-4 py-3 font-semibold ${item.media >= 7 ? 'text-green-600' : item.media >= 5 ? 'text-yellow-600' : 'text-red-600'}`}>
                  {item.media.toFixed(1)}
                </td>
                <td className={`px-4 py-3 ${item.frequencia < 75 ? 'font-semibold text-red-600' : 'text-gray-700'}`}>
                  {item.frequencia.toFixed(0)}%
                </td>
                <td className="px-4 py-3">
                  <TendenciaIcon tendencia={item.tendencia} />
                </td>
              </tr>
            )
          })}
          {sorted.length === 0 && (
            <tr>
              <td colSpan={4} className="px-4 py-8 text-center text-sm text-gray-400">
                Nenhum aluno encontrado.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
