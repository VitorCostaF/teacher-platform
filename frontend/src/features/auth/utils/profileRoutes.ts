import type { Perfil } from '../services/auth.service'

export const ROTAS_POR_PERFIL: Record<Perfil, string> = {
  professor: '/professor/dashboard',
  aluno: '/aluno/feed',
  responsavel: '/responsavel/acompanhamento',
  coordenador: '/admin/visao-geral',
  admin: '/admin/visao-geral',
}
