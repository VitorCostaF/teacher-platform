import { useTurmas } from '@/features/turmas/hooks/useTurmas'
import { ConteudoTabs } from './ConteudoTabs'
import type {
  FonteConteudo, NivelDificuldade, ProvaConfig, TipoEntrega, TipoQuestao, TipoQuestaoAtividade,
} from '../types'

interface Props {
  config: ProvaConfig
  tipoEntrega: TipoEntrega
  prazoEntrega: string
  isGenerating: boolean
  isUploading: boolean
  avisoConteudo: string | null
  podeGerar: boolean
  onChange: <K extends keyof ProvaConfig>(campo: K, valor: ProvaConfig[K]) => void
  onTipoEntregaChange: (v: TipoEntrega) => void
  onPrazoChange: (v: string) => void
  onGerar: () => void
  onUpload: (file: File) => void
}

const DIFICULDADES: { value: NivelDificuldade; label: string }[] = [
  { value: 'FACIL', label: 'Fácil' },
  { value: 'MEDIO', label: 'Médio' },
  { value: 'DIFICIL', label: 'Difícil' },
  { value: 'MISTO', label: 'Misto' },
]

const TIPOS_QUESTAO: { tipo: TipoQuestaoAtividade; label: string }[] = [
  { tipo: 'MULTIPLA_ESCOLHA', label: 'Múltipla escolha' },
  { tipo: 'VERDADEIRO_FALSO', label: 'Verdadeiro / Falso' },
  { tipo: 'DISSERTATIVA', label: 'Dissertativa' },
  { tipo: 'LEITURA_COM_PERGUNTAS', label: 'Leitura com perguntas' },
  { tipo: 'PESQUISA_COM_ROTEIRO', label: 'Pesquisa com roteiro' },
  { tipo: 'PROJETO_COM_ETAPAS', label: 'Projeto com etapas' },
]

const TIPOS_ENTREGA: { value: TipoEntrega; label: string }[] = [
  { value: 'ONLINE', label: 'Online' },
  { value: 'PDF', label: 'PDF para imprimir' },
  { value: 'AMBOS', label: 'Ambos' },
]

const fieldClass =
  'w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500'

