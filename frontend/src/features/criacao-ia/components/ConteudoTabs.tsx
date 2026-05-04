import { useCallback, useRef, useState } from 'react'
import type { FonteConteudo } from '../types'

interface Props {
  fonte: FonteConteudo
  conteudoTexto: string
  topicos: string[]
  isUploading: boolean
  avisoConteudo: string | null
  onFonteChange: (f: FonteConteudo) => void
  onConteudoChange: (v: string) => void
  onTopicosChange: (t: string[]) => void
  onUpload: (file: File) => void
}

const TABS: { id: FonteConteudo; label: string }[] = [
  { id: 'texto', label: 'Colar texto' },
  { id: 'upload', label: 'Upload arquivo' },
  { id: 'topicos', label: 'Tópicos livres' },
]

const TIPOS_ACEITOS = '.pdf,.doc,.docx'
const MAX_MB = 10

export function ConteudoTabs({
  fonte, conteudoTexto, topicos, isUploading, avisoConteudo,
  onFonteChange, onConteudoChange, onTopicosChange, onUpload,
}: Props) {
  const [isDragging, setIsDragging] = useState(false)
  const [uploadProgress, setUploadProgress] = useState(0)
  const [novoTopico, setNovoTopico] = useState('')
  const fileRef = useRef<HTMLInputElement>(null)

  const handleFile = useCallback((file: File | null) => {
    if (!file) return
    if (file.size > MAX_MB * 1024 * 1024) {
      alert(`O arquivo deve ter no máximo ${MAX_MB}MB.`)
      return
    }
    setUploadProgress(0)
    let p = 0
    const interval = setInterval(() => {
      p += 20
      setUploadProgress(Math.min(p, 90))
      if (p >= 90) clearInterval(interval)
    }, 200)
    onUpload(file)
    setTimeout(() => { clearInterval(interval); setUploadProgress(100) }, 1200)
  }, [onUpload])

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault()
    setIsDragging(false)
    handleFile(e.dataTransfer.files[0] ?? null)
  }, [handleFile])

  const adicionarTopico = useCallback(() => {
    const t = novoTopico.trim()
    if (t && !topicos.includes(t)) {
      onTopicosChange([...topicos, t])
    }
    setNovoTopico('')
  }, [novoTopico, topicos, onTopicosChange])

  return (
    <div className="flex flex-col gap-3">
      <div className="flex gap-1 rounded-lg bg-gray-100 p-1">
        {TABS.map(tab => (
          <button
            key={tab.id}
            onClick={() => onFonteChange(tab.id)}
            className={[
              'flex-1 rounded-md px-3 py-1.5 text-xs font-medium transition-colors',
              fonte === tab.id
                ? 'bg-white text-gray-900 shadow-sm'
                : 'text-gray-500 hover:text-gray-700',
            ].join(' ')}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {fonte === 'texto' && (
        <textarea
          value={conteudoTexto}
          onChange={e => onConteudoChange(e.target.value)}
          placeholder="Cole aqui o conteúdo que será usado para gerar as questões..."
          rows={8}
          className="w-full resize-none rounded-lg border border-gray-300 px-3 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        />
      )}

      {fonte === 'upload' && (
        <div className="flex flex-col gap-2">
          <div
            onDragOver={e => { e.preventDefault(); setIsDragging(true) }}
            onDragLeave={() => setIsDragging(false)}
            onDrop={handleDrop}
            onClick={() => fileRef.current?.click()}
            className={[
              'flex cursor-pointer flex-col items-center justify-center gap-2 rounded-lg border-2 border-dashed px-4 py-8 transition-colors',
              isDragging ? 'border-blue-400 bg-blue-50' : 'border-gray-300 hover:border-gray-400 hover:bg-gray-50',
            ].join(' ')}
          >
            <svg className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                d="M9 13h6m-3-3v6m5 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <p className="text-sm text-gray-600">
              {isUploading ? 'Processando...' : 'Arraste um arquivo ou clique para selecionar'}
            </p>
            <p className="text-xs text-gray-400">PDF ou DOCX, até {MAX_MB}MB</p>
            <input
              ref={fileRef}
              type="file"
              accept={TIPOS_ACEITOS}
              className="hidden"
              onChange={e => handleFile(e.target.files?.[0] ?? null)}
            />
          </div>

          {(isUploading || uploadProgress > 0) && (
            <div className="h-1.5 w-full overflow-hidden rounded-full bg-gray-200">
              <div
                className="h-full rounded-full bg-blue-500 transition-all duration-300"
                style={{ width: `${uploadProgress}%` }}
              />
            </div>
          )}

          {avisoConteudo && (
            <div className="flex items-start gap-2 rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-700">
              <svg className="mt-0.5 h-4 w-4 shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
              </svg>
              {avisoConteudo}
            </div>
          )}

          {conteudoTexto && (
            <p className="text-xs text-gray-500">
              ✓ Conteúdo extraído ({conteudoTexto.split(' ').length} palavras)
            </p>
          )}
        </div>
      )}

      {fonte === 'topicos' && (
        <div className="flex flex-col gap-3">
          <div className="flex gap-2">
            <input
              value={novoTopico}
              onChange={e => setNovoTopico(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && (e.preventDefault(), adicionarTopico())}
              placeholder="Digite um tópico e pressione Enter"
              className="flex-1 rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
            <button
              onClick={adicionarTopico}
              disabled={!novoTopico.trim()}
              className="rounded-lg bg-blue-600 px-3 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-40"
            >
              +
            </button>
          </div>
          <div className="flex flex-wrap gap-2">
            {topicos.map(t => (
              <span
                key={t}
                className="inline-flex items-center gap-1 rounded-full bg-blue-100 px-3 py-1 text-sm text-blue-800"
              >
                {t}
                <button
                  onClick={() => onTopicosChange(topicos.filter(x => x !== t))}
                  className="ml-1 text-blue-600 hover:text-blue-900"
                >
                  ×
                </button>
              </span>
            ))}
            {topicos.length === 0 && (
              <p className="text-sm text-gray-400">Nenhum tópico adicionado</p>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
