import type { AlunoTurma } from '../types'

interface AlunoListItemProps {
  aluno: AlunoTurma
  onRemover: (alunoId: string) => void
}

function AvatarFallback({ nome }: { nome: string }) {
  const initials = nome
    .split(' ')
    .slice(0, 2)
    .map(n => n[0])
    .join('')
    .toUpperCase()
  return (
    <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-blue-100 text-sm font-semibold text-blue-700">
      {initials}
    </div>
  )
}

export function AlunoListItem({ aluno, onRemover }: AlunoListItemProps) {
  return (
    <li className="flex items-center justify-between gap-4 py-3">
      <div className="flex items-center gap-3 min-w-0">
        {aluno.avatarUrl ? (
          <img
            src={aluno.avatarUrl}
            alt={aluno.nome}
            className="h-10 w-10 shrink-0 rounded-full object-cover"
          />
        ) : (
          <AvatarFallback nome={aluno.nome} />
        )}
        <div className="min-w-0">
          <p className="truncate text-sm font-medium text-gray-900">{aluno.nome}</p>
          <p className="truncate text-sm text-gray-500">{aluno.email}</p>
        </div>
      </div>

      <button
        onClick={() => onRemover(aluno.id)}
        className="shrink-0 rounded-lg p-2 text-gray-400 transition-colors hover:bg-red-50 hover:text-red-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-500"
        aria-label={`Remover ${aluno.nome}`}
      >
        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
        </svg>
      </button>
    </li>
  )
}
