import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { toastEmitter } from '@/lib/toastEmitter'
import { iaService } from '../services/ia.service'
import { AvaliacaoPreviewAluno } from '../components/AvaliacaoPreviewAluno'
import { PublicacaoConfigForm } from '../components/PublicacaoConfigForm'
import type { PublicarDto } from '../types'

function dataLocal() {
  const d = new Date()
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset())
  return d.toISOString().slice(0, 16)
}

const configInicial: PublicarDto = {
  disponivelEm: dataLocal(),
  encerraEm: undefined,
  turmasIds: [],
  embaralharQuestoes: false,
  embaralharAlternativas: false,
  liberarGabaritoApos: 'encerramento',
  peso: 10,
}

export function RevisaoPublicacaoPage() {
  const { id } = useParams<{ id: string }>()
  const provaId = Number(id)
  const navigate = useNavigate()

  const [config, setConfig] = useState<PublicarDto>(configInicial)
  const [isPublicando, setIsPublicando] = useState(false)
  const [isSalvando, setIsSalvando] = useState(false)

  const { data: preview, isLoading, isError } = useQuery({
    queryKey: ['prova-preview', provaId],
    queryFn: () => iaService.getPreview(provaId),
    enabled: !!provaId,
  })

  function handleChange<K extends keyof PublicarDto>(campo: K, valor: PublicarDto[K]) {
    setConfig(prev => ({ ...prev, [campo]: valor }))
  }

  function validar(): string | null {
    if (!config.disponivelEm) return 'Informe a data de disponibilidade'
    if (config.turmasIds.length === 0) return 'Selecione ao menos uma turma'
    return null
  }

  async function handlePublicar() {
    const erro = validar()
    if (erro) { toastEmitter.emit('error', erro); return }
    setIsPublicando(true)
    try {
      await iaService.publicar(provaId, config)
      toastEmitter.emit('success', 'Prova publicada com sucesso!')
      navigate('/professor/turmas')
    } catch {
      toastEmitter.emit('error', 'Erro ao publicar. Tente novamente.')
    } finally {
      setIsPublicando(false)
    }
  }

  async function handleSalvarRascunho() {
    setIsSalvando(true)
    try {
      toastEmitter.emit('success', 'Rascunho salvo!')
    } catch {
      toastEmitter.emit('error', 'Erro ao salvar rascunho.')
    } finally {
      setIsSalvando(false)
    }
  }

  return (
    <div className="flex h-screen flex-col bg-gray-50">
      {/* Header */}
      <header className="flex items-center justify-between border-b border-gray-200 bg-white px-6 py-4">
        <div>
          <h1 className="text-lg font-semibold text-gray-900">Revisão e Publicação</h1>
          <p className="text-xs text-gray-500">Revise a avaliação e configure as opções de publicação</p>
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
        {/* Left — preview (55%) */}
        <main className="flex flex-1 flex-col overflow-y-auto p-5" style={{ flex: '0 0 55%' }}>
          {isLoading && (
            <div className="flex flex-col gap-4 rounded-xl border border-gray-200 bg-white p-5">
              <div className="h-6 w-48 animate-pulse rounded bg-gray-100" />
              <div className="h-4 w-32 animate-pulse rounded bg-gray-100" />
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="h-20 animate-pulse rounded-lg bg-gray-100" />
              ))}
            </div>
          )}
          {isError && (
            <div className="flex flex-1 items-center justify-center">
              <p className="text-sm text-red-600">Erro ao carregar prévia. Verifique se o rascunho existe.</p>
            </div>
          )}
          {preview && <AvaliacaoPreviewAluno preview={preview} />}
        </main>

        {/* Right — config form (45%) */}
        <aside
          className="flex flex-col overflow-y-auto border-l border-gray-200 bg-white p-5"
          style={{ flex: '0 0 45%' }}
        >
          <PublicacaoConfigForm config={config} onChange={handleChange} />
        </aside>
      </div>

      {/* Footer */}
      <div className="flex items-center justify-between border-t border-gray-200 bg-white px-6 py-3">
        <button
          type="button"
          onClick={handleSalvarRascunho}
          disabled={isSalvando || isPublicando}
          className="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-40"
        >
          {isSalvando ? 'Salvando...' : 'Salvar como rascunho'}
        </button>

        <button
          type="button"
          onClick={handlePublicar}
          disabled={isPublicando || isSalvando || !preview}
          className="flex items-center gap-2 rounded-lg bg-blue-600 px-5 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isPublicando ? (
            <>
              <svg className="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
              </svg>
              Publicando...
            </>
          ) : (
            <>
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              Publicar
            </>
          )}
        </button>
      </div>
    </div>
  )
}