export function AtividadeConfigPanel({
  config, tipoEntrega, prazoEntrega, isGenerating, isUploading, avisoConteudo, podeGerar,
  onChange, onTipoEntregaChange, onPrazoChange, onGerar, onUpload,
}: Props) {
  const { data: turmas = [], isLoading: loadingTurmas } = useTurmas()

  function handleTurmaChange(turmaId: number) {
    const turma = turmas.find(t => t.id === turmaId)
    onChange('turmaId', turmaId)
    if (turma) onChange('disciplina', turma.disciplina)
  }

  const totalQuestoes = Object.values(config.quantidades).reduce((s, n) => s + (n ?? 0), 0)

  return (
    <div className="flex flex-col gap-4 overflow-y-auto">
      <div>
        <h2 className="text-base font-semibold text-gray-900">Configurar atividade</h2>
        <p className="mt-0.5 text-xs text-gray-500">Defina os parâmetros e gere a atividade com IA</p>
      </div>

      {/* Turma */}
      <div className="flex flex-col gap-1.5">
        <label className="text-xs font-medium text-gray-700">Turma</label>
        <select
          value={config.turmaId ?? ''}
          onChange={e => handleTurmaChange(Number(e.target.value))}
          disabled={loadingTurmas}
          className={fieldClass}
        >
          <option value="">Selecione uma turma</option>
          {turmas.map(t => (
            <option key={t.id} value={t.id}>{t.nome} — {t.disciplina}</option>
          ))}
        </select>
      </div>

      {/* Título */}
      <div className="flex flex-col gap-1.5">
        <label className="text-xs font-medium text-gray-700">Título da atividade</label>
        <input
          type="text"
          value={config.titulo}
          onChange={e => onChange('titulo', e.target.value)}
          placeholder="Ex: Atividade sobre Revolução Francesa"
          className={fieldClass}
        />
      </div>

      {/* Série */}
      <div className="flex flex-col gap-1.5">
        <label className="text-xs font-medium text-gray-700">Série / Ano</label>
        <input
          type="text"
          value={config.serie}
          onChange={e => onChange('serie', e.target.value)}
          placeholder="Ex: 9º ano"
          className={fieldClass}
        />
      </div>

      {/* Tipo de entrega */}
      <div className="flex flex-col gap-1.5">
        <label className="text-xs font-medium text-gray-700">Tipo de entrega</label>
        <select value={tipoEntrega} onChange={e => onTipoEntregaChange(e.target.value as TipoEntrega)} className={fieldClass}>
          {TIPOS_ENTREGA.map(t => (
            <option key={t.value} value={t.value}>{t.label}</option>
          ))}
        </select>
      </div>

      {/* Prazo */}
      <div className="flex flex-col gap-1.5">
        <label className="text-xs font-medium text-gray-700">Prazo de entrega</label>
        <input
          type="datetime-local"
          value={prazoEntrega}
          onChange={e => onPrazoChange(e.target.value)}
          className={fieldClass}
        />
      </div>

      {/* Dificuldade */}
      <div className="flex flex-col gap-1.5">
        <label className="text-xs font-medium text-gray-700">Nível de dificuldade</label>
        <div className="grid grid-cols-4 gap-1.5">
          {DIFICULDADES.map(d => (
            <button
              key={d.value}
              type="button"
              onClick={() => onChange('dificuldade', d.value)}
              className={[
                'rounded-lg border px-2 py-1.5 text-xs font-medium transition-colors',
                config.dificuldade === d.value
                  ? 'border-blue-600 bg-blue-600 text-white'
                  : 'border-gray-300 bg-white text-gray-700 hover:border-blue-400',
              ].join(' ')}
            >
              {d.label}
            </button>
          ))}
        </div>
      </div>

      {/* Tipos e Quantidades */}
      <div className="flex flex-col gap-2">
        <label className="text-xs font-medium text-gray-700">
          Tipos de questão
          {totalQuestoes > 0 && (
            <span className="ml-1.5 font-normal text-gray-400">({totalQuestoes} no total)</span>
          )}
        </label>
        <div className="flex flex-col gap-2">
          {TIPOS_QUESTAO.map(({ tipo, label }) => {
            const qtd = config.quantidades[tipo as TipoQuestao] ?? 0
            const ativo = qtd > 0
            return (
              <div key={tipo} className="flex items-center gap-3">
                <input
                  type="checkbox"
                  id={`tipo_${tipo}`}
                  checked={ativo}
                  onChange={e => {
                    const novas = { ...config.quantidades }
                    if (e.target.checked) novas[tipo as TipoQuestao] = 3
                    else delete novas[tipo as TipoQuestao]
                    onChange('quantidades', novas)
                  }}
                  className="h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
                <label htmlFor={`tipo_${tipo}`} className="flex-1 text-sm text-gray-700 cursor-pointer">
                  {label}
                </label>
                {ativo && (
                  <input
                    type="number"
                    min={1}
                    max={20}
                    value={qtd}
                    onChange={e => {
                      const n = Math.max(1, Math.min(20, Number(e.target.value)))
                      onChange('quantidades', { ...config.quantidades, [tipo as TipoQuestao]: n })
                    }}
                    className="w-16 rounded-lg border border-gray-300 px-2 py-1 text-center text-sm focus:border-blue-500 focus:outline-none"
                  />
                )}
              </div>
            )
          })}
        </div>
      </div>

      {/* Conteúdo */}
      <div className="flex flex-col gap-1.5">
        <label className="text-xs font-medium text-gray-700">Base de conteúdo</label>
        <ConteudoTabs
          fonte={config.fonte}
          conteudoTexto={config.conteudoTexto}
          topicos={config.topicos}
          isUploading={isUploading}
          avisoConteudo={avisoConteudo}
          onFonteChange={(f: FonteConteudo) => onChange('fonte', f)}
          onConteudoChange={v => onChange('conteudoTexto', v)}
          onTopicosChange={t => onChange('topicos', t)}
          onUpload={onUpload}
        />
      </div>

      {/* Botão Gerar */}
      <button
        type="button"
        onClick={onGerar}
        disabled={!podeGerar}
        className="mt-1 flex w-full items-center justify-center gap-2 rounded-xl bg-blue-600 px-4 py-3 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
      >
        {isGenerating ? (
          <>
            <svg className="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
            </svg>
            Gerando atividade...
          </>
        ) : (
          <>
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.347.347a.25.25 0 01-.348 0l-.347-.347a5 5 0 010-7.072z" />
            </svg>
            Gerar com IA
          </>
        )}
      </button>
    </div>
  )
}
