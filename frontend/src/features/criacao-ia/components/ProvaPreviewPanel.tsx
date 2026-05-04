import type { QuestaoGerada } from '../types'
import { QuestaoCard } from './QuestaoCard'

interface Props {
  questoes: (QuestaoGerada & { _regenerando?: boolean })[]
  isGenerating: boolean
  onEditar: (id: string, changes: Partial<QuestaoGerada>) => void
  onRegergar: (id: string) => void
  onRemover: (id: string) => void
  onAdicionarManual: () => void
}

export function ProvaPreviewPanel({
  questoes, isGenerating,
  onEditar, onRegergar, onRemover, onAdicionarManual,
}: Props) {
  if (isGenerating) {
    return (
      <div className="flex flex-1 flex-col items-center justify-center gap-4 rounded-2xl border-2 border-dashed border-blue-200 bg-blue-50/40 p-8">
        <svg className="h-10 w-10 animate-spin text-blue-500" viewBox="0 0 24 24" fill="none">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
        </svg>
        <div className="text-center">
          <p className="text-sm font-medium text-blue-700">Gerando questões com IA...</p>
          <p className="mt-1 text-xs text-blue-500">Isso pode levar alguns segundos</p>
        </div>
      </div>
    )
  }

  if (questoes.length === 0) {
    return (
      <div className="flex flex-1 flex-col items-center justify-center gap-3 rounded-2xl border-2 border-dashed border-gray-200 bg-gray-50/50 p-8">
        <svg className="h-12 w-12 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <div className="text-center">
          <p className="text-sm font-medium text-gray-500">Nenhuma questão gerada ainda</p>
          <p className="mt-1 text-xs text-gray-400">Configure a prova ao lado e clique em "Gerar com IA"</p>
        </div>
        <button
          type="button"
          onClick={onAdicionarManual}
          className="mt-2 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm text-gray-600 hover:border-gray-400 hover:bg-gray-50"
        >
          + Adicionar questão manualmente
        </button>
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col gap-3 overflow-y-auto">
      <div className="flex items-center justify-between">
        <span className="text-sm font-medium text-gray-700">
          {questoes.length} {questoes.length === 1 ? 'questão' : 'questões'}
        </span>
        <button
          type="button"
          onClick={onAdicionarManual}
          className="rounded-lg border border-gray-300 px-3 py-1.5 text-xs text-gray-600 hover:border-gray-400 hover:bg-gray-50"
        >
          + Adicionar questão
        </button>
      </div>

      <div className="flex flex-col gap-3">
        {questoes.map((q, i) => (
          <QuestaoCard
            key={q.id}
            questao={q}
            index={i + 1}
            onEditar={onEditar}
            onRegergar={onRegergar}
            onRemover={onRemover}
          />
        ))}
      </div>
    </div>
  )
}
