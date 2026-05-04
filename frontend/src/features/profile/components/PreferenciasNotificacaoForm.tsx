import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiClient } from '@/lib/api'
import { Skeleton } from '@/components/feedback/Skeleton'

interface Preferencias {
  faltaAluno: boolean
  quedaFrequencia: boolean
  prazoProva: boolean
}

async function fetchPreferencias(): Promise<Preferencias> {
  const res = await apiClient.get<Preferencias>('/usuario/preferencias-notificacao')
  return res.data
}

async function salvarPreferencias(prefs: Partial<Preferencias>): Promise<Preferencias> {
  const res = await apiClient.put<Preferencias>('/usuario/preferencias-notificacao', prefs)
  return res.data
}

interface ToggleRowProps {
  label: string
  checked: boolean
  onChange: (v: boolean) => void
}

function ToggleRow({ label, checked, onChange }: ToggleRowProps) {
  return (
    <div className="flex items-center justify-between py-3">
      <span className="text-sm text-gray-800">{label}</span>
      <button
        type="button"
        role="switch"
        aria-checked={checked}
        onClick={() => onChange(!checked)}
        className={[
          'relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2',
          checked ? 'bg-blue-600' : 'bg-gray-300',
        ].join(' ')}
      >
        <span
          className={[
            'pointer-events-none inline-block h-5 w-5 rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out',
            checked ? 'translate-x-5' : 'translate-x-0',
          ].join(' ')}
        />
      </button>
    </div>
  )
}

export function PreferenciasNotificacaoForm() {
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['preferencias-notificacao'],
    queryFn: fetchPreferencias,
  })

  const { mutate } = useMutation({
    mutationFn: salvarPreferencias,
    onSuccess: (updated) => {
      queryClient.setQueryData(['preferencias-notificacao'], updated)
    },
  })

  if (isLoading) return <Skeleton className="h-32 w-full rounded-xl" />
  if (!data) return null

  return (
    <div className="rounded-xl border border-gray-200 bg-white px-4 shadow-sm divide-y divide-gray-100">
      <ToggleRow
        label="Faltas do meu filho"
        checked={data.faltaAluno}
        onChange={v => mutate({ faltaAluno: v })}
      />
      <ToggleRow
        label="Queda de frequência"
        checked={data.quedaFrequencia}
        onChange={v => mutate({ quedaFrequencia: v })}
      />
      <ToggleRow
        label="Lembretes de prazo"
        checked={data.prazoProva}
        onChange={v => mutate({ prazoProva: v })}
      />
    </div>
  )
}
