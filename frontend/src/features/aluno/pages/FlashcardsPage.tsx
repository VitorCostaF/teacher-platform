import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Skeleton } from '@/components/feedback/Skeleton'
import { alunoService } from '../services/aluno.service'
import { Flashcard } from '../components/Flashcard'
import { FlashcardActions } from '../components/FlashcardActions'
import { FlashcardProgressBar } from '../components/FlashcardProgressBar'
import { useFlashcards } from '../hooks/useFlashcards'
import { BottomNavigation } from '../components/BottomNavigation'

const DISCIPLINAS = [
  { id: undefined, label: 'Todas' },
  { id: 1, label: 'Matemática' },
  { id: 2, label: 'Português' },
  { id: 3, label: 'História' },
  { id: 4, label: 'Ciências' },
]

function SessaoEncerrada({ respondidos, total, onReiniciar }: { respondidos: number; total: number; onReiniciar: () => void }) {
  const navigate = useNavigate()
  const pct = total === 0 ? 0 : Math.round((respondidos / total) * 100)

  return (
    <div className="flex flex-1 flex-col items-center justify-center gap-6 px-6 text-center">
      <div className="flex h-20 w-20 items-center justify-center rounded-full bg-green-100 text-4xl">
        🎉
      </div>
      <div>
        <h2 className="text-xl font-bold text-gray-900">Sessão concluída!</h2>
        <p className="mt-1 text-sm text-gray-500">
          Você revisou {respondidos} de {total} cards ({pct}%)
        </p>
      </div>
      <div className="flex flex-col gap-3 w-full max-w-xs">
        <button
          type="button"
          onClick={onReiniciar}
          className="w-full rounded-xl bg-blue-600 py-3 text-sm font-semibold text-white hover:bg-blue-700"
        >
          Estudar novamente
        </button>
        <button
          type="button"
          onClick={() => navigate('/aluno/feed')}
          className="w-full rounded-xl border border-gray-300 bg-white py-3 text-sm font-medium text-gray-700 hover:bg-gray-50"
        >
          Voltar ao feed
        </button>
      </div>
    </div>
  )
}

function FlashcardsSession({ cards }: { cards: import('../types').FlashcardData[] }) {
  const {
    cardAtual, respondidos, total, isAvaliando,
    sessaoEncerrada, isFlipped, setIsFlipped,
    responder, encerrarSessao,
  } = useFlashcards(cards)

  const [, forceRestart] = useState(0)

  if (sessaoEncerrada) {
    return (
      <SessaoEncerrada
        respondidos={respondidos}
        total={total}
        onReiniciar={() => forceRestart(n => n + 1)}
      />
    )
  }

  if (!cardAtual) return null

  return (
    <div className="flex flex-1 flex-col gap-6 px-4 py-4">
      <FlashcardProgressBar respondidos={respondidos} total={total} />

      <div className="flex flex-1 flex-col items-center justify-center gap-6">
        <Flashcard
          card={cardAtual}
          isFlipped={isFlipped}
          onFlip={() => setIsFlipped(f => !f)}
        />

        <div className="w-full max-w-sm">
          <FlashcardActions
            isFlipped={isFlipped}
            onResponder={responder}
            isLoading={isAvaliando}
          />
        </div>
      </div>

      <button
        type="button"
        onClick={encerrarSessao}
        className="mx-auto text-xs text-gray-400 underline hover:text-gray-600"
      >
        Encerrar sessão
      </button>
    </div>
  )
}

export function FlashcardsPage() {
  const [disciplinaId, setDisciplinaId] = useState<number | undefined>(undefined)
  const [sessionKey, setSessionKey] = useState(0)

  const { data: cards = [], isLoading, isError } = useQuery({
    queryKey: ['flashcards', disciplinaId],
    queryFn: () => alunoService.getFlashcards(disciplinaId),
  })

  return (
    <div className="flex min-h-screen flex-col bg-gray-50 pb-20">
      {/* Header */}
      <header className="flex items-center justify-between border-b border-gray-200 bg-white px-4 py-4">
        <h1 className="text-base font-bold text-gray-900">Flashcards</h1>
        <select
          value={disciplinaId ?? ''}
          onChange={e => {
            setDisciplinaId(e.target.value ? Number(e.target.value) : undefined)
            setSessionKey(k => k + 1)
          }}
          className="rounded-lg border border-gray-300 px-3 py-1.5 text-sm text-gray-700 focus:border-blue-500 focus:outline-none"
        >
          {DISCIPLINAS.map(d => (
            <option key={d.label} value={d.id ?? ''}>{d.label}</option>
          ))}
        </select>
      </header>

      {isLoading && (
        <div className="flex flex-1 flex-col gap-4 px-4 py-6">
          <Skeleton className="h-4 w-full" />
          <Skeleton className="mx-auto h-64 w-full max-w-sm rounded-2xl" />
          <div className="flex gap-3">
            <Skeleton className="h-12 flex-1 rounded-xl" />
            <Skeleton className="h-12 flex-1 rounded-xl" />
          </div>
        </div>
      )}

      {isError && (
        <div className="mx-4 mt-6 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          Erro ao carregar flashcards. Tente novamente.
        </div>
      )}

      {!isLoading && !isError && cards.length === 0 && (
        <div className="flex flex-1 flex-col items-center justify-center gap-3 px-6 text-center">
          <span className="text-4xl">📚</span>
          <p className="text-sm font-medium text-gray-700">Nenhum flashcard disponível</p>
          <p className="text-xs text-gray-500">Tente selecionar outra disciplina</p>
        </div>
      )}

      {!isLoading && cards.length > 0 && (
        <FlashcardsSession key={sessionKey} cards={cards} />
      )}

      <BottomNavigation />
    </div>
  )
}
