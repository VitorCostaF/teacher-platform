import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ReferenceLine,
  ResponsiveContainer,
} from 'recharts'
import type { DesempenhoSerie } from '../types'

interface Props {
  dados: DesempenhoSerie[]
}

export function DesempenhoPorSerieChart({ dados }: Props) {
  if (dados.length === 0) {
    return (
      <div className="flex h-48 items-center justify-center rounded-xl border border-gray-200 bg-white">
        <p className="text-sm text-gray-400">Sem dados por série</p>
      </div>
    )
  }

  return (
    <div className="rounded-xl border border-gray-200 bg-white p-4">
      <ResponsiveContainer width="100%" height={220}>
        <BarChart data={dados} margin={{ top: 8, right: 8, left: -8, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
          <XAxis dataKey="serie" tick={{ fontSize: 12 }} />
          <YAxis domain={[0, 10]} tick={{ fontSize: 12 }} />
          <Tooltip
            formatter={(value: number) => [value.toFixed(1), 'Média']}
            labelFormatter={(label) => `Série: ${label}`}
            contentStyle={{ borderRadius: 8, border: '1px solid #e5e7eb', fontSize: 12 }}
          />
          <ReferenceLine y={7} stroke="#22c55e" strokeDasharray="4 2" label={{ value: '7.0', fontSize: 10, fill: '#22c55e' }} />
          <ReferenceLine y={5} stroke="#f59e0b" strokeDasharray="4 2" label={{ value: '5.0', fontSize: 10, fill: '#f59e0b' }} />
          <Bar dataKey="media" fill="#3b82f6" radius={[4, 4, 0, 0]} maxBarSize={48} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}
