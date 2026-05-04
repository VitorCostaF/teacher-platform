import { apiClient } from '@/lib/api'
import type { QuestaoGerada, ProvaConfig } from '../types'

export interface CriarRascunhoResult {
  id: number
  titulo: string
}

function mapQuestoes(questoes: QuestaoGerada[]) {
  return questoes.map(q => ({
    enunciado: q.enunciado,
    tipo: q.tipo,
    alternativas: q.alternativas ?? null,
    gabaritoIndice: q.tipo === 'MULTIPLA_ESCOLHA' || q.tipo === 'VERDADEIRO_FALSO' ? q.gabarito : null,
    gabaritoDissertativo: q.criteriosCorrecao ?? null,
    dificuldade: q.dificuldade,
    topico: q.topico,
    pontos: null,
  }))
}

export const provasService = {
  salvarRascunho: (config: ProvaConfig, questoes: QuestaoGerada[]) =>
    apiClient.post<CriarRascunhoResult>('/provas/rascunho', {
      titulo: config.titulo,
      tipo: 'PROVA',
      turmaId: config.turmaId,
      duracaoMinutos: config.duracaoMinutos ? Number(config.duracaoMinutos) : null,
      questoes: mapQuestoes(questoes),
    }).then(r => r.data),
}
