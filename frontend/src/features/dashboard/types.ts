export interface DashboardProfessorData {
  alertas: AlertaItem[]
  turmas: TurmaDashboard[]
  mediasHistoricas: MediaHistorica[]
  mapaCalor: MapaCalorItem[]
  temProvaAtiva?: boolean
}

export interface AlertaItem {
  id: number
  tipo: 'aviso' | 'urgente'
  descricao: string
  referenciaId: number
  referenciaUrl: string
}

export interface TurmaDashboard {
  id: number
  nome: string
  media: number
  percentualFrequencia: number
  alunosEmAlerta: number
  ultimaAtividade: string | null
}

export interface MediaHistorica {
  periodo: string
  turmas: Record<string, number>
}

export interface MapaCalorItem {
  turma: string
  topico: string
  percentualErros: number
}
