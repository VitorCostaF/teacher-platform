import type { QuestaoAtividade } from '../../types'

interface Props {
  questao: QuestaoAtividade
  resposta: number | null
  onChange: (valor: number) => void
}

export function QuestaoMultiplaEscolha({ questao, resposta, onChange }: Props) {
  return (
    <div className="flex flex-col gap-2">
      {questao.alternativas?.map((alt, i) => (
        <label
          key={i}
          className={[
            'flex cursor-pointer items-start gap-3 rounded-xl border p-3.5 transition-colors',
            resposta === i
              ? 'border-blue-400 bg-blue-50'
              : 'border-gray-200 bg-white hover:border-blue-200 hover:bg-blue-50/40',
          ].join(' ')}
        >
          <input
            type="radio"
            name={`q_${questao.id}`}
            value={i}
            checked={resposta === i}
            onChange={() => onChange(i)}
            className="mt-0.5 h-4 w-4 shrink-0 text-blue-600 focus:ring-blue-500"
          />
          <span className="text-sm text-gray-800 leading-snug">
            <span className="mr-1.5 font-semibold">{String.fromCharCode(65 + i)})</span>
            {alt}
          </span>
        </label>
      ))}
    </div>
  )
}
