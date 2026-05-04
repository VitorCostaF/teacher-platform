import type { AtividadeRecente } from '../types'

interface Props {
  atividades: AtividadeRecente[]
}

const acaoLabel: Record<string, string> = {
  CRIAR_ALUNO: 'Novo aluno criado',
  CONVIDAR_PROFESSOR: 'Convite de professor enviado',
  ATIVAR_PROFESSOR: 'Professor ativado',
  DESATIVAR_PROFESSOR: 'Professor desativado',
  TRANSFERIR_ALUNO: 'Aluno transferido de turma',
  ATUALIZAR_CONFIGURACOES: 'Configurações atualizadas',
}

const acaoCor: Record<string, string> = {
  CRIAR_ALUNO: 'bg-blue-500',
  CONVIDAR_PROFESSOR: 'bg-purple-500',
  ATIVAR_PROFESSOR: 'bg-green-500',
  DESATIVAR_PROFESSOR: 'bg-red-500',
  TRANSFERIR_ALUNO: 'bg-yellow-500',
  ATUALIZAR_CONFIGURACOES: 'bg-gray-400',
}

function formatarData(iso: string): string {
  return new Date(iso).toLocaleDateString('pt-BR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

export function AtividadeRecenteFeed({ atividades }: Props) {
  if (atividades.length === 0) {
    return (
      <p className="py-4 text-center text-sm text-gray-400">
        Nenhuma atividade recente registrada.
      </p>
    )
  }

  return (
    <ul className="flex flex-col gap-3">
      {atividades.map((a, i) => (
        <li key={i} className="flex items-start gap-3">
          <span className={`mt-1.5 h-2 w-2 shrink-0 rounded-full ${acaoCor[a.acao] ?? 'bg-gray-400'}`} />
          <div className="flex flex-col">
            <span className="text-sm text-gray-800">
              {acaoLabel[a.acao] ?? a.acao}
              {a.entidadeId && (
                <span className="ml-1 text-xs text-gray-400">#{a.entidadeId.slice(0, 8)}</span>
              )}
            </span>
            <span className="text-xs text-gray-400">{formatarData(a.criadoEm)}</span>
          </div>
        </li>
      ))}
    </ul>
  )
}
