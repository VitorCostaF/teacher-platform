import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { toastEmitter } from '@/lib/toastEmitter'
import { useGeradorProvas } from '../hooks/useGeradorProvas'
import { provasService } from '../services/provas.service'
import { ProvaConfigPanel } from '../components/ProvaConfigPanel'
import { ProvaPreviewPanel } from '../components/ProvaPreviewPanel'
import { ProvaActionsBar } from '../components/ProvaActionsBar'
import type { QuestaoGerada } from '../types'

export function GeradorProvasPage() {
  const navigate = useNavigate()
  const [isPublicando, setIsPublicando] = useState(false)

  const {
    config, questoes, isGenerating, isUploading, avisoConteudo, podeGerar,
    atualizarConfig, gerarComIA, regenerarQuestao, editarQuestao,
    removerQuestao, adicionarQuestaoManual, uploadArquivo,
  } = useGeradorProvas()

  async function handleSalvarRascunho() {
    if (questoes.length === 0) {
      toastEmitter.emit('error', 'Gere ou adicione questões antes de salvar.')
      return
    }
    try {
      await provasService.salvarRascunho(config, questoes)
      toastEmitter.emit('success', 'Rascunho salvo com sucesso!')
      navigate(`/professor/turmas/${config.turmaId}`)
    } catch {
      toastEmitter.emit('error', 'Erro ao salvar rascunho.')
    }
  }

  async function handlePublicar() {
    if (questoes.length === 0) {
      toastEmitter.emit('error', 'Gere ou adicione questões antes de publicar.')
      return
    }
    setIsPublicando(true)
    try {
      await provasService.salvarRascunho(config, questoes)
      toastEmitter.emit('success', 'Prova salva! Publique-a na tela de detalhes da turma.')
      navigate(`/professor/turmas/${config.turmaId}`)
    } catch {
      toastEmitter.emit('error', 'Erro ao salvar a prova.')
    } finally {
      setIsPublicando(false)
    }
  }

  function handleDescartar() {
    if (questoes.length > 0) {
      if (!confirm('Deseja descartar as questões geradas?')) return
    }
    navigate(-1)
  }

  return (
    <div className="flex h-screen flex-col bg-gray-50">
      {/* Header */}
      <header className="flex items-center justify-between border-b border-gray-200 bg-white px-6 py-4">
        <div>
          <h1 className="text-lg font-semibold text-gray-900">Gerador de Provas com IA</h1>
          <p className="text-xs text-gray-500">Configure os parâmetros e gere questões automaticamente</p>
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

      {/* Main split layout */}
      <div className="flex flex-1 overflow-hidden">
        {/* Left panel — config */}
        <aside className="flex w-96 shrink-0 flex-col border-r border-gray-200 bg-white p-5 overflow-y-auto">
          <ProvaConfigPanel
            config={config}
            isGenerating={isGenerating}
            isUploading={isUploading}
            avisoConteudo={avisoConteudo}
            podeGerar={podeGerar}
            onChange={atualizarConfig}
            onGerar={gerarComIA}
            onUpload={uploadArquivo}
          />
        </aside>

        {/* Right panel — preview */}
        <main className="flex flex-1 flex-col overflow-hidden p-5">
          <ProvaPreviewPanel
            questoes={questoes as (QuestaoGerada & { _regenerando?: boolean })[]}
            isGenerating={isGenerating}
            onEditar={editarQuestao}
            onRegergar={regenerarQuestao}
            onRemover={removerQuestao}
            onAdicionarManual={adicionarQuestaoManual}
          />
        </main>
      </div>

      {/* Bottom actions */}
      <ProvaActionsBar
        totalQuestoes={questoes.length}
        onPublicar={handlePublicar}
        onSalvarRascunho={handleSalvarRascunho}
        onDescartar={handleDescartar}
        isPublicando={isPublicando}
      />
    </div>
  )
}
