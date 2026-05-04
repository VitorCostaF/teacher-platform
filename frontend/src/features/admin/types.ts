export interface AdminDashboardData {
  totalAlunos: number
  totalProfessores: number
  totalTurmas: number
  mediaNotas: number
  frequenciaMedia: number
  desempenhoPorSerie: DesempenhoSerie[]
  alertasConsolidados: AlertaConsolidado[]
  atividadeRecente: AtividadeRecente[]
}

export interface DesempenhoSerie {
  serie: string
  media: number
  totalAlunos: number
}

export interface AlertaConsolidado {
  tipo: string
  descricao: string
  referenciaId: number
  referenciaUrl: string
}

export interface AtividadeRecente {
  acao: string
  entidade: string
  entidadeId: string
  criadoEm: string
}

export interface ProfessorAdmin {
  id: string
  nome: string
  email: string
  avatarUrl: string | null
  ativo: boolean
}

export interface AlunoAdmin {
  id: string
  nome: string
  email: string
  avatarUrl: string | null
  ativo: boolean
}

export interface TurmaResumoAdmin {
  id: number
  nome: string
  disciplina: string
}

export interface ConfiguracoesEscola {
  id: number
  nome: string
  cnpj: string
  logoUrl: string | null
  notaMinimaAprovacao: number
  frequenciaMinimaAprovacao: number
  sistemaAvaliacao: 'NUMERICA' | 'CONCEITUAL'
}

export interface ConvidarProfessorDto {
  nome: string
  email: string
  disciplinas: string[]
}

export interface ResponsavelDto {
  nome: string
  email: string
  parentesco?: string
}

export interface CriarAlunoDto {
  nome: string
  email: string
  turmasIds: number[]
  responsaveis?: ResponsavelDto[]
}

export interface ConfiguracoesDto {
  nome: string
  notaMinimaAprovacao: number
  frequenciaMinimaAprovacao: number
  sistemaAvaliacao: 'NUMERICA' | 'CONCEITUAL'
}

export interface FiltrosProfessor {
  nome?: string
  ativo?: boolean
  page?: number
  size?: number
}

export interface FiltrosAluno {
  nome?: string
  ativo?: boolean
  page?: number
  size?: number
}

export interface PaginadoResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
