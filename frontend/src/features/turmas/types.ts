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

export interface PeriodoLetivo {
  id: number
  nome: string
  ativo: boolean
}
