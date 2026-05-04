export interface AlunoResponsavel {
  id: string
  nome: string
  avatarUrl?: string
}

export interface AlertaItem {
  id: string
  tipo: string
  mensagem: string
  disciplina?: string
}

export interface PainelData {
  mediaGeral: number
  percentualFrequencia: number
  proximaProva?: {
    id: string
    titulo: string
    disciplina: string
    data: string
  }
  alertasAtivos: AlertaItem[]
}

export interface BoletimDisciplina {
  disciplina: string
  notas: (number | null)[]
  mediaFinal: number | null
  situacao: string
}

export interface FrequenciaCalendarioDia {
  data: string
  status: 'PRESENTE' | 'FALTA' | 'FALTA_JUSTIFICADA' | 'SEM_AULA'
}

export interface FrequenciaData {
  disciplina: string
  percentual: number
  presencas: number
  totalAulas: number
  diasCalendario: FrequenciaCalendarioDia[]
}

export interface ProvaCalendario {
  id: string
  titulo: string
  disciplina: string
  data: string
  nota?: number | null
}
