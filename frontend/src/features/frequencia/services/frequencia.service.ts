import { apiClient } from '@/lib/api'
import type { FrequenciaResponse, LancarFrequenciaDto } from '../types'

export const frequenciaService = {
  buscarPorData: (turmaId: number, data: string) =>
    apiClient
      .get<FrequenciaResponse>(`/turmas/${turmaId}/frequencia`, { params: { data } })
      .then(r => (r.data && typeof r.data === 'object' ? r.data : null))
      .catch(() => null),

  lancar: (turmaId: number, payload: LancarFrequenciaDto) =>
    apiClient.post<FrequenciaResponse>(`/turmas/${turmaId}/frequencia`, payload).then(r => r.data),

  editar: (turmaId: number, frequenciaId: number, payload: LancarFrequenciaDto) =>
    apiClient.put<FrequenciaResponse>(`/turmas/${turmaId}/frequencia/${frequenciaId}`, payload).then(r => r.data),
}
