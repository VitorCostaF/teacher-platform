import { Link } from 'react-router-dom'
import type { PainelData } from '../types'

interface PainelCardsResponsavelProps {
  alunoId: string
  data: PainelData
}

export function PainelCardsResponsavel({ alunoId, data }: PainelCardsResponsavelProps) {
  const { mediaGeral, percentualFrequencia, proximaProva, alertasAtivos } = data

  const mediaColor =
    mediaGeral >= 7 ? 'text-green-700' : mediaGeral >= 5 ? 'text-yellow-700' : 'text-red-700'

  const frequenciaColor =
    percentualFrequencia >= 75 ? 'text-green-700' : 'text-red-700'

  return (
    <div className="flex flex-col gap-4">
      {/* Cards */}
      <div className="grid grid-cols-3 gap-3">
        <div className="flex flex-col items-center rounded-xl border border-gray-200 bg-white p-3 shadow-sm">
          <p className="text-xs text-gray-500">Média Geral</p>
          <p className={`mt-1 text-2xl font-bold ${mediaColor}`}>
            {mediaGeral.toFixed(1)}
          </p>
        </div>

        <div className="flex flex-col items-center rounded-xl border border-gray-200 bg-white p-3 shadow-sm">
          <p className="text-xs text-gray-500">Frequência</p>
          <p className={`mt-1 text-2xl font-bold ${frequenciaColor}`}>
            {percentualFrequencia.toFixed(0)}%
          </p>
        </div>

        <div className="flex flex-col items-center rounded-xl border border-gray-200 bg-white p-3 shadow-sm">
          <p className="text-xs text-gray-500">Próxima Prova</p>
          {proximaProva ? (
            <>
              <p className="mt-1 text-center text-xs font-semibold text-gray-800 line-clamp-2">
                {proximaProva.disciplina}
              </p>
              <p className="text-xs text-gray-500">
                {new Date(proximaProva.data).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })}
              </p>
            </>
          ) : (
            <p className="mt-1 text-xs text-gray-400">—</p>
          )}
        </div>
      </div>

      {/* Alertas */}
      {alertasAtivos.length > 0 && (
        <div className="flex flex-col gap-2">
          <p className="text-xs font-semibold text-gray-600">Alertas</p>
          {alertasAtivos.map(alerta => (
            <div
              key={alerta.id}
              className="rounded-lg border border-yellow-200 bg-yellow-50 px-3 py-2 text-sm text-yellow-800"
            >
              {alerta.mensagem}
            </div>
          ))}
        </div>
      )}

      {/* Links rápidos */}
      <div className="flex gap-2">
        <Link
          to={`/responsavel/alunos/${alunoId}/boletim`}
          className="flex-1 rounded-lg border border-blue-200 bg-blue-50 py-2 text-center text-xs font-semibold text-blue-700 hover:bg-blue-100"
        >
          Boletim
        </Link>
        <Link
          to={`/responsavel/alunos/${alunoId}/frequencia`}
          className="flex-1 rounded-lg border border-blue-200 bg-blue-50 py-2 text-center text-xs font-semibold text-blue-700 hover:bg-blue-100"
        >
          Frequência
        </Link>
        <Link
          to={`/responsavel/acompanhamento?tab=calendario`}
          className="flex-1 rounded-lg border border-blue-200 bg-blue-50 py-2 text-center text-xs font-semibold text-blue-700 hover:bg-blue-100"
        >
          Calendário
        </Link>
      </div>
    </div>
  )
}
