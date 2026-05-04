export type TipoQuestao = 'MULTIPLA_ESCOLHA' | 'VERDADEIRO_FALSO' | 'DISSERTATIVA'
export type NivelDificuldade = 'FACIL' | 'MEDIO' | 'DIFICIL' | 'MISTO'
export type FonteConteudo = 'texto' | 'upload' | 'topicos'

export interface QuestaoGerada {
  id: string
  tipo: TipoQuestao
  enunciado: string
  alternativas?: string[]
  gabarito?: number
  dificuldade: string
  topico: string
  criteriosCorrecao?: string
}

export interface GeracaoResponse {
  questoes: QuestaoGerada[]
  tokensUsados: number
}

export interface GerarProvaDto {
  disciplina: string
  serie: string
  dificuldade: NivelDificuldade
  quantidadesPorTipo: Partial<Record<TipoQuestao, number>>
  conteudoTexto?: string
  topicos?: string[]
}

export interface RegerarQuestaoDto {
  disciplina: string
  serie: string
  tipo: TipoQuestao
  dificuldade: string
  topico: string
}

export interface ProvaConfig {
  turmaId: number | null
  disciplina: string
  serie: string
  titulo: string
  dificuldade: NivelDificuldade
  duracaoMinutos: string
  quantidades: Partial<Record<TipoQuestao, number>>
  fonte: FonteConteudo
  conteudoTexto: string
  topicos: string[]
}
