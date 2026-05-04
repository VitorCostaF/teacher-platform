import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { iaService } from '../services/ia.service'
import { SugestaoConteudoResult } from '../components/SugestaoConteudoResult'
import { toastEmitter } from '@/lib/toastEmitter'
import type { SugestaoConteudoResponse } from '../types'

const fieldClass =
  'w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500'

const SERIES = [
  '1º ano', '2º ano', '3º ano', '4º ano', '5º ano',
  '6º ano', '7º ano', '8º ano', '9º ano',
  '1ª série EM', '2ª série EM', '3ª série EM',
]

const BIMESTRES = ['1º Bimestre', '2º Bimestre', '3º Bimestre', '4º Bimestre', '1º Trimestre', '2º Trimestre', '3º Trimestre']

const DISCIPLINAS = [
  'Matemática', 'Português', 'História', 'Geografia', 'Ciências',
  'Física', 'Química', 'Biologia', 'Arte', 'Educação Física', 'Inglês',
]

export function SugestoesConteudoPage() {
  const navigate = useNavigate()

  const [serie, setSerie] = useState('')
  const [disciplina, setDisciplina] = useState('')
  const [bimestre, setBimestre] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [resultado, setResultado] = useState<SugestaoConteudoResponse | null>(null)

  const podeBuscar = !!serie && !!disciplina && !!bimestre && !isLoading

  async function handleBuscar() {
    if (!podeBuscar) return
    setIsLoading(true)
    setResultado(null)
    try {
      const res = await iaService.getSugestoesConteudo({ serie, disciplina, bimestre })
      setResultado(res)
    } catch {
      toastEmitter.emit('error', 'Erro ao buscar sugestões. Tente novamente.')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="flex h-screen flex-col bg-gray-50">
      {/* Header */}
      <header className="flex items-center justify-between border-b border-gray-200 bg-white px-6 py-4">
        <div>
          <h1 className="text-lg font-semibold text-gray-900">Sugestões de Conteúdos BNCC</h1>
          <p className="text-xs text-gray-500">Obtenha competências, tópicos e links alinhados à BNCC</p>
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
        <aside className="flex w-80 shrink-0 flex-col gap-4 overflow-y-auto border-r border-gray-200 bg-white p-5">
          <div>
            <h2 className="text-base font-semibold text-gray-900">Filtros</h2>
            <p className="mt-0.5 text-xs text-gray-500">Selecione série, disciplina e período</p>
          </div>

          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-medium text-gray-700">Série / Ano</label>
            <select value={serie} onChange={e => setSerie(e.target.value)} className={fieldClass}>
              <option value="">Selecione</option>
              {SERIES.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>

          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-medium text-gray-700">Disciplina</label>
            <select value={disciplina} onChange={e => setDisciplina(e.target.value)} className={fieldClass}>
              <option value="">Selecione</option>
              {DISCIPLINAS.map(d => <option key={d} value={d}>{d}</option>)}
            </select>
          </div>

          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-medium text-gray-700">Bimestre / Trimestre</label>
            <select value={bimestre} onChange={e => setBimestre(e.target.value)} className={fieldClass}>
              <option value="">Selecione</option>
              {BIMESTRES.map(b => <option key={b} value={b}>{b}</option>)}
            </select>
          </div>

          <button
            type="button"
            onClick={handleBuscar}
            disabled={!podeBuscar}
            className="mt-1 flex w-full items-center justify-center gap-2 rounded-xl bg-blue-600 px-4 py-3 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {isLoading ? (
              <>
                <svg className="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                </svg>
                Buscando...
              </>
            ) : (
              <>
                <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
                Buscar Sugestões
              </>
            )}
          </button>
        </aside>

        {/* Right panel */}
        <main className="flex flex-1 flex-col overflow-y-auto p-5">
          {isLoading && (
            <div className="flex flex-col gap-4 rounded-xl border border-gray-200 bg-white p-5">
              <div className="h-5 w-40 animate-pulse rounded bg-gray-100" />
              <div className="flex flex-col gap-2">
                {Array.from({ length: 4 }).map((_, i) => (
                  <div key={i} className="h-10 animate-pulse rounded-lg bg-gray-100" />
                ))}
              </div>
              <div className="h-5 w-32 animate-pulse rounded bg-gray-100" />
              <div className="flex flex-wrap gap-2">
                {Array.from({ length: 8 }).map((_, i) => (
                  <div key={i} className="h-7 w-24 animate-pulse rounded-full bg-gray-100" />
                ))}
              </div>
            </div>
          )}

          {!isLoading && !resultado && (
            <div className="flex flex-1 flex-col items-center justify-center text-center">
              <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-blue-50">
                <svg className="h-8 w-8 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                    d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                </svg>
              </div>
              <p className="text-sm font-medium text-gray-700">Nenhuma sugestão ainda</p>
              <p className="mt-1 text-xs text-gray-500">Selecione série, disciplina e bimestre e clique em "Buscar Sugestões"</p>
            </div>
          )}

          {!isLoading && resultado && (
            <div className="rounded-xl border border-gray-200 bg-white p-5">
              <SugestaoConteudoResult resultado={resultado} />
            </div>
          )}
        </main>
      </div>
    </div>
  )
}
