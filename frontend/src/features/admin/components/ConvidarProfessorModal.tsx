import { useEffect, useRef, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { toastEmitter } from '@/lib/toastEmitter'
import { adminService } from '../services/admin.service'

const schema = z.object({
  nome: z.string().min(2, 'Nome obrigatório'),
  email: z.string().email('E-mail inválido'),
  disciplinas: z.string().min(1, 'Informe ao menos uma disciplina'),
})

type FormData = z.infer<typeof schema>

interface Props {
  isOpen: boolean
  onClose: () => void
}

export function ConvidarProfessorModal({ isOpen, onClose }: Props) {
  const dialogRef = useRef<HTMLDialogElement>(null)
  const queryClient = useQueryClient()

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  useEffect(() => {
    const dialog = dialogRef.current
    if (!dialog) return
    if (isOpen) { if (!dialog.open) dialog.showModal() }
    else { if (dialog.open) dialog.close(); reset() }
  }, [isOpen, reset])

  const { mutate, isPending } = useMutation({
    mutationFn: (data: FormData) =>
      adminService.convidarProfessor({
        nome: data.nome,
        email: data.email,
        disciplinas: data.disciplinas.split(',').map(d => d.trim()).filter(Boolean),
      }),
    onSuccess: (_, vars) => {
      toastEmitter.emit('success', `Convite enviado para ${vars.email}`)
      queryClient.invalidateQueries({ queryKey: ['admin', 'professores'] })
      onClose()
    },
  })

  const handleBackdrop = (e: React.MouseEvent<HTMLDialogElement>) => {
    if (e.target === dialogRef.current && !isPending) onClose()
  }

  return (
    <dialog
      ref={dialogRef}
      onClick={handleBackdrop}
      aria-modal="true"
      className="w-full max-w-md rounded-2xl border border-gray-200 bg-white p-0 shadow-xl backdrop:bg-black/40 open:flex open:flex-col"
    >
      <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
        <h2 className="text-base font-semibold text-gray-900">Convidar Professor</h2>
        <button type="button" onClick={onClose} disabled={isPending}
          className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100">
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <form onSubmit={handleSubmit(d => mutate(d))} className="flex flex-col gap-4 p-6">
        <div className="flex flex-col gap-1.5">
          <label className="text-sm font-medium text-gray-700">Nome</label>
          <Input placeholder="Nome completo" error={errors.nome?.message} {...register('nome')} />
        </div>
        <div className="flex flex-col gap-1.5">
          <label className="text-sm font-medium text-gray-700">E-mail</label>
          <Input type="email" placeholder="professor@escola.com" error={errors.email?.message} {...register('email')} />
        </div>
        <div className="flex flex-col gap-1.5">
          <label className="text-sm font-medium text-gray-700">Disciplinas</label>
          <Input
            placeholder="Ex: Matemática, Física (separadas por vírgula)"
            error={errors.disciplinas?.message}
            {...register('disciplinas')}
          />
          <p className="text-xs text-gray-400">Separe múltiplas disciplinas por vírgula</p>
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <Button type="button" variant="secondary" onClick={onClose} disabled={isPending}>Cancelar</Button>
          <Button type="submit" loading={isPending}>Enviar convite</Button>
        </div>
      </form>
    </dialog>
  )
}
