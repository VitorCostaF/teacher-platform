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

export interface FlashcardData {
  id: number
  pergunta: string
  resposta: string
  topico: string
}

export interface DisciplinaDesempenho {
  id: number
  nome: string
  media: number
  tendencia: 'UP' | 'DOWN' | 'STABLE'
  proximaAvaliacao?: string
}

export interface Conquista {
  tipo: string
  descricao: string
  icone: string
  obtidaEm: string
}

export interface DesempenhoData {
  mediaGlobal: number
  totalEntregues: number
  percentualFrequencia: number
  porDisciplina: DisciplinaDesempenho[]
  evolucaoNotas: { avaliacaoNome: string; nota: number; disciplina: string }[]
  conquistas: Conquista[]
}

// Realização de atividade
export type RespostasMap = Record<number, string | number | string[]>

export interface QuestaoAtividade {
  id: number
  numero: number
  tipo: string
  enunciado: string
  alternativas?: string[]
}

export interface AtividadeDetalhe {
  id: number
  titulo: string
  disciplina: string
  prazo: string
  permiteAtraso: boolean
  questoes: QuestaoAtividade[]
  respostasRascunho?: RespostasMap
}

export interface EntregaResult {
  id: number
  status: string
  entregueEm: string
}
