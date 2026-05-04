import { useRef, useState } from 'react'
import { alunoService } from '../../services/aluno.service'

const MAX_MB = 5
const TIPOS_ACEITOS = '.pdf,.jpg,.jpeg,.png'

interface Props {
  urlAtual: string
  onChange: (url: string) => void
}

export function QuestaoUploadArquivo({ urlAtual, onChange }: Props) {
  const fileRef = useRef<HTMLInputElement>(null)
  const [uploading, setUploading] = useState(false)
  const [progresso, setProgresso] = useState(0)
  const [erro, setErro] = useState<string | null>(null)
  const [nomeArquivo, setNomeArquivo] = useState<string | null>(null)

  async function handleFile(file: File | null) {
    if (!file) return
    if (file.size > MAX_MB * 1024 * 1024) {
      setErro(`O arquivo deve ter no máximo ${MAX_MB}MB.`)
      return
    }
    setErro(null)
    setUploading(true)
    setProgresso(0)
    setNomeArquivo(file.name)

    let p = 0
    const interval = setInterval(() => {
      p = Math.min(p + 15, 85)
      setProgresso(p)
    }, 200)

    try {
      const { url } = await alunoService.uploadRespostaArquivo(file)
      clearInterval(interval)
      setProgresso(100)
      onChange(url)
    } catch {
      clearInterval(interval)
      setErro('Falha no upload. Tente novamente.')
      setProgresso(0)
    } finally {
      setUploading(false)
    }
  }

  const arquivoAtivo = urlAtual || nomeArquivo

  return (
    <div className="flex flex-col gap-3">
      {arquivoAtivo ? (
        <div className="flex items-center gap-3 rounded-xl border border-green-200 bg-green-50 p-3.5">
          <svg className="h-5 w-5 shrink-0 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span className="min-w-0 flex-1 truncate text-sm text-green-800">
            {nomeArquivo ?? 'Arquivo enviado'}
          </span>
          <button
            type="button"
            onClick={() => { onChange(''); setNomeArquivo(null) }}
            className="shrink-0 text-xs text-green-600 underline hover:text-green-800"
          >
            Trocar
          </button>
        </div>
      ) : (
        <div
          onClick={() => fileRef.current?.click()}
          className="flex cursor-pointer flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed border-gray-300 px-4 py-8 transition-colors hover:border-blue-400 hover:bg-blue-50"
        >
          <svg className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
              d="M9 13h6m-3-3v6m5 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          <p className="text-sm text-gray-600">
            {uploading ? 'Enviando...' : 'Clique para selecionar o arquivo'}
          </p>
          <p className="text-xs text-gray-400">PDF ou imagem, até {MAX_MB}MB</p>
        </div>
      )}

      <input
        ref={fileRef}
        type="file"
        accept={TIPOS_ACEITOS}
        className="hidden"
        onChange={e => handleFile(e.target.files?.[0] ?? null)}
      />

      {uploading && (
        <div className="h-1.5 w-full overflow-hidden rounded-full bg-gray-200">
          <div
            className="h-full rounded-full bg-blue-500 transition-all duration-300"
            style={{ width: `${progresso}%` }}
          />
        </div>
      )}

      {erro && <p className="text-xs text-red-600">{erro}</p>}
    </div>
  )
}
