import { useNavigate } from 'react-router-dom'
import type { FeedItem } from '../types'

interface Props {
  item: FeedItem
}

const STATUS_STYLE: Record<string, string> = {
  PENDENTE: 'bg-gray-100 text-gray-600',
  EM_ANDAMENTO: 'bg-blue-100 text-blue-700',
  ENTREGUE: 'bg-green-100 text-green-700',
  ATRASADO: 'bg-red-100 text-red-700',
  CORRIGIDO: 'bg-purple-100 text-purple-700',
}

const STATUS_LABEL: Record<string, string> = {
  PENDENTE: 'Pendente',
  EM_ANDAMENTO: 'Em andamento',
  ENTREGUE: 'Entregue',
  ATRASADO: 'Atrasado',
  CORRIGIDO: 'Corrigido',
}

function formatarPrazo(prazoISO: string): string {
  const prazo = new Date(prazoISO)
  const agora = new Date()
  const diffMs = prazo.getTime() - agora.getTime()
  const diffH = Math.round(diffMs / 3_600_000)

  if (diffH < 0) return 'Prazo encerrado'
  if (diffH < 1) return 'Menos de 1 hora'
  if (diffH < 24) return `${diffH}h restantes`
  const diffD = Math.round(diffH / 24)
  if (diffD === 1) return 'Amanhã'
  if (diffD < 7) return `${diffD} dias`
  return prazo.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' })
}

export function FeedItemCard({ item }: Props) {
  const navigate = useNavigate()
  const rota = item.tipo === 'PROVA' ? `/aluno/provas/${item.id}` : `/aluno/atividades/${item.id}`

  const bg = item.atrasado
    ? 'bg-red-50 border-red-200'
    : 'bg-white border-gray-200'

  return (
    <button
      type="button"
      onClick={() => navigate(rota)}
      className={`w-full rounded-xl border p-4 text-left transition-shadow hover:shadow-md ${bg}`}
    >
      <div className="flex items-start justify-between gap-2">
        <div className="flex flex-col gap-1.5">
          {/* Tipo badge */}
          <span className={[
            'inline-block self-start rounded-full px-2 py-0.5 text-xs font-medium',
            item.tipo === 'PROVA' ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700',
          ].join(' ')}>
            {item.tipo === 'PROVA' ? 'Prova' : 'Atividade'}
          </span>

          <p className="text-sm font-semibold text-gray-900 leading-snug">{item.titulo}</p>
          <p className="text-xs text-gray-500">{item.disciplina}</p>
        </div>

        {/* Status */}
        <span className={[
          'shrink-0 rounded-full px-2 py-0.5 text-xs font-medium',
          STATUS_STYLE[item.status] ?? 'bg-gray-100 text-gray-600',
        ].join(' ')}>
          {STATUS_LABEL[item.status] ?? item.status}
        </span>
      </div>

      {/* Prazo */}
      <div className={[
        'mt-3 flex items-center gap-1.5 text-xs font-medium',
        item.atrasado ? 'text-red-600' : 'text-gray-500',
      ].join(' ')}>
        <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        {formatarPrazo(item.prazo)}
      </div>
    </button>
  )
}
