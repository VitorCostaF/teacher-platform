import { useState } from 'react'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts'
import type { AlunoResumo } from '../types'

interface FaixaData {
  faixa: string
  quantidade: number
  alunos: AlunoResumo[]
}

interface Props {
  histograma: FaixaData[]
}

export function HistogramaNotas({ histograma }: Props) {
  const [faixaSelecionada, setFaixaSelecionada] = useState<FaixaData | null>(null)

  function handleClick(data: FaixaData) {
    setFaixaSelecionada(prev => prev?.faixa === data.faixa ? null : data)
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="rounded-xl border border-gray-200 bg-white p-4">
        <ResponsiveContainer width="100%" height={220}>
          <BarChart data={histograma} margin={{ top: 4, right: 8, left: -16, bottom: 4 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis dataKey="faixa" tick={{ fontSize: 12 }} />
            <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
            <Tooltip
              contentStyle={{ borderRadius: 8, border: '1px solid #e5e7eb', fontSize: 12 }}
              formatter={(value: number) => [value, 'alunos']}
            />
            <Bar dataKey="quantidade" radius={[4, 4, 0, 0]} cursor="pointer" onClick={handleClick}>
              {histograma.map((entry) => (
                <Cell
                  key={entry.faixa}
                  fill={faixaSelecionada?.faixa === entry.faixa ? '#1d4ed8' : '#3b82f6'}
                />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>

      {faixaSelecionada && faixaSelecionada.alunos.length > 0 && (
        <div className="rounded-xl border border-blue-200 bg-blue-50 p-4">
          <div className="mb-2 flex items-center justify-between">
            <p className="text-sm font-semibold text-blue-900">
              Alunos na faixa {faixaSelecionada.faixa} ({faixaSelecionada.alunos.length})
            </p>
            <button
              type="button"
              onClick={() => setFaixaSelecionada(null)}
              className="rounded-full p-0.5 text-blue-400 hover:text-blue-600"
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <ul className="flex flex-wrap gap-2">
            {faixaSelecionada.alunos.map(a => (
              <li
                key={a.id}
                className="rounded-lg bg-white px-3 py-1 text-sm text-gray-800 shadow-sm"
              >
                {a.nome}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
