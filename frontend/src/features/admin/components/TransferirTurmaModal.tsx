import { useEffect, useRef, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Button } from '@/components/ui/Button'
import { toastEmitter } from '@/lib/toastEmitter'
import { adminService } from '../services/admin.service'
import type { AlunoAdmin } from '../types'

interface Props {
  aluno: AlunoAdmin | null
  onClose: () => void
}

export function TransferirTurmaModal({ aluno, onClose }: Props) {
  const dialogRef = useRef<HTMLDialogElement>(null)
  const queryClient = useQueryClient()
  const [novaTurmaId, setNovaTurmaId] = useState<number | null>(null)

  const isOpen = aluno !== null

  const { data: turmas = [] } = useQuery({
    queryKey: ['admin', 'turmas'],
    queryFn: adminService.getTurmas,
    enabled: isOpen,
  })

  useEffect(() => {
    const dialog = dialogRef.current
    if (!dialog) return
    if (isOpen) { if (!dialog.open) dialog.showModal() }
    else { if (dialog.open) dialog.close(); setNovaTurmaId(null) }
  }, [isOpen])

  const { mutate, isPending } = useMutation({
    mutationFn: () => adminService.transferirAluno(aluno!.id, novaTurmaId!),
    onSuccess: () => {
      toastEmitter.emit('success', `${aluno!.nome} transferido com sucesso.`)
      queryClient.invalidateQueries({ queryKey: ['admin', 'alunos'] })
      onClose()
    },
  })

  const handleBackdrop = (e: React.MouseEvent<HTMLDialogElement>) => {
    if (e.target === dialogRef.current && !isPending) onClose()
  }

  if (!aluno) return null

  return (
    <dialog
      ref={dialogRef}
      onClick={handleBackdrop}
      aria-modal="true"
      className="w-full max-w-md rounded-2xl border border-gray-200 bg-white p-0 shadow-xl backdrop:bg-black/40 open:flex open:flex-col"
    >
      <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
        <h2 className="text-base font-semibold text-gray-900">Transferir Aluno</h2>
        <button type="button" onClick={onClose} disabled={isPending}
          className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100">
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <div className="flex flex-col gap-4 p-6">
        <p className="text-sm text-gray-600">
          Transferindo <span className="font-semibold">{aluno.nome}</span> para uma nova turma.
        </p>

        <div className="flex flex-col gap-1.5">
          <label className="text-sm font-medium text-gray-700">Nova turma</label>
          <select
            value={novaTurmaId ?? ''}
            onChange={e => setNovaTurmaId(e.target.value ? Number(e.target.value) : null)}
            className="w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm text-gray-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
          >
            <option value="">Selecione uma turma</option>
            {turmas.map(t => (
              <option key={t.id} value={t.id}>{t.nome} — {t.disciplina}</option>
            ))}
          </select>
        </div>

        <div className="rounded-xl border border-yellow-200 bg-yellow-50 p-3">
          <p className="text-xs text-yellow-800">
            O histórico da turma anterior será mantido. As matrículas anteriores serão encerradas.
          </p>
        </div>

        <div className="flex justify-end gap-3">
          <Button variant="secondary" onClick={onClose} disabled={isPending}>Cancelar</Button>
          <Button
            loading={isPending}
            disabled={!novaTurmaId || isPending}
            onClick={() => mutate()}
          >
            Confirmar transferência
          </Button>
        </div>
      </div>
    </dialog>
  )
}
