import { apiClient } from '@/lib/api'
import type { PeriodoLetivo, TurmaResumo } from '../types'

export const turmasService = {
  listar: (periodoId?: number) =>
    apiClient
      .get<TurmaResumo[]>('/professor/turmas', { params: periodoId ? { periodo: periodoId } : undefined })
      .then(r => r.data),

  listarPeriodos: () =>
    apiClient.get<PeriodoLetivo[]>('/periodos-letivos').then(r => r.data),
}
