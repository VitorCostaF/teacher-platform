import { useState } from 'react'
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  ReferenceLine,
} from 'recharts'
import type { BoletimDisciplina } from '../types'

interface BoletimTableProps {
  boletim: BoletimDisciplina[]
  periodo: string
  onPeriodoChange: (p: string) => void
}

const PERIODOS = [
  { value: 'anual', label: 'Ano letivo' },
  { value: '1B', label: '1º Bimestre' },
  { value: '2B', label: '2º Bimestre' },
  { value: '3B', label: '3º Bimestre' },
  { value: '4B', label: '4º Bimestre' },
]

const BIMESTRES = ['B1', 'B2', 'B3', 'B4']

function situacaoColor(situacao: string) {
  if (situacao === 'Aprovado') return 'text-green-700'
  if (situacao === 'Reprovado') return 'text-red-700'
  return 'text-yellow-700'
}

function formatNota(n: number | null) {
  if (n === null) return '—'
  return n.toFixed(1)
}

function buildChartData(disciplina: BoletimDisciplina) {
  return BIMESTRES.map((b, i) => ({
    name: b,
    nota: disciplina.notas[i] ?? null,
  })).filter(d => d.nota !== null)
}

export function BoletimTable({ boletim, periodo, onPeriodoChange }: BoletimTableProps) {
  const [graficoAberto, setGraficoAberto] = useState<string | null>(null)

  return (
    <div className="flex flex-col gap-4">
      {/* Select período */}
      <div className="flex items-center gap-2">
        <label htmlFor="periodo-select" className="text-sm font-medium text-gray-700">
          Período:
        </label>
        <select
          id="periodo-select"
          value={periodo}
          onChange={e => onPeriodoChange(e.target.value)}
          className="rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm text-gray-900 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        >
          {PERIODOS.map(p => (
            <option key={p.value} value={p.value}>
              {p.label}
            </option>
          ))}
        </select>
      </div>

      {/* Tabela */}
      <div className="overflow-x-auto rounded-xl border border-gray-200 bg-white shadow-sm">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-200 bg-gray-50">
              <th className="px-3 py-2.5 text-left text-xs font-semibold text-gray-600">Disciplina</th>
              {BIMESTRES.map(b => (
                <th key={b} className="px-2 py-2.5 text-center text-xs font-semibold text-gray-600">
                  {b}
                </th>
              ))}
              <th className="px-2 py-2.5 text-center text-xs font-semibold text-gray-600">Média</th>
              <th className="px-2 py-2.5 text-center text-xs font-semibold text-gray-600">Sit.</th>
              <th className="px-2 py-2.5 text-center text-xs font-semibold text-gray-600">Graf.</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {boletim.map(row => (
              <>
                <tr key={row.disciplina} className="hover:bg-gray-50">
                  <td className="px-3 py-2.5 font-medium text-gray-900">{row.disciplina}</td>
                  {row.notas.map((nota, i) => (
                    <td key={i} className="px-2 py-2.5 text-center text-gray-700">
                      {formatNota(nota)}
                    </td>
                  ))}
                  <td className="px-2 py-2.5 text-center font-semibold text-gray-900">
                    {formatNota(row.mediaFinal)}
                  </td>
                  <td className={`px-2 py-2.5 text-center text-xs font-semibold ${situacaoColor(row.situacao)}`}>
                    {row.situacao}
                  </td>
                  <td className="px-2 py-2.5 text-center">
                    <button
                      type="button"
                      aria-label={`Ver gráfico de ${row.disciplina}`}
                      onClick={() =>
                        setGraficoAberto(g => (g === row.disciplina ? null : row.disciplina))
                      }
                      className="rounded px-1.5 py-0.5 text-xs text-blue-600 hover:bg-blue-50"
                    >
                      {graficoAberto === row.disciplina ? '▲' : '▼'}
                    </button>
                  </td>
                </tr>

                {graficoAberto === row.disciplina && (
                  <tr key={`${row.disciplina}-chart`}>
                    <td colSpan={8} className="px-4 py-3">
                      <ResponsiveContainer width="100%" height={140}>
                        <LineChart data={buildChartData(row)}>
                          <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                          <YAxis domain={[0, 10]} tick={{ fontSize: 11 }} />
                          <Tooltip />
                          <ReferenceLine y={5} stroke="#ef4444" strokeDasharray="3 3" />
                          <Line
                            type="monotone"
                            dataKey="nota"
                            stroke="#2563eb"
                            strokeWidth={2}
                            dot={{ r: 4 }}
                          />
                        </LineChart>
                      </ResponsiveContainer>
                    </td>
                  </tr>
                )}
              </>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
