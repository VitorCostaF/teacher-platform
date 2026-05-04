import { useState, useRef } from 'react'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  Legend, ResponsiveContainer,
} from 'recharts'
import type { MediaHistorica } from '../types'

const CORES = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899']
type Periodo = 'mes' | 'bimestre' | 'semestre' | 'ano'

function extrairTurmas(dados: MediaHistorica[]): string[] {
  const nomes = new Set<string>()
  dados.forEach(d => Object.keys(d.turmas).forEach(t => nomes.add(t)))
  return [...nomes]
}

interface Props {
  dados: MediaHistorica[]
}

export function MediasHistoricasChart({ dados }: Props) {
  const [periodo, setPeriodo] = useState<Periodo>('bimestre')
  const containerRef = useRef<HTMLDivElement>(null)

  const turmas = extrairTurmas(dados)

  const chartData = dados.map(d => ({ periodo: d.periodo, ...d.turmas }))

  async function exportarPNG() {
    if (!containerRef.current) return
    const { default: html2canvas } = await import('html2canvas')
    const canvas = await html2canvas(containerRef.current, { backgroundColor: '#fff' })
    const link = document.createElement('a')
    link.download = 'medias-historicas.png'
    link.href = canvas.toDataURL('image/png')
    link.click()
  }

  const periodos: { value: Periodo; label: string }[] = [
    { value: 'mes', label: 'Mês' },
    { value: 'bimestre', label: 'Bimestre' },
    { value: 'semestre', label: 'Semestre' },
    { value: 'ano', label: 'Ano' },
  ]

  return (
    <div className="flex flex-col gap-3">
      <div className="flex items-center justify-between gap-2">
        <div className="flex gap-1 rounded-lg border border-gray-200 bg-gray-50 p-1">
          {periodos.map(p => (
            <button
              key={p.value}
              type="button"
              onClick={() => setPeriodo(p.value)}
              className={[
                'rounded-md px-3 py-1 text-xs font-medium transition-colors',
                periodo === p.value
                  ? 'bg-white text-gray-900 shadow-sm'
                  : 'text-gray-500 hover:text-gray-700',
              ].join(' ')}
            >
              {p.label}
            </button>
          ))}
        </div>
        <button
          type="button"
          onClick={exportarPNG}
          className="flex items-center gap-1.5 rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50"
        >
          <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
          </svg>
          Exportar PNG
        </button>
      </div>

      <div ref={containerRef} className="rounded-xl border border-gray-200 bg-white p-4">
        <ResponsiveContainer width="100%" height={280}>
          <LineChart data={chartData} margin={{ top: 4, right: 16, left: -16, bottom: 4 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis dataKey="periodo" tick={{ fontSize: 12 }} />
            <YAxis domain={[0, 10]} tick={{ fontSize: 12 }} />
            <Tooltip
              contentStyle={{ borderRadius: 8, border: '1px solid #e5e7eb', fontSize: 12 }}
              formatter={(value: number, name: string) => [value.toFixed(1), name]}
            />
            <Legend wrapperStyle={{ fontSize: 12 }} />
            {turmas.map((turma, i) => (
              <Line
                key={turma}
                type="monotone"
                dataKey={turma}
                stroke={CORES[i % CORES.length]}
                strokeWidth={2}
                dot={{ r: 3 }}
                activeDot={{ r: 5 }}
              />
            ))}
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}
