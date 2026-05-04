import { useState } from 'react'
import type { QuestaoGerada } from '../types'

interface Props {
  questao: QuestaoGerada & { _regenerando?: boolean }
  index: number
  onEditar: (id: string, changes: Partial<QuestaoGerada>) => void
  onRegergar: (id: string) => void
  onRemover: (id: string) => void
}

const LABEL_TIPO: Record<string, string> = {
  MULTIPLA_ESCOLHA: 'Múltipla escolha',
  VERDADEIRO_FALSO: 'V / F',
  DISSERTATIVA: 'Dissertativa',
}

const LABEL_DIFICULDADE: Record<string, string> = {
  FACIL: 'Fácil',
  MEDIO: 'Médio',
  DIFICIL: 'Difícil',
}

const COR_DIFICULDADE: Record<string, string> = {
  FACIL: 'bg-green-100 text-green-700',
  MEDIO: 'bg-yellow-100 text-yellow-700',
  DIFICIL: 'bg-red-100 text-red-700',
}

export function QuestaoCard({ questao, index, onEditar, onRegergar, onRemover }: Props) {
  const [editando, setEditando] = useState(false)
  const [enunciadoLocal, setEnunciadoLocal] = useState(questao.enunciado)

  function salvarEnunciado() {
    if (enunciadoLocal.trim() !== questao.enunciado) {
      onEditar(questao.id, { enunciado: enunciadoLocal.trim() })
    }
    setEditando(false)
  }

  const difLabel = LABEL_DIFICULDADE[questao.dificuldade] ?? questao.dificuldade
  const difCor = COR_DIFICULDADE[questao.dificuldade] ?? 'bg-gray-100 text-gray-600'

  return (
    <div className={[
      'rounded-xl border bg-white p-4 transition-opacity',
      questao._regenerando ? 'opacity-50' : 'opacity-100',
    ].join(' ')}>
      {/* Header */}
      <div className="flex items-start justify-between gap-2">
        <div className="flex items-center gap-2 flex-wrap">
          <span className="flex h-6 w-6 items-center justify-center rounded-full bg-blue-600 text-xs font-bold text-white">
            {index}
          </span>
          <span className="rounded-full bg-gray-100 px-2 py-0.5 text-xs text-gray-600">
            {LABEL_TIPO[questao.tipo] ?? questao.tipo}
          </span>
          <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${difCor}`}>
            {difLabel}
          </span>
          {questao.topico && (
            <span className="text-xs text-gray-400">{questao.topico}</span>
          )}
        </div>
        <div className="flex shrink-0 gap-1">
          <button
            type="button"
            onClick={() => onRegergar(questao.id)}
            disabled={!!questao._regenerando}
            title="Regenerar questão"
            className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-blue-600 disabled:opacity-40"
          >
            <svg className={['h-4 w-4', questao._regenerando ? 'animate-spin' : ''].join(' ')} fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
          </button>
          <button
            type="button"
            onClick={() => { setEditando(!editando); setEnunciadoLocal(questao.enunciado) }}
            title="Editar enunciado"
            className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-700"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
          </button>
          <button
            type="button"
            onClick={() => onRemover(questao.id)}
            title="Remover questão"
            className="rounded-lg p-1.5 text-gray-400 hover:bg-red-50 hover:text-red-600"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>

      {/* Enunciado */}
      <div className="mt-3">
        {editando ? (
          <div className="flex flex-col gap-2">
            <textarea
              value={enunciadoLocal}
              onChange={e => setEnunciadoLocal(e.target.value)}
              rows={3}
              autoFocus
              className="w-full resize-none rounded-lg border border-blue-400 px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
            <div className="flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setEditando(false)}
                className="rounded-lg px-3 py-1.5 text-xs text-gray-500 hover:bg-gray-100"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={salvarEnunciado}
                className="rounded-lg bg-blue-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-blue-700"
              >
                Salvar
              </button>
            </div>
          </div>
        ) : (
          <p className="text-sm text-gray-800 leading-relaxed">{questao.enunciado || <em className="text-gray-400">Enunciado vazio</em>}</p>
        )}
      </div>

      {/* Alternativas — Múltipla escolha */}
      {questao.tipo === 'MULTIPLA_ESCOLHA' && questao.alternativas && (
        <div className="mt-3 flex flex-col gap-1.5">
          {questao.alternativas.map((alt, i) => (
            <div
              key={i}
              className={[
                'flex items-center gap-2 rounded-lg px-3 py-1.5 text-sm',
                questao.gabarito === i
                  ? 'bg-green-50 text-green-800 font-medium'
                  : 'bg-gray-50 text-gray-700',
              ].join(' ')}
            >
              <span className="shrink-0 text-xs font-bold">
                {String.fromCharCode(65 + i)})
              </span>
              {alt || <em className="text-gray-400 font-normal">Alternativa vazia</em>}
              {questao.gabarito === i && (
                <svg className="ml-auto h-3.5 w-3.5 shrink-0 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Verdadeiro/Falso */}
      {questao.tipo === 'VERDADEIRO_FALSO' && (
        <div className="mt-3 flex gap-2">
          {['Verdadeiro', 'Falso'].map((op, i) => (
            <span
              key={op}
              className={[
                'rounded-lg px-3 py-1.5 text-sm',
                questao.gabarito === i ? 'bg-green-50 font-medium text-green-800' : 'bg-gray-50 text-gray-600',
              ].join(' ')}
            >
              {op}
              {questao.gabarito === i && ' ✓'}
            </span>
          ))}
        </div>
      )}

      {/* Dissertativa — critérios */}
      {questao.tipo === 'DISSERTATIVA' && questao.criteriosCorrecao && (
        <div className="mt-3 rounded-lg bg-blue-50 px-3 py-2 text-xs text-blue-700">
          <strong>Critérios:</strong> {questao.criteriosCorrecao}
        </div>
      )}
    </div>
  )
}
