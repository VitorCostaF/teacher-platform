import { apiClient } from '@/lib/api'
import type {
  AtividadeDetalhe, AutosavePayload, DesempenhoData, EntregaResult,
  FeedData, FlashcardData, RespostasMap, ResultadoData, SessaoProvaData,
} from '../types'

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

  iniciarProva: (provaId: number) =>
    apiClient.post<SessaoProvaData>(`/provas/${provaId}/iniciar`).then(r => r.data),

  autosaveProva: (provaId: number, sessaoId: number, payload: AutosavePayload) =>
    apiClient.put(`/provas/${provaId}/sessoes/${sessaoId}/autosave`, payload).then(r => r.data),

  entregarProva: (provaId: number, sessaoId: number, respostas: RespostasMap) =>
    apiClient.post<EntregaResult>(`/provas/${provaId}/sessoes/${sessaoId}/entregar`, { respostas }).then(r => r.data),

  getResultado: (entregaId: number) =>
    apiClient.get<ResultadoData>(`/aluno/avaliacoes/${entregaId}/resultado`).then(r => r.data),
}
