import { apiClient } from '@/lib/api'
import type { DesempenhoData, FeedData, FlashcardData } from '../types'

export const alunoService = {
  getFeed: () =>
    apiClient.get<FeedData>('/aluno/feed').then(r => r.data),

  getDesempenho: () =>
    apiClient.get<DesempenhoData>('/aluno/desempenho').then(r => r.data),

  getFlashcards: (disciplinaId?: number) =>
    apiClient.get<FlashcardData[]>('/aluno/flashcards', { params: disciplinaId ? { disciplina: disciplinaId } : undefined }).then(r => r.data),

  avaliarFlashcard: (cardId: number, sabia: boolean) =>
    apiClient.post(`/aluno/flashcards/${cardId}/avaliacao`, { sabia }).then(r => r.data),
}
