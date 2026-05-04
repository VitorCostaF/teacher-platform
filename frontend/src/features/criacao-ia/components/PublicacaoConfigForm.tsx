import { useTurmas } from '@/features/turmas/hooks/useTurmas'
import type { PublicarDto } from '../types'

interface Props {
  config: PublicarDto
  onChange: <K extends keyof PublicarDto>(campo: K, valor: PublicarDto[K]) => void
}

const OPCOES_GABARITO: { value: PublicarDto['liberarGabaritoApos']; label: string }[] = [
  { value: 'entrega', label: 'Após entrega' },
  { value: 'encerramento', label: 'Após encerramento' },
  { value: 'manual', label: 'Manual' },
]

const fieldClass =
  'w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500'

export function PublicacaoConfigForm({ config, onChange }: Props) {
  const { data: turmas = [], isLoading } = useTurmas()

  function toggleTurma(id: number) {
    const atual = config.turmasIds
    const novas = atual.includes(id) ? atual.filter(t => t !== id) : [...atual, id]
    onChange('turmasIds', novas)
  }

  return (
    <div className="flex flex-col gap-5">
      <div>
        <h2 className="text-base font-semibold text-gray-900">Configurar publicação</h2>
        <p className="mt-0.5 text-xs text-gray-500">Defina quando e para quem a avaliação ficará disponível</p>
      </div>

      {/* Disponível em */}
      <div className="flex flex-col gap-1.5">
        <label className="text-xs font-medium text-gray-700">
          Disponível em
          <span className="ml-1 font-normal text-gray-400">(data futura → agendada)</span>
        </label>
        <input
          type="datetime-local"
          value={config.disponivelEm}
          onChange={e => onChange('disponivelEm', e.target.value)}
          className={fieldClass}
        />
      </div>

      {/* Encerra em */}
      <div className="flex flex-col gap-1.5">
        <label className="text-xs font-medium text-gray-700">
          Encerra em
          <span className="ml-1 font-normal text-gray-400">(opcional)</span>
        </label>
        <input
          type="datetime-local"
          value={config.encerraEm ?? ''}
          onChange={e => onChange('encerraEm', e.target.value || undefined)}
          className={fieldClass}
        />
      </div>

      {/* Turmas */}
      <div className="flex flex-col gap-2">
        <label className="text-xs font-medium text-gray-700">
          Turmas
          {config.turmasIds.length > 0 && (
            <span className="ml-1.5 font-normal text-gray-400">({config.turmasIds.length} selecionada{config.turmasIds.length > 1 ? 's' : ''})</span>
          )}
        </label>
        {isLoading ? (
          <div className="flex flex-col gap-2">
            {[1, 2, 3].map(i => <div key={i} className="h-8 animate-pulse rounded-lg bg-gray-100" />)}
          </div>
        ) : turmas.length === 0 ? (
          <p className="text-xs text-gray-400">Nenhuma turma encontrada</p>
        ) : (
          <div className="flex flex-col gap-1.5 rounded-lg border border-gray-200 p-3">
            {turmas.map(t => (
              <label key={t.id} className="flex cursor-pointer items-center gap-3 rounded-lg px-1 py-1 hover:bg-gray-50">
                <input
                  type="checkbox"
                  checked={config.turmasIds.includes(t.id)}
                  onChange={() => toggleTurma(t.id)}
                  className="h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
                <span className="text-sm text-gray-700">{t.nome} — {t.disciplina}</span>
              </label>
            ))}
          </div>
        )}
      </div>

      {/* Embaralhamento */}
      <div className="flex flex-col gap-2">
        <label className="text-xs font-medium text-gray-700">Embaralhamento</label>
        <div className="flex flex-col gap-2 rounded-lg border border-gray-200 p-3">
          <label className="flex cursor-pointer items-center gap-3">
            <input
              type="checkbox"
              checked={config.embaralharQuestoes}
              onChange={e => onChange('embaralharQuestoes', e.target.checked)}
              className="h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
            />
            <span className="text-sm text-gray-700">Embaralhar ordem das questões</span>
          </label>
          <label className="flex cursor-pointer items-center gap-3">
            <input
              type="checkbox"
              checked={config.embaralharAlternativas}
              onChange={e => onChange('embaralharAlternativas', e.target.checked)}
              className="h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
            />
            <span className="text-sm text-gray-700">Embaralhar alternativas</span>
          </label>
        </div>
      </div>

      {/* Liberação do gabarito */}
      <div className="flex flex-col gap-1.5">
        <label className="text-xs font-medium text-gray-700">Liberar gabarito</label>
        <select
          value={config.liberarGabaritoApos}
          onChange={e => onChange('liberarGabaritoApos', e.target.value as PublicarDto['liberarGabaritoApos'])}
          className={fieldClass}
        >
          {OPCOES_GABARITO.map(o => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>
      </div>

      {/* Peso */}
      <div className="flex flex-col gap-1.5">
        <label className="text-xs font-medium text-gray-700">
          Peso na nota final
          <span className="ml-1 font-normal text-gray-400">(0.0 – 10.0)</span>
        </label>
        <input
          type="number"
          min={0}
          max={10}
          step={0.5}
          value={config.peso}
          onChange={e => onChange('peso', Math.max(0, Math.min(10, Number(e.target.value))))}
          className={fieldClass}
        />
      </div>
    </div>
  )
}
