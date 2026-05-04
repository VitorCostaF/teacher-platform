import { useState } from 'react'
import type { FrequenciaData, FrequenciaCalendarioDia } from '../types'
import { FrequenciaResumo } from './FrequenciaResumo'

interface FrequenciaCalendarioProps {
  frequencias: FrequenciaData[]
}

type StatusDia = FrequenciaCalendarioDia['status']

const STATUS_COLOR: Record<StatusDia, string> = {
  PRESENTE: 'bg-green-400',
  FALTA: 'bg-red-400',
  FALTA_JUSTIFICADA: 'bg-yellow-400',
  SEM_AULA: 'bg-gray-200',
}

const STATUS_LABEL: Record<StatusDia, string> = {
  PRESENTE: 'Presente',
  FALTA: 'Falta',
  FALTA_JUSTIFICADA: 'Just.',
  SEM_AULA: 'Sem aula',
}

const DIAS_SEMANA = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb']

function buildGradeMensal(dias: FrequenciaCalendarioDia[]) {
  if (dias.length === 0) return { semanas: [], ano: 0, mes: 0 }

  const primeiraData = new Date(dias[0].data + 'T00:00:00')
  const ano = primeiraData.getFullYear()
  const mes = primeiraData.getMonth()

  const totalDiasNoMes = new Date(ano, mes + 1, 0).getDate()
  const primeiroDiaSemana = new Date(ano, mes, 1).getDay()

  const mapaStatus: Record<string, StatusDia> = {}
  for (const d of dias) {
    mapaStatus[d.data] = d.status
  }

  // Preencher grade com células vazias no início
  const celulas: Array<{ dia: number | null; status: StatusDia | null; dateStr: string | null }> = []
  for (let i = 0; i < primeiroDiaSemana; i++) {
    celulas.push({ dia: null, status: null, dateStr: null })
  }
  for (let d = 1; d <= totalDiasNoMes; d++) {
    const dateStr = `${ano}-${String(mes + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    celulas.push({ dia: d, status: mapaStatus[dateStr] ?? null, dateStr })
  }

  // Dividir em semanas
  const semanas: typeof celulas[] = []
  for (let i = 0; i < celulas.length; i += 7) {
    semanas.push(celulas.slice(i, i + 7))
  }
  // Completar última semana
  const ultima = semanas[semanas.length - 1]
  while (ultima.length < 7) {
    ultima.push({ dia: null, status: null, dateStr: null })
  }

  return { semanas, ano, mes }
}

export function FrequenciaCalendario({ frequencias }: FrequenciaCalendarioProps) {
  const [disciplinaSelecionada, setDisciplinaSelecionada] = useState(0)

  if (frequencias.length === 0) {
    return (
      <p className="text-center text-sm text-gray-500">Nenhum dado de frequência disponível.</p>
    )
  }

  const freq = frequencias[disciplinaSelecionada]
  const { semanas, ano, mes } = buildGradeMensal(freq.diasCalendario)
  const abaixoDoMinimo = freq.percentual < 75

  const nomeMes = new Date(ano, mes).toLocaleString('pt-BR', { month: 'long', year: 'numeric' })

  return (
    <div className="flex flex-col gap-4">
      {/* Select disciplina */}
      {frequencias.length > 1 && (
        <div className="flex items-center gap-2">
          <label htmlFor="disciplina-freq" className="text-sm font-medium text-gray-700">
            Disciplina:
          </label>
          <select
            id="disciplina-freq"
            value={disciplinaSelecionada}
            onChange={e => setDisciplinaSelecionada(Number(e.target.value))}
            className="rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm text-gray-900 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
          >
            {frequencias.map((f, i) => (
              <option key={f.disciplina} value={i}>
                {f.disciplina}
              </option>
            ))}
          </select>
        </div>
      )}

      {/* Banner de risco */}
      {abaixoDoMinimo && (
        <div className="rounded-lg border border-red-300 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
          Atenção: frequência abaixo de 75%
        </div>
      )}

      {/* Resumo */}
      <FrequenciaResumo
        presencas={freq.presencas}
        totalAulas={freq.totalAulas}
        percentual={freq.percentual}
      />

      {/* Grade mensal */}
      {semanas.length > 0 && (
        <div className="rounded-xl border border-gray-200 bg-white p-3 shadow-sm">
          <p className="mb-2 text-center text-xs font-semibold capitalize text-gray-600">
            {nomeMes}
          </p>
          <div className="grid grid-cols-7 gap-1">
            {DIAS_SEMANA.map(d => (
              <div key={d} className="text-center text-xs font-medium text-gray-400">
                {d}
              </div>
            ))}
            {semanas.map((semana, si) =>
              semana.map((celula, ci) => (
                <div
                  key={`${si}-${ci}`}
                  title={
                    celula.status ? `${celula.dia} — ${STATUS_LABEL[celula.status]}` : undefined
                  }
                  className={[
                    'flex h-7 w-full items-center justify-center rounded text-xs font-medium',
                    celula.dia === null ? '' : celula.status ? STATUS_COLOR[celula.status] : 'bg-gray-100',
                    celula.status === 'PRESENTE' || celula.status === 'FALTA'
                      ? 'text-white'
                      : 'text-gray-700',
                  ].join(' ')}
                >
                  {celula.dia}
                </div>
              ))
            )}
          </div>

          {/* Legenda */}
          <div className="mt-3 flex flex-wrap justify-center gap-3">
            {(Object.entries(STATUS_LABEL) as [StatusDia, string][]).map(([k, v]) => (
              <div key={k} className="flex items-center gap-1">
                <div className={`h-3 w-3 rounded-sm ${STATUS_COLOR[k]}`} />
                <span className="text-xs text-gray-600">{v}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
