import { apiClient } from '@/lib/api'
import type { AlunoResponsavel, PainelData, BoletimDisciplina, FrequenciaData, ProvaCalendario } from '../types'

export const responsavelService = {
  getAlunos: () =>
    apiClient.get<AlunoResponsavel[]>('/responsavel/alunos').then(r => r.data),

  getPainel: (alunoId: string) =>
    apiClient.get<PainelData>(`/responsavel/alunos/${alunoId}/painel`).then(r => r.data),

  getBoletim: (alunoId: string, periodo?: string) =>
    apiClient.get<BoletimDisciplina[]>(`/responsavel/alunos/${alunoId}/boletim`, {
      params: { periodo },
    }).then(r => r.data),

  getFrequencia: (alunoId: string) =>
    apiClient.get<FrequenciaData[]>(`/responsavel/alunos/${alunoId}/frequencia`).then(r => r.data),

  getCalendario: (alunoId: string) =>
    apiClient.get<ProvaCalendario[]>(`/responsavel/alunos/${alunoId}/calendario`).then(r => r.data),
}
