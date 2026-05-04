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

// Grade de Aulas
export type PeriodoGrade = 'SEMANA' | 'MES' | 'SEMESTRE'

export interface AulaGrade {
  id: string
  semana: number
  aula: number
  conteudo: string
  objetivos: string
  recursosSugeridos: string
}

export interface GradeResponse {
  aulas: AulaGrade[]
}

export interface GerarGradeDto {
  turmaId: number
  disciplina: string
  serie: string
  periodo: PeriodoGrade
  aulasPorSemana: number
  topicosObrigatorios?: string
  alinhamentoBNCC: boolean
}

// Revisão e Publicação
export interface PublicarDto {
  disponivelEm: string
  encerraEm?: string
  turmasIds: number[]
  embaralharQuestoes: boolean
  embaralharAlternativas: boolean
  liberarGabaritoApos: 'entrega' | 'encerramento' | 'manual'
  peso: number
}

export interface PreviewAvaliacaoResponse {
  id: number
  titulo: string
  disciplina: string
  serie: string
  duracaoMinutos: number | null
  questoes: QuestaoGerada[]
}

// Gerador de Atividades
export type TipoEntrega = 'ONLINE' | 'PDF' | 'AMBOS'
export type TipoQuestaoAtividade =
  | TipoQuestao
  | 'LEITURA_COM_PERGUNTAS'
  | 'PESQUISA_COM_ROTEIRO'
  | 'PROJETO_COM_ETAPAS'

// Sugestão de Conteúdos
export interface SugestaoParams {
  serie: string
  disciplina: string
  bimestre: string
}

export interface SugestaoConteudoResponse {
  competenciasBNCC: string[]
  topicos: string[]
  linksComplementares: string[]
}
