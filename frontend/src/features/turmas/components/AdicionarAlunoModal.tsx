import { useEffect, useRef, useState } from 'react'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Skeleton } from '@/components/feedback/Skeleton'
import type { AlunoTurma } from '../types'
import { turmasService } from '../services/turmas.service'

interface AdicionarAlunoModalProps {
  isOpen: boolean
  turmaId: number
  onClose: () => void
  onAdicionado: () => void
}

type Aba = 'buscar' | 'email'

function AvatarFallback({ nome }: { nome: string }) {
  const initials = nome.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase()
  return (
    <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-blue-100 text-xs font-semibold text-blue-700">
      {initials}
    </div>
  )
}

export function AdicionarAlunoModal({ isOpen, turmaId, onClose, onAdicionado }: AdicionarAlunoModalProps) {
  const dialogRef = useRef<HTMLDialogElement>(null)
  const [aba, setAba] = useState<Aba>('buscar')
  const [query, setQuery] = useState('')
  const [resultados, setResultados] = useState<AlunoTurma[]>([])
  const [buscando, setBuscando] = useState(false)
  const [email, setEmail] = useState('')
  const [adicionando, setAdicionando] = useState<string | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
    const dialog = dialogRef.current
    if (!dialog) return
    if (isOpen) { if (!dialog.open) dialog.showModal() }
    else { if (dialog.open) dialog.close(); resetState() }
  }, [isOpen])

  useEffect(() => {
    const dialog = dialogRef.current
    if (!dialog) return
    const handleCancel = (e: Event) => { e.preventDefault(); onClose() }
    dialog.addEventListener('cancel', handleCancel)
    return () => dialog.removeEventListener('cancel', handleCancel)
  }, [onClose])

  // Debounce de busca 300ms
  useEffect(() => {
    if (aba !== 'buscar' || !query.trim()) {
      setResultados([])
      return
    }
    const timer = setTimeout(async () => {
      setBuscando(true)
      try {
        const data = await turmasService.buscarAlunos(query.trim())
        setResultados(data)
      } catch {
        setResultados([])
      } finally {
        setBuscando(false)
      }
    }, 300)
    return () => clearTimeout(timer)
  }, [query, aba])

  const resetState = () => {
    setAba('buscar')
    setQuery('')
    setResultados([])
    setEmail('')
    setErro(null)
    setAdicionando(null)
  }

  const handleAdicionar = async (alunoId?: string, emailVal?: string) => {
    const key = alunoId ?? emailVal ?? ''
    setAdicionando(key)
    setErro(null)
    try {
      await turmasService.adicionarAluno(turmaId, alunoId ? { alunoId } : { email: emailVal })
      onAdicionado()
      onClose()
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Erro ao adicionar aluno')
    } finally {
      setAdicionando(null)
    }
  }

  const handleBackdropClick = (e: React.MouseEvent<HTMLDialogElement>) => {
    if (e.target === dialogRef.current) onClose()
  }

  return (
    <dialog
      ref={dialogRef}
      onClick={handleBackdropClick}
      aria-modal="true"
      aria-labelledby="adicionar-modal-title"
      className="w-full max-w-lg rounded-2xl border border-gray-200 bg-white p-0 shadow-xl backdrop:bg-black/40 open:flex open:flex-col"
    >
      <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
        <h2 id="adicionar-modal-title" className="text-base font-semibold text-gray-900">
          Adicionar aluno
        </h2>
        <button onClick={onClose} className="rounded-lg p-1 text-gray-400 hover:text-gray-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-gray-400" aria-label="Fechar">
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
        </button>
      </div>

      <div className="flex border-b border-gray-100">
        {(['buscar', 'email'] as Aba[]).map(a => (
          <button
            key={a}
            onClick={() => { setAba(a); setErro(null) }}
            className={['flex-1 py-2.5 text-sm font-medium transition-colors border-b-2', aba === a ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700'].join(' ')}
          >
            {a === 'buscar' ? 'Buscar aluno' : 'Adicionar por e-mail'}
          </button>
        ))}
      </div>

      <div className="flex flex-col gap-4 p-6">
        {aba === 'buscar' ? (
          <>
            <Input
              placeholder="Buscar por nome ou e-mail..."
              value={query}
              onChange={e => setQuery(e.target.value)}
              autoFocus
            />
            {buscando ? (
              <div className="space-y-3">
                {[1, 2, 3].map(i => (
                  <div key={i} className="flex items-center gap-3">
                    <Skeleton className="h-8 w-8 rounded-full" />
                    <div className="flex-1 space-y-1.5">
                      <Skeleton className="h-3.5 w-40" />
                      <Skeleton className="h-3 w-32" />
                    </div>
                  </div>
                ))}
              </div>
            ) : resultados.length > 0 ? (
              <ul className="max-h-60 divide-y divide-gray-100 overflow-y-auto">
                {resultados.map(aluno => (
                  <li key={aluno.id} className="flex items-center gap-3 py-2.5">
                    {aluno.avatarUrl ? (
                      <img src={aluno.avatarUrl} alt={aluno.nome} className="h-8 w-8 shrink-0 rounded-full object-cover" />
                    ) : (
                      <AvatarFallback nome={aluno.nome} />
                    )}
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-medium text-gray-900">{aluno.nome}</p>
                      <p className="truncate text-xs text-gray-500">{aluno.email}</p>
                    </div>
                    <Button
                      variant="secondary"
                      onClick={() => handleAdicionar(aluno.id)}
                      loading={adicionando === aluno.id}
                      disabled={adicionando !== null}
                      className="shrink-0 text-xs"
                    >
                      Adicionar
                    </Button>
                  </li>
                ))}
              </ul>
            ) : query.trim() ? (
              <p className="text-center text-sm text-gray-500">Nenhum aluno encontrado</p>
            ) : null}
          </>
        ) : (
          <>
            <Input
              type="email"
              placeholder="email@exemplo.com"
              value={email}
              onChange={e => setEmail(e.target.value)}
              autoFocus
            />
            <Button
              onClick={() => handleAdicionar(undefined, email.trim())}
              loading={adicionando === email.trim()}
              disabled={!email.trim() || adicionando !== null}
            >
              Adicionar aluno
            </Button>
          </>
        )}

        {erro && (
          <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{erro}</p>
        )}
      </div>
    </dialog>
  )
}
