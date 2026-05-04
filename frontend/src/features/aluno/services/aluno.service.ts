import { apiClient } from '@/lib/api'
import type { DesempenhoData, FeedData } from '../types'

export const alunoService = {
  getFeed: () =>
    apiClient.get<FeedData>('/aluno/feed').then(r => r.data),

  getDesempenho: () =>
    apiClient.get<DesempenhoData>('/aluno/desempenho').then(r => r.data),
}
