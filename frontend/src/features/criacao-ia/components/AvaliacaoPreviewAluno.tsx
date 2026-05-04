import { useState } from 'react'
import type { PreviewAvaliacaoResponse } from '../types'

interface Props {
  preview: PreviewAvaliacaoResponse
}

const LABEL_TIPO: Record<string, string> = {
  MULTIPLA_ESCOLHA: 'Múltipla escolha',
  VERDADEIRO_FALSO: 'V / F',
  DISSERTATIVA: 'Dissertativa',
}

export function AvaliacaoPreviewAluno({ preview }: Props) {
  const [modoAluno, setModoAluno] = useState(true)

  return (
    <div className="flex flex-col gap-4">
      {/* Toggle */}
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-gray-700">
          Prévia — {modoAluno ? 'visão do aluno' : 'visão do professor'}
        </p>
        <button
          type="button"
          onClick={() => setModoAluno(v => !v)}
          className="flex items-center gap-2 rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-xs font-medium text-gray-700 hover:bg-gray-50"
        >
          <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
              d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
              d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
          </svg>
          {modoAluno ? 'Ver como professor' : 'Ver como aluno'}
        </button>
      </div>

      {/* Cartão da avaliação */}
      <div className="flex flex-col gap-4 overflow-y-auto rounded-xl border border-gray-200 bg-white p-5">
        {/* Cabeçalho */}
        <div className="border-b border-gray-100 pb-4">
          <h2 className="text-base font-bold text-gray-900">{preview.titulo}</h2>
          <div className="mt-1 flex flex-wrap items-center gap-3 text-xs text-gray-500">
            <span>{preview.disciplina}</span>
            {preview.serie && <><span>·</span><span>{preview.serie}</span></>}
            {preview.duracaoMinutos && (
              <><span>·</span><span>{preview.duracaoMinutos} min</span></>
            )}
            <span>·</span>
            <span>{preview.questoes.length} questões</span>
          </div>
          {modoAluno && (
            <div className="mt-3 flex gap-6 text-xs text-gray-500">
              <span>Nome: _______________________________</span>
              <span>Data: _______________</span>
            </div>
          )}
        </div>

        {/* Questões */}
        <div className="flex flex-col gap-5">
          {preview.questoes.map((q, i) => (
            <div key={q.id} className="flex flex-col gap-2">
              {/* Header da questão */}
              <div className="flex items-center gap-2">
                <span className="flex h-5 w-5 items-center justify-center rounded-full bg-blue-600 text-xs font-bold text-white">
                  {i + 1}
                </span>
                <span className="rounded-full bg-gray-100 px-2 py-0.5 text-xs text-gray-600">
                  {LABEL_TIPO[q.tipo] ?? q.tipo}
                </span>
                {!modoAluno && q.topico && (
                  <span className="text-xs text-gray-400">{q.topico}</span>
                )}
              </div>

              {/* Enunciado */}
              <p className="text-sm leading-relaxed text-gray-800">{q.enunciado}</p>

              {/* Alternativas — Múltipla escolha */}
              {q.tipo === 'MULTIPLA_ESCOLHA' && q.alternativas && (
                <div className="flex flex-col gap-1.5 pl-1">
                  {q.alternativas.map((alt, ai) => {
                    const isGabarito = !modoAluno && q.gabarito === ai
                    return (
                      <div
                        key={ai}
                        className={[
                          'flex items-center gap-2 rounded-lg px-3 py-1.5 text-sm',
                          isGabarito
                            ? 'bg-green-50 font-medium text-green-800'
                            : 'bg-gray-50 text-gray-700',
                        ].join(' ')}
                      >
                        {modoAluno ? (
                          <span className="flex h-4 w-4 shrink-0 items-center justify-center rounded-full border border-gray-300 text-xs" />
                        ) : (
                          <span className="shrink-0 text-xs font-bold">
                            {String.fromCharCode(65 + ai)})
                          </span>
                        )}
                        {alt}
                        {isGabarito && (
                          <svg className="ml-auto h-3.5 w-3.5 shrink-0 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                            <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                          </svg>
                        )}
                      </div>
                    )
                  })}
                </div>
              )}

              {/* Verdadeiro/Falso */}
              {q.tipo === 'VERDADEIRO_FALSO' && (
                <div className="flex gap-2 pl-1">
                  {['Verdadeiro', 'Falso'].map((op, oi) => {
                    const isGabarito = !modoAluno && q.gabarito === oi
                    return (
                      <span
                        key={op}
                        className={[
                          'rounded-lg px-3 py-1.5 text-sm',
                          isGabarito ? 'bg-green-50 font-medium text-green-800' : 'bg-gray-50 text-gray-600',
                        ].join(' ')}
                      >
                        {modoAluno && (
                          <span className="mr-2 inline-flex h-4 w-4 items-center justify-center rounded-full border border-gray-300" />
                        )}
                        {op}
                        {isGabarito && ' ✓'}
                      </span>
                    )
                  })}
                </div>
              )}

              {/* Dissertativa */}
              {q.tipo === 'DISSERTATIVA' && (
                <div className="pl-1">
                  {modoAluno ? (
                    <div className="flex flex-col gap-1.5">
                      {Array.from({ length: 4 }).map((_, li) => (
                        <div key={li} className="h-6 border-b border-gray-300" />
                      ))}
                    </div>
                  ) : q.criteriosCorrecao ? (
                    <div className="rounded-lg bg-blue-50 px-3 py-2 text-xs text-blue-700">
                      <strong>Critérios:</strong> {q.criteriosCorrecao}
                    </div>
                  ) : null}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
