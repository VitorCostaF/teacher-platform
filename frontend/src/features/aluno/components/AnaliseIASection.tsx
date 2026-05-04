import { useNavigate } from 'react-router-dom'
import type { AnaliseTopico } from '../types'

interface Props {
  analise: AnaliseTopico[]
}

export function AnaliseIASection({ analise }: Props) {
  const navigate = useNavigate()

  const topicosComErro = analise.filter(t => t.questoesErradas > 0)

  return (
    <section className="flex flex-col gap-3">
      <div className="flex items-center gap-2">
        <span className="text-base">🤖</span>
        <h2 className="text-sm font-semibold text-gray-900">Análise personalizada</h2>
      </div>

      {topicosComErro.length === 0 ? (
        <div className="flex flex-col items-center gap-2 rounded-xl border border-green-200 bg-green-50 py-6 text-center">
          <span className="text-3xl">🎉</span>
          <p className="text-sm font-semibold text-green-800">Ótimo trabalho! Você acertou tudo.</p>
        </div>
      ) : (
        <div className="flex flex-col gap-2">
          {topicosComErro.map(t => (
            <div
              key={t.topico}
              className="flex items-center justify-between gap-3 rounded-xl border border-orange-100 bg-orange-50 p-3.5"
            >
              <div className="flex flex-col gap-0.5">
                <p className="text-sm font-medium text-gray-900">{t.topico}</p>
                <p className="text-xs text-gray-600">
                  Você errou{' '}
                  <span className="font-semibold text-red-600">{t.questoesErradas}</span>{' '}
                  de {t.totalQuestoes} questões. Que tal revisar?
                </p>
              </div>
              <button
                type="button"
                onClick={() => navigate('/aluno/flashcards')}
                className="shrink-0 rounded-lg bg-blue-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-blue-700"
              >
                Revisar
              </button>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}
