import { useMemo, useState } from 'react'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer,
} from 'recharts'

interface Ponto {
  avaliacaoNome: string
  nota: number
  disciplina: string
}

interface Props {
  evolucaoNotas: Ponto[]
}

export function EvolucaoNotasAlunoChart({ evolucaoNotas }: Props) {
  const disciplinas = useMemo(() => {
    const unicas = [...new Set(evolucaoNotas.map(p => p.disciplina))]
    return ['Todas', ...unicas]
  }, [evolucaoNotas])

  const [disciplina, setDisciplina] = useState('Todas')

  const dados = useMemo(() =>
    evolucaoNotas.filter(p => disciplina === 'Todas' || p.disciplina === disciplina),
    [evolucaoNotas, disciplina]
  )

  if (evolucaoNotas.length === 0) {
    return (
      <div className="flex h-40 items-center justify-center rounded-xl border border-gray-200 bg-white text-sm text-gray-400">
        Nenhuma nota registrada ainda
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-3 rounded-xl border border-gray-200 bg-white p-4">
      <div className="flex items-center justify-between gap-2">
        <h3 className="text-sm font-semibold text-gray-900">Evolução de Notas</h3>
        <select
          value={disciplina}
          onChange={e => setDisciplina(e.target.value)}
          className="rounded-lg border border-gray-300 px-2 py-1 text-xs text-gray-700 focus:border-blue-500 focus:outline-none"
        >
          {disciplinas.map(d => <option key={d} value={d}>{d}</option>)}
        </select>
      </div>

      <ResponsiveContainer width="100%" height={200}>
        <LineChart data={dados} margin={{ top: 4, right: 8, left: -20, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
          <XAxis
            dataKey="avaliacaoNome"
            tick={{ fontSize: 10, fill: '#9ca3af' }}
            tickLine={false}
            axisLine={false}
          />
          <YAxis
            domain={[0, 10]}
            tick={{ fontSize: 10, fill: '#9ca3af' }}
            tickLine={false}
            axisLine={false}
          />
          <Tooltip
            contentStyle={{ fontSize: 12, borderRadius: 8, border: '1px solid #e5e7eb' }}
            formatter={(value: number) => [value.toFixed(1), 'Nota']}
            labelFormatter={(label: string) => label}
          />
          <Line
            type="monotone"
            dataKey="nota"
            stroke="#3b82f6"
            strokeWidth={2}
            dot={{ r: 4, fill: '#3b82f6' }}
            activeDot={{ r: 6 }}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  )
}
