import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ReferenceLine, ResponsiveContainer, Cell,
} from 'recharts'

interface Props {
  dados: Array<{ mes: string; percentual: number }>
}

export function FrequenciaMensalChart({ dados }: Props) {
  return (
    <div className="rounded-xl border border-gray-200 bg-white p-4">
      <ResponsiveContainer width="100%" height={220}>
        <BarChart data={dados} margin={{ top: 8, right: 8, left: -16, bottom: 4 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
          <XAxis dataKey="mes" tick={{ fontSize: 12 }} />
          <YAxis domain={[0, 100]} tick={{ fontSize: 12 }} tickFormatter={(v) => `${v}%`} />
          <Tooltip
            contentStyle={{ borderRadius: 8, border: '1px solid #e5e7eb', fontSize: 12 }}
            formatter={(value: number) => [`${value.toFixed(0)}%`, 'Frequência']}
          />
          <ReferenceLine
            y={75}
            stroke="#ef4444"
            strokeDasharray="4 4"
            label={{ value: '75%', position: 'insideTopRight', fontSize: 11, fill: '#991b1b' }}
          />
          <Bar dataKey="percentual" radius={[4, 4, 0, 0]}>
            {dados.map((entry) => (
              <Cell
                key={entry.mes}
                fill={entry.percentual >= 75 ? '#10b981' : '#ef4444'}
              />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}
