import type { FlashcardData } from '../types'

interface Props {
  card: FlashcardData
  isFlipped: boolean
  onFlip: () => void
}

export function Flashcard({ card, isFlipped, onFlip }: Props) {
  return (
    <div
      style={{ perspective: '1000px' }}
      className="w-full max-w-sm mx-auto"
    >
      <div
        role="button"
        tabIndex={0}
        aria-label={isFlipped ? 'Resposta revelada' : 'Clique para revelar resposta'}
        onClick={onFlip}
        onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') onFlip() }}
        style={{
          transformStyle: 'preserve-3d',
          transition: 'transform 0.6s',
          transform: isFlipped ? 'rotateY(180deg)' : 'rotateY(0deg)',
          position: 'relative',
          height: '260px',
          cursor: 'pointer',
        }}
        className="w-full select-none"
      >
        {/* Frente */}
        <div
          style={{ backfaceVisibility: 'hidden' }}
          className="absolute inset-0 flex flex-col items-center justify-center rounded-2xl border border-gray-200 bg-white p-6 shadow-md"
        >
          <span className="mb-4 text-xs font-semibold uppercase tracking-widest text-gray-400">
            {card.topico}
          </span>
          <p className="text-center text-lg font-semibold text-gray-900 leading-snug">
            {card.pergunta}
          </p>
          <span className="mt-6 text-xs text-gray-400">Toque para revelar</span>
        </div>

        {/* Verso */}
        <div
          style={{ backfaceVisibility: 'hidden', transform: 'rotateY(180deg)' }}
          className="absolute inset-0 flex flex-col items-center justify-center rounded-2xl border border-blue-200 bg-blue-50 p-6 shadow-md"
        >
          <span className="mb-4 text-xs font-semibold uppercase tracking-widest text-blue-400">
            Resposta
          </span>
          <p className="text-center text-base text-gray-800 leading-relaxed">
            {card.resposta}
          </p>
        </div>
      </div>
    </div>
  )
}
