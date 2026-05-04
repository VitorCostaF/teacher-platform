import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { SugestaoConteudoResponse } from '../types'

interface Props {
  resultado: SugestaoConteudoResponse
}

export function SugestaoConteudoResult({ resultado }: Props) {
  const navigate = useNavigate()
  const [topicosSelecionados, setTopicosSelecionados] = useState<string[]>([])

  function toggleTopico(topico: string) {
    setTopicosSelecionados(prev =>
      prev.includes(topico) ? prev.filter(t => t !== topico) : [...prev, topico]
    )
  }

  const topicosParaNavegar = topicosSelecionados.length > 0 ? topicosSelecionados : resultado.topicos

  function handleUsarParaProva() {
    navigate('/professor/criar/prova', { state: { topicos: topicosParaNavegar } })
  }

  function handleUsarParaAtividade() {
    navigate('/professor/criar/atividade', { state: { topicos: topicosParaNavegar } })
  }

  return (
    <div className="flex flex-col gap-6">
      {/* Competências BNCC */}
      {resultado.competenciasBNCC.length > 0 && (
        <section>
          <h3 className="mb-3 text-sm font-semibold text-gray-900">Competências BNCC</h3>
          <ul className="flex flex-col gap-2">
            {resultado.competenciasBNCC.map((comp, i) => (
              <li key={i} className="flex items-start gap-2 rounded-lg bg-blue-50 p-3">
                <span className="mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-blue-600 text-xs font-bold text-white">
                  {i + 1}
                </span>
                <span className="text-sm text-gray-700">{comp}</span>
              </li>
            ))}
          </ul>
        </section>
      )}

      {/* Tópicos Sugeridos */}
      {resultado.topicos.length > 0 && (
        <section>
          <div className="mb-3 flex items-center justify-between">
            <h3 className="text-sm font-semibold text-gray-900">Tópicos Sugeridos</h3>
            {topicosSelecionados.length > 0 && (
              <span className="text-xs text-gray-500">{topicosSelecionados.length} selecionado(s)</span>
            )}
          </div>
          <div className="flex flex-wrap gap-2">
            {resultado.topicos.map((topico, i) => {
              const selecionado = topicosSelecionados.includes(topico)
              return (
                <button
                  key={i}
                  type="button"
                  onClick={() => toggleTopico(topico)}
                  className={[
                    'rounded-full border px-3 py-1 text-xs font-medium transition-colors',
                    selecionado
                      ? 'border-blue-600 bg-blue-600 text-white'
                      : 'border-gray-300 bg-white text-gray-700 hover:border-blue-400 hover:bg-blue-50',
                  ].join(' ')}
                >
                  {topico}
                </button>
              )
            })}
          </div>
          {topicosSelecionados.length === 0 && (
            <p className="mt-2 text-xs text-gray-400">Clique nos chips para selecionar tópicos específicos</p>
          )}
        </section>
      )}

      {/* Links Curados */}
      {resultado.linksComplementares.length > 0 && (
        <section>
          <h3 className="mb-3 text-sm font-semibold text-gray-900">Links Complementares</h3>
          <ul className="flex flex-col gap-2">
            {resultado.linksComplementares.map((link, i) => (
              <li key={i}>
                <a
                  href={link}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-2 rounded-lg border border-gray-200 bg-white p-3 text-sm text-blue-600 transition-colors hover:border-blue-300 hover:bg-blue-50"
                >
                  <svg className="h-4 w-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                      d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                  </svg>
                  <span className="truncate">{link}</span>
                </a>
              </li>
            ))}
          </ul>
        </section>
      )}

      {/* Ações */}
      <div className="flex flex-wrap gap-3 border-t border-gray-200 pt-4">
        <button
          type="button"
          onClick={handleUsarParaProva}
          className="flex items-center gap-2 rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
              d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
          Usar para gerar prova
        </button>
        <button
          type="button"
          onClick={handleUsarParaAtividade}
          className="flex items-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
              d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
          </svg>
          Usar para gerar atividade
        </button>
      </div>
    </div>
  )
}
