import { useEffect } from 'react'
import type { AlunoResponsavel } from '../types'

interface AlunoSelectorProps {
  alunos: AlunoResponsavel[]
  alunoId: string
  onChange: (id: string) => void
}

const STORAGE_KEY = 'responsavel_aluno_selecionado'

export function AlunoSelector({ alunos, alunoId, onChange }: AlunoSelectorProps) {
  useEffect(() => {
    const saved = sessionStorage.getItem(STORAGE_KEY)
    if (saved && alunos.some(a => a.id === saved)) {
      onChange(saved)
    } else if (alunos.length > 0) {
      onChange(alunos[0].id)
    }
  }, [alunos]) // eslint-disable-line react-hooks/exhaustive-deps

  if (alunos.length <= 1) return null

  function handleChange(e: React.ChangeEvent<HTMLSelectElement>) {
    const id = e.target.value
    sessionStorage.setItem(STORAGE_KEY, id)
    onChange(id)
  }

  return (
    <div className="flex items-center gap-2">
      <label htmlFor="aluno-select" className="text-sm font-medium text-gray-700">
        Aluno:
      </label>
      <select
        id="aluno-select"
        value={alunoId}
        onChange={handleChange}
        className="rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm text-gray-900 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
      >
        {alunos.map(a => (
          <option key={a.id} value={a.id}>
            {a.nome}
          </option>
        ))}
      </select>
    </div>
  )
}
