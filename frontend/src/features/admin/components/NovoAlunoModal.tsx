import { useEffect, useRef } from 'react'
import { useForm, useFieldArray } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { toastEmitter } from '@/lib/toastEmitter'
import { adminService } from '../services/admin.service'

const responsavelSchema = z.object({
  nome: z.string().min(2, 'Nome obrigatório'),
  email: z.string().email('E-mail inválido'),
  parentesco: z.string().optional(),
})

const schema = z.object({
  nome: z.string().min(2, 'Nome obrigatório'),
  email: z.string().email('E-mail inválido'),
  turmasIds: z.array(z.number()).min(1, 'Selecione ao menos uma turma'),
  responsaveis: z.array(responsavelSchema).optional(),
})

type FormData = z.infer<typeof schema>

interface Props {
  isOpen: boolean
  onClose: () => void
}

export function NovoAlunoModal({ isOpen, onClose }: Props) {
  const dialogRef = useRef<HTMLDialogElement>(null)
  const queryClient = useQueryClient()

  const { register, handleSubmit, reset, watch, setValue, control, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { turmasIds: [], responsaveis: [] },
  })

  const { fields: respFields, append, remove } = useFieldArray({ control, name: 'responsaveis' })

  const { data: turmas = [] } = useQuery({
    queryKey: ['admin', 'turmas'],
    queryFn: adminService.getTurmas,
    enabled: isOpen,
  })

  useEffect(() => {
    const dialog = dialogRef.current
    if (!dialog) return
    if (isOpen) { if (!dialog.open) dialog.showModal() }
    else { if (dialog.open) dialog.close(); reset() }
  }, [isOpen, reset])

  const { mutate, isPending } = useMutation({
    mutationFn: (data: FormData) => adminService.criarAluno(data),
    onSuccess: () => {
      toastEmitter.emit('success', 'Aluno criado. Convite enviado por e-mail.')
      queryClient.invalidateQueries({ queryKey: ['admin', 'alunos'] })
      onClose()
    },
  })

  const turmasSelecionadas = watch('turmasIds') ?? []

  function toggleTurma(id: number) {
    const atual = turmasSelecionadas
    const nova = atual.includes(id) ? atual.filter(t => t !== id) : [...atual, id]
    setValue('turmasIds', nova, { shouldValidate: true })
  }

  const handleBackdrop = (e: React.MouseEvent<HTMLDialogElement>) => {
    if (e.target === dialogRef.current && !isPending) onClose()
  }

  return (
    <dialog
      ref={dialogRef}
      onClick={handleBackdrop}
      aria-modal="true"
      className="w-full max-w-lg rounded-2xl border border-gray-200 bg-white p-0 shadow-xl backdrop:bg-black/40 open:flex open:flex-col"
    >
      <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
        <h2 className="text-base font-semibold text-gray-900">Novo Aluno</h2>
        <button type="button" onClick={onClose} disabled={isPending}
          className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100">
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <form onSubmit={handleSubmit(d => mutate(d))} className="flex flex-col gap-5 overflow-y-auto p-6">
        <div className="flex flex-col gap-3">
          <h3 className="text-xs font-semibold uppercase tracking-wide text-gray-500">Dados pessoais</h3>
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium text-gray-700">Nome</label>
            <Input placeholder="Nome completo" error={errors.nome?.message} {...register('nome')} />
          </div>
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium text-gray-700">E-mail</label>
            <Input type="email" placeholder="aluno@escola.com" error={errors.email?.message} {...register('email')} />
          </div>
        </div>

        <div className="flex flex-col gap-3">
          <h3 className="text-xs font-semibold uppercase tracking-wide text-gray-500">Turmas</h3>
          {errors.turmasIds && <p className="text-xs text-red-600">{errors.turmasIds.message}</p>}
          <div className="flex flex-wrap gap-2">
            {turmas.map(t => (
              <button
                key={t.id}
                type="button"
                onClick={() => toggleTurma(t.id)}
                className={[
                  'rounded-full border px-3 py-1 text-xs font-medium transition-colors',
                  turmasSelecionadas.includes(t.id)
                    ? 'border-blue-500 bg-blue-500 text-white'
                    : 'border-gray-200 bg-white text-gray-600 hover:border-blue-300',
                ].join(' ')}
              >
                {t.nome}
              </button>
            ))}
            {turmas.length === 0 && <p className="text-xs text-gray-400">Nenhuma turma disponível</p>}
          </div>
        </div>

        <div className="flex flex-col gap-3">
          <div className="flex items-center justify-between">
            <h3 className="text-xs font-semibold uppercase tracking-wide text-gray-500">Responsáveis</h3>
            <button
              type="button"
              onClick={() => append({ nome: '', email: '', parentesco: '' })}
              className="text-xs text-blue-600 hover:underline"
            >
              + Adicionar
            </button>
          </div>
          {respFields.map((field, i) => (
            <div key={field.id} className="flex flex-col gap-2 rounded-xl border border-gray-200 p-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-medium text-gray-500">Responsável {i + 1}</span>
                <button type="button" onClick={() => remove(i)} className="text-xs text-red-500 hover:underline">Remover</button>
              </div>
              <Input placeholder="Nome" {...register(`responsaveis.${i}.nome`)} />
              <Input type="email" placeholder="E-mail" {...register(`responsaveis.${i}.email`)} />
              <Input placeholder="Parentesco (ex: Mãe)" {...register(`responsaveis.${i}.parentesco`)} />
            </div>
          ))}
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <Button type="button" variant="secondary" onClick={onClose} disabled={isPending}>Cancelar</Button>
          <Button type="submit" loading={isPending}>Criar aluno</Button>
        </div>
      </form>
    </dialog>
  )
}
