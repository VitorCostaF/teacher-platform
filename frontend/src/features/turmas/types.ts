export interface TurmaResumo {
  id: number
  nome: string
  disciplina: string
  totalAlunos: number
  proximaAula: string | null
  pendencias: {
    frequenciasNaoLancadas: number
    atividadesNaoCorrigidas: number
  }
}

export interface TurmaDetalhe {
  id: number
  nome: string
  disciplina: string
  serie: string
  totalAlunos: number
  proximaAula: string | null
  gradeHoraria: string | null
  periodoLetivo: string
  professor: string
  pendencias: {
    frequenciasNaoLancadas: number
    atividadesNaoCorrigidas: number
  }
}

export interface AlunoTurma {
  id: string
  nome: string
  email: string
  avatarUrl: string | null
  matriculadoEm: string
}

export interface ImportacaoResult {
  importados: number
  erros: Array<{ linha: number; motivo: string }>
}

export interface PeriodoLetivo {
  id: number
  nome: string
  ativo: boolean
}
