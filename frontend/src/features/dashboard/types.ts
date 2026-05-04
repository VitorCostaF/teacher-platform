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

export interface AlunoResumo {
  id: string
  nome: string
  foto?: string
}

export interface DesempenhoTurmaData {
  mediaGeral: number
  maiorNota: number
  menorNota: number
  percentualAprovacao: number
  percentualFrequencia: number
  histograma: Array<{ faixa: string; quantidade: number; alunos: AlunoResumo[] }>
  ranking: Array<{ aluno: AlunoResumo; media: number; frequencia: number; tendencia: 'UP' | 'DOWN' | 'STABLE' }>
}

export interface DesempenhoAlunoData {
  aluno: AlunoResumo
  situacao: string
  evolucaoNotas: Array<{ avaliacaoNome: string; nota: number }>
  frequenciaMensal: Array<{ mes: string; percentual: number }>
  topicosAcerto: Array<{ topico: string; percentual: number }>
  observacoes: Array<{ texto: string; criadoEm: string }>
}
