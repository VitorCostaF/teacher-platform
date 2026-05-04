import { useEffect, useRef, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Button } from '@/components/ui/Button'
import { toastEmitter } from '@/lib/toastEmitter'
import { adminService } from '../services/admin.service'
import type { ProfessorAdmin } from '../types'

interface Props {
  professor: ProfessorAdmin | null
  onClose: () => void
}

export function ProfessorDetalhesModal({ professor, onClose }: Props) {
  const dialogRef = useRef<HTMLDialogElement>(null)
  const queryClient = useQueryClient()
  const [showDesativar, setShowDesativar] = useState(false)
  const [motivo, setMotivo] = useState('')

  const isOpen = professor !== null

  useEffect(() => {
    const dialog = dialogRef.current
    if (!dialog) return
    if (isOpen) { if (!dialog.open) dialog.showModal() }
    else { if (dialog.open) dialog.close(); setShowDesativar(false); setMotivo('') }
  }, [isOpen])

  const { mutate, isPending } = useMutation({
    mutationFn: ({ ativo, motivo }: { ativo: boolean; motivo: string }) =>
      adminService.alterarStatusProfessor(professor!.id, { ativo, motivo }),
    onSuccess: (_, vars) => {
      const acao = vars.ativo ? 'ativado' : 'desativado'
      toastEmitter.emit('success', `Professor ${acao} com sucesso.`)
      queryClient.invalidateQueries({ queryKey: ['admin', 'professores'] })
      onClose()
    },
  })

  const handleBackdrop = (e: React.MouseEvent<HTMLDialogElement>) => {
    if (e.target === dialogRef.current && !isPending) onClose()
  }

  if (!professor) return null

  return (
    <dialog
      ref={dialogRef}
      onClick={handleBackdrop}
      aria-modal="true"
      className="w-full max-w-md rounded-2xl border border-gray-200 bg-white p-0 shadow-xl backdrop:bg-black/40 open:flex open:flex-col"
    >
      <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
        <h2 className="text-base font-semibold text-gray-900">Detalhes do Professor</h2>
        <button type="button" onClick={onClose} disabled={isPending}
          className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100">
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <div className="flex flex-col gap-4 p-6">
        <div className="flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-purple-100 text-purple-700 text-lg font-bold">
            {professor.nome.charAt(0).toUpperCase()}
          </div>
          <div>
            <p className="font-semibold text-gray-900">{professor.nome}</p>
            <p className="text-sm text-gray-500">{professor.email}</p>
          </div>
          <span className={`ml-auto rounded-full px-2.5 py-0.5 text-xs font-medium ${professor.ativo ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
            {professor.ativo ? 'Ativo' : 'Inativo'}
          </span>
        </div>

        {showDesativar && (
          <div className="flex flex-col gap-3 rounded-xl border border-orange-200 bg-orange-50 p-4">
            <p className="text-sm font-medium text-orange-800">
              Confirmar desativação de {professor.nome}
            </p>
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-medium text-orange-700">Motivo (obrigatório)</label>
              <textarea
                value={motivo}
                onChange={e => setMotivo(e.target.value)}
                rows={3}
                placeholder="Descreva o motivo da desativação..."
                className="w-full resize-none rounded-lg border border-orange-200 bg-white px-3 py-2 text-sm focus:border-orange-400 focus:outline-none"
              />
            </div>
            <div className="flex gap-2">
              <Button variant="secondary" onClick={() => { setShowDesativar(false); setMotivo('') }} disabled={isPending}>
                Cancelar
              </Button>
              <Button
                variant="destructive"
                loading={isPending}
                disabled={!motivo.trim() || isPending}
                onClick={() => mutate({ ativo: false, motivo: motivo.trim() })}
              >
                Desativar
              </Button>
            </div>
          </div>
        )}

        {!showDesativar && (
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="secondary" onClick={onClose}>Fechar</Button>
            {professor.ativo ? (
              <Button variant="destructive" onClick={() => setShowDesativar(true)}>
                Desativar
              </Button>
            ) : (
              <Button
                loading={isPending}
                onClick={() => mutate({ ativo: true, motivo: 'Reativação pelo administrador' })}
              >
                Reativar
              </Button>
            )}
          </div>
        )}
      </div>
    </dialog>
  )
}
