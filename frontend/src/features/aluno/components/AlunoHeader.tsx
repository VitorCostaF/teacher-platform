import { getCurrentUser } from '@/store/authStore'

interface Props {
  notificacoes?: number
  pontos?: number
}

function saudacao(): string {
  const hora = new Date().getHours()
  if (hora < 12) return 'Bom dia'
  if (hora < 18) return 'Boa tarde'
  return 'Boa noite'
}

export function AlunoHeader({ notificacoes = 0, pontos = 0 }: Props) {
  const user = getCurrentUser()
  const primeiroNome = user?.nome.split(' ')[0] ?? 'Aluno'

  return (
    <header className="flex items-center justify-between bg-white px-4 py-4 shadow-sm">
      <div>
        <p className="text-xs text-gray-500">{saudacao()},</p>
        <h1 className="text-base font-bold text-gray-900">{primeiroNome} 👋</h1>
      </div>

      <div className="flex items-center gap-3">
        {/* Pontos */}
        {pontos > 0 && (
          <div className="flex items-center gap-1 rounded-full bg-yellow-50 px-2.5 py-1">
            <svg className="h-4 w-4 text-yellow-500" fill="currentColor" viewBox="0 0 20 20">
              <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
            </svg>
            <span className="text-xs font-semibold text-yellow-700">{pontos}</span>
          </div>
        )}

        {/* Notificações */}
        <button type="button" className="relative rounded-full p-1.5 text-gray-500 hover:bg-gray-100">
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
          </svg>
          {notificacoes > 0 && (
            <span className="absolute -right-0.5 -top-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-xs font-bold text-white">
              {notificacoes > 9 ? '9+' : notificacoes}
            </span>
          )}
        </button>
      </div>
    </header>
  )
}
