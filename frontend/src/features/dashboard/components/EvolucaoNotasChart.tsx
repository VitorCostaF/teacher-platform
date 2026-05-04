import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ReferenceLine, ResponsiveContainer,
} from 'recharts'

interface Props {
  dados: Array<{ avaliacaoNome: string; nota: number }>
}

export function EvolucaoNotasChart({ dados }: Props) {
  return (
    <div className="rounded-xl border border-gray-200 bg-white p-4">
      <ResponsiveContainer width="100%" height={240}>
        <LineChart data={dados} margin={{ top: 8, right: 8, left: -16, bottom: 4 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
          <XAxis
            dataKey="avaliacaoNome"
            tick={{ fontSize: 11 }}
            interval={0}
            angle={-20}
            textAnchor="end"
            height={40}
          />
          <YAxis domain={[0, 10]} tick={{ fontSize: 12 }} />
          <Tooltip
            contentStyle={{ borderRadius: 8, border: '1px solid #e5e7eb', fontSize: 12 }}
            formatter={(value: number, _: string, props: { payload?: { avaliacaoNome: string } }) => [
              value.toFixed(1),
              props.payload?.avaliacaoNome ?? 'Nota',
            ]}
          />
          <ReferenceLine
            y={5}
            stroke="#f59e0b"
            strokeDasharray="4 4"
            label={{ value: 'Mínimo (5.0)', position: 'insideTopRight', fontSize: 11, fill: '#92400e' }}
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
