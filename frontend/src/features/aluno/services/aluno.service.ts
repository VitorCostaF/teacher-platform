import { apiClient } from '@/lib/api'
import type { AtividadeDetalhe, DesempenhoData, EntregaResult, FeedData, FlashcardData, RespostasMap } from '../types'

export const alunoService = {
  getFeed: () =>
    apiClient.get<FeedData>('/aluno/feed').then(r => r.data),

  getDesempenho: () =>
    apiClient.get<DesempenhoData>('/aluno/desempenho').then(r => r.data),

  getFlashcards: (disciplinaId?: number) =>
    apiClient.get<FlashcardData[]>('/aluno/flashcards', { params: disciplinaId ? { disciplina: disciplinaId } : undefined }).then(r => r.data),

  avaliarFlashcard: (cardId: number, sabia: boolean) =>
    apiClient.post(`/aluno/flashcards/${cardId}/avaliacao`, { sabia }).then(r => r.data),

  getAtividade: (id: number) =>
    apiClient.get<AtividadeDetalhe>(`/atividades/${id}`).then(r => r.data),

  salvarRascunho: (id: number, respostas: RespostasMap) =>
    apiClient.put(`/atividades/${id}/rascunho`, { respostas }).then(r => r.data),

  entregar: (id: number, respostas: RespostasMap) =>
    apiClient.post<EntregaResult>(`/atividades/${id}/entregar`, { respostas }).then(r => r.data),

  uploadRespostaArquivo: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return apiClient.post<{ url: string }>('/upload/resposta', form).then(r => r.data)
  },
}
