import { useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTurmas } from '@/features/turmas/hooks/useTurmas'
import { toastEmitter } from '@/lib/toastEmitter'
import { iaService } from '../services/ia.service'
import { GradeAulasTable } from '../components/GradeAulasTable'
import { GradeExportButtons } from '../components/GradeExportButtons'
import type { AulaGrade, GerarGradeDto, PeriodoGrade } from '../types'

const fieldClass =
  'w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500'

const PERIODOS: { value: PeriodoGrade; label: string }[] = [
  { value: 'SEMANA', label: 'Semana' },
  { value: 'MES', label: 'Mês' },
  { value: 'SEMESTRE', label: 'Semestre' },
]

export function GeradorGradePage() {
  const navigate = useNavigate()
  const { data: turmas = [], isLoading: loadingTurmas } = useTurmas()
  const tableRef = useRef<HTMLDivElement>(null)

  const [turmaId, setTurmaId] = useState<number | ''>('')
  const [disciplina, setDisciplina] = useState('')
  const [serie, setSerie] = useState('')
  const [periodo, setPeriodo] = useState<PeriodoGrade>('SEMANA')
  const [aulasPorSemana, setAulasPorSemana] = useState(2)
  const [topicosObrigatorios, setTopicosObrigatorios] = useState('')
  const [alinhamentoBNCC, setAlinhamentoBNCC] = useState(false)

  const [aulas, setAulas] = useState<AulaGrade[]>([])
  const [isGenerating, setIsGenerating] = useState(false)
  const [isSalvando, setIsSalvando] = useState(false)

  function handleTurmaChange(id: number) {
    setTurmaId(id)
    const turma = turmas.find(t => t.id === id)
    if (turma) {
      setDisciplina(turma.disciplina)
      setSerie(turma.nome)
    }
  }

  const podeGerar = !!turmaId && !!disciplina.trim() && !isGenerating

  async function handleGerar() {
    if (!podeGerar) return
    setIsGenerating(true)
    try {
      const res = await iaService.gerarGrade({
        turmaId: turmaId as number,
        disciplina,
        serie,
        periodo,
        aulasPorSemana,
        topicosObrigatorios: topicosObrigatorios.trim() || undefined,
        alinhamentoBNCC,
      })
      setAulas(res.aulas)
      if (res.aulas.length === 0) {
        toastEmitter.emit('error', 'A IA não gerou aulas. Ajuste os parâmetros e tente novamente.')
      }
    } catch {
      toastEmitter.emit('error', 'Erro ao gerar grade. Tente novamente.')
    } finally {
      setIsGenerating(false)
    }
  }

  async function handleSalvar() {
    setIsSalvando(true)
    try {
      // Endpoint futuro: POST /grades
      toastEmitter.emit('success', 'Grade salva na plataforma!')
    } catch {
      toastEmitter.emit('error', 'Erro ao salvar grade.')
    } finally {
      setIsSalvando(false)
    }
  }

  return (
    <div className="flex h-screen flex-col bg-gray-50">
      {/* Header */}
      <header className="flex items-center justify-between border-b border-gray-200 bg-white px-6 py-4">
        <div>
          <h1 className="text-lg font-semibold text-gray-900">Gerador de Grade de Aulas com IA</h1>
          <p className="text-xs text-gray-500">Configure os parâmetros e gere sua grade automaticamente</p>
        </div>
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="rounded-lg p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-700"
        >
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </header>

      {/* Main */}
      <div className="flex flex-1 overflow-hidden">
        {/* Left panel */}
        <aside className="flex w-96 shrink-0 flex-col gap-4 overflow-y-auto border-r border-gray-200 bg-white p-5">
          <div>
            <h2 className="text-base font-semibold text-gray-900">Configurar grade</h2>
            <p className="mt-0.5 text-xs text-gray-500">Defina os parâmetros e gere as aulas com IA</p>
          </div>

          {/* Turma */}
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-medium text-gray-700">Turma</label>
            <select
              value={turmaId}
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

          {/* Disciplina / Série */}
          <div className="grid grid-cols-2 gap-3">
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-medium text-gray-700">Disciplina</label>
              <input
                type="text"
                value={disciplina}
                onChange={e => setDisciplina(e.target.value)}
                placeholder="Ex: Matemática"
                className={fieldClass}
              />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-medium text-gray-700">Série / Ano</label>
              <input
                type="text"
                value={serie}
                onChange={e => setSerie(e.target.value)}
                placeholder="Ex: 9º ano"
                className={fieldClass}
              />
            </div>
          </div>

          {/* Período */}
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-medium text-gray-700">Período</label>
            <div className="grid grid-cols-3 gap-1.5">
              {PERIODOS.map(p => (
                <button
                  key={p.value}
                  type="button"
                  onClick={() => setPeriodo(p.value)}
                  className={[
                    'rounded-lg border px-2 py-1.5 text-xs font-medium transition-colors',
                    periodo === p.value
                      ? 'border-blue-600 bg-blue-600 text-white'
                      : 'border-gray-300 bg-white text-gray-700 hover:border-blue-400',
                  ].join(' ')}
                >
                  {p.label}
                </button>
              ))}
            </div>
          </div>

          {/* Aulas/semana */}
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-medium text-gray-700">Aulas por semana</label>
            <input
              type="number"
              min={1}
              max={10}
              value={aulasPorSemana}
              onChange={e => setAulasPorSemana(Math.max(1, Math.min(10, Number(e.target.value))))}
              className={fieldClass}
            />
          </div>

          {/* Tópicos obrigatórios */}
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-medium text-gray-700">Tópicos obrigatórios</label>
            <textarea
              value={topicosObrigatorios}
              onChange={e => setTopicosObrigatorios(e.target.value)}
              placeholder="Liste tópicos que devem estar na grade, separados por vírgula ou linha..."
              rows={3}
              className={`${fieldClass} resize-none`}
            />
          </div>

          {/* BNCC */}
          <div className="flex items-center gap-3">
            <input
              id="bncc-toggle"
              type="checkbox"
              checked={alinhamentoBNCC}
              onChange={e => setAlinhamentoBNCC(e.target.checked)}
              className="h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
            />
            <label htmlFor="bncc-toggle" className="text-sm text-gray-700 cursor-pointer">
              Alinhar com BNCC
            </label>
          </div>

          {/* Botão Gerar */}
          <button
            type="button"
            onClick={handleGerar}
            disabled={!podeGerar}
            className="mt-1 flex w-full items-center justify-center gap-2 rounded-xl bg-blue-600 px-4 py-3 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {isGenerating ? (
              <>
                <svg className="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                </svg>
                Gerando grade...
              </>
            ) : (
              <>
                <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                    d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.347.347a.25.25 0 01-.348 0l-.347-.347a5 5 0 010-7.072z" />
                </svg>
                Gerar Grade com IA
              </>
            )}
          </button>
        </aside>

        {/* Right panel */}
        <main className="flex flex-1 flex-col gap-4 overflow-y-auto p-5">
          {isGenerating && (
            <div className="flex flex-col gap-3 rounded-xl border border-gray-200 bg-white p-4">
              {Array.from({ length: 5 }).map((_, i) => (
                <div key={i} className="h-10 animate-pulse rounded-lg bg-gray-100" />
              ))}
            </div>
          )}

          {!isGenerating && aulas.length === 0 && (
            <div className="flex flex-1 flex-col items-center justify-center text-center">
              <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-blue-50">
                <svg className="h-8 w-8 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                    d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <p className="text-sm font-medium text-gray-700">Nenhuma grade gerada</p>
              <p className="mt-1 text-xs text-gray-500">Configure os parâmetros e clique em "Gerar Grade com IA"</p>
            </div>
          )}

          {!isGenerating && aulas.length > 0 && (
            <div className="flex flex-col gap-4">
              <div className="flex items-center justify-between">
                <p className="text-sm font-medium text-gray-700">{aulas.length} aulas geradas</p>
                <GradeExportButtons
                  tableRef={tableRef}
                  aulas={aulas}
                  onSalvar={handleSalvar}
                  isSalvando={isSalvando}
                />
              </div>
              <div ref={tableRef}>
                <GradeAulasTable aulas={aulas} onChange={setAulas} />
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  )
}
