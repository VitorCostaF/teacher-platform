// Valores reais do backend: PRESENTE, AUSENTE, JUSTIFICADO
export type StatusFrequencia = 'PRESENTE' | 'AUSENTE' | 'JUSTIFICADO'

export interface FrequenciaAluno {
  alunoId: string
  status: StatusFrequencia | null
  observacao: string
}

export interface LancarFrequenciaDto {
  data: string
  alunos: FrequenciaAluno[]
}

export interface FrequenciaResponse {
  id: number
  data: string
  alunos: FrequenciaAluno[]
}
