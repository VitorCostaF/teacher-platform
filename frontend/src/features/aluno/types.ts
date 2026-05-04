export interface FeedData {
  urgentes: FeedItem[]
  paraFazer: FeedItem[]
  novosConteudos: ConteudoItem[]
  recomendados: RecomendacaoItem[]
}

export interface FeedItem {
  id: number
  tipo: 'PROVA' | 'ATIVIDADE'
  titulo: string
  disciplina: string
  prazo: string
  status: string
  atrasado: boolean
}

export interface ConteudoItem {
  id: number
  titulo: string
  tipo: 'texto' | 'video' | 'pdf'
  tempoLeituraMinutos: number
}

export interface RecomendacaoItem {
  id: number
  titulo: string
  explicacao: string
  tipo: string
}

export interface DesempenhoData {
  mediaGeral: number
  totalAtividades: number
  totalEntregues: number
  porDisciplina: { disciplina: string; media: number }[]
}
