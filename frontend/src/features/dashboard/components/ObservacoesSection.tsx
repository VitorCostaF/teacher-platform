import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { getCurrentUser } from '@/store/authStore'
import { dashboardService } from '../services/dashboard.service'

interface Observacao {
  texto: string
  criadoEm: string
}

interface Props {
  turmaId: number
  alunoId: string
  observacoes: Observacao[]
}

function formatarData(iso: string): string {
  return new Date(iso).toLocaleDateString('pt-BR', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
  })
}

export function ObservacoesSection({ turmaId, alunoId, observacoes }: Props) {
  const user = getCurrentUser()
  const [texto, setTexto] = useState('')
  const queryClient = useQueryClient()

  const podeCriar = user?.perfil === 'professor' || user?.perfil === 'coordenador' || user?.perfil === 'admin'

  if (!podeCriar && observacoes.length === 0) return null
  if (user?.perfil === 'responsavel') return null

  const { mutate, isPending } = useMutation({
    mutationFn: () => dashboardService.criarObservacao(turmaId, alunoId, texto.trim()),
    onSuccess: () => {
      setTexto('')
      queryClient.invalidateQueries({ queryKey: ['desempenho-aluno', turmaId, alunoId] })
    },
  })

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!texto.trim()) return
    mutate()
  }

  return (
    <div className="flex flex-col gap-3">
      <ul className="flex flex-col gap-2">
        {observacoes.map((obs, i) => (
          <li key={i} className="rounded-xl border border-gray-200 bg-white p-3">
            <p className="text-sm text-gray-800 leading-relaxed">{obs.texto}</p>
            <p className="mt-1 text-xs text-gray-400">{formatarData(obs.criadoEm)}</p>
          </li>
        ))}
        {observacoes.length === 0 && (
          <li className="text-sm text-gray-400 italic">Nenhuma observação registrada.</li>
        )}
      </ul>

      {podeCriar && (
        <form onSubmit={handleSubmit} className="flex flex-col gap-2">
          <textarea
            value={texto}
            onChange={e => setTexto(e.target.value)}
            rows={3}
            placeholder="Adicionar observação..."
            className="w-full resize-none rounded-xl border border-gray-200 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
          <button
            type="submit"
            disabled={!texto.trim() || isPending}
            className="self-end rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {isPending ? 'Salvando...' : 'Salvar observação'}
          </button>
        </form>
      )}
    </div>
  )
}
