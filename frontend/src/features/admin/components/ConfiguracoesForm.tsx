import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Skeleton } from '@/components/feedback/Skeleton'
import { toastEmitter } from '@/lib/toastEmitter'
import { adminService } from '../services/admin.service'

const schema = z.object({
  nome: z.string().min(2, 'Nome da escola obrigatório'),
  notaMinimaAprovacao: z.number({ invalid_type_error: 'Informe um número' }).min(0).max(10),
  frequenciaMinimaAprovacao: z.number({ invalid_type_error: 'Informe um número' }).min(0).max(100),
  sistemaAvaliacao: z.enum(['NUMERICA', 'CONCEITUAL']),
})

type FormData = z.infer<typeof schema>

export function ConfiguracoesForm() {
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['admin', 'configuracoes'],
    queryFn: adminService.getConfiguracoes,
  })

  const { register, handleSubmit, reset, formState: { errors, isDirty } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  useEffect(() => {
    if (data) {
      reset({
        nome: data.nome,
        notaMinimaAprovacao: data.notaMinimaAprovacao,
        frequenciaMinimaAprovacao: data.frequenciaMinimaAprovacao,
        sistemaAvaliacao: data.sistemaAvaliacao,
      })
    }
  }, [data, reset])

  const { mutate, isPending } = useMutation({
    mutationFn: adminService.salvarConfiguracoes,
    onSuccess: () => {
      toastEmitter.emit('success', 'Configurações salvas com sucesso.')
      queryClient.invalidateQueries({ queryKey: ['admin', 'configuracoes'] })
    },
  })

  if (isLoading) {
    return (
      <div className="flex flex-col gap-4">
        <Skeleton className="h-10 w-full rounded-xl" />
        <Skeleton className="h-10 w-1/2 rounded-xl" />
        <Skeleton className="h-10 w-1/2 rounded-xl" />
      </div>
    )
  }

  return (
    <form onSubmit={handleSubmit(d => mutate(d))} className="flex flex-col gap-6">
      <section className="flex flex-col gap-4 rounded-xl border border-gray-200 bg-white p-5">
        <h2 className="text-sm font-semibold text-gray-700">Dados da Escola</h2>
        <div className="flex flex-col gap-1.5">
          <label className="text-sm font-medium text-gray-700">Nome da escola</label>
          <Input placeholder="Nome da escola" error={errors.nome?.message} {...register('nome')} />
        </div>
        {data?.cnpj && (
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium text-gray-500">CNPJ</label>
            <p className="rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-sm text-gray-600">
              {data.cnpj}
            </p>
          </div>
        )}
      </section>

      <section className="flex flex-col gap-4 rounded-xl border border-gray-200 bg-white p-5">
        <h2 className="text-sm font-semibold text-gray-700">Regras Pedagógicas</h2>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium text-gray-700">Nota mínima para aprovação</label>
            <Input
              type="number"
              step="0.1"
              min="0"
              max="10"
              placeholder="Ex: 5.0"
              error={errors.notaMinimaAprovacao?.message}
              {...register('notaMinimaAprovacao', { valueAsNumber: true })}
            />
          </div>
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium text-gray-700">Frequência mínima (%)</label>
            <Input
              type="number"
              step="1"
              min="0"
              max="100"
              placeholder="Ex: 75"
              error={errors.frequenciaMinimaAprovacao?.message}
              {...register('frequenciaMinimaAprovacao', { valueAsNumber: true })}
            />
          </div>
        </div>
        <div className="flex flex-col gap-1.5">
          <label className="text-sm font-medium text-gray-700">Sistema de avaliação</label>
          <select
            className="w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm text-gray-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
            {...register('sistemaAvaliacao')}
          >
            <option value="NUMERICA">Numérico (0–10)</option>
            <option value="CONCEITUAL">Conceitual (A–F)</option>
          </select>
          {errors.sistemaAvaliacao && <p className="text-xs text-red-600">{errors.sistemaAvaliacao.message}</p>}
        </div>
      </section>

      <div className="flex justify-end">
        <Button type="submit" loading={isPending} disabled={!isDirty || isPending}>
          Salvar configurações
        </Button>
      </div>
    </form>
  )
}
