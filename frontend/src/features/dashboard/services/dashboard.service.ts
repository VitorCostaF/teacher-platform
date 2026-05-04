import { apiClient } from '@/lib/api'
import type { DashboardProfessorData, DesempenhoTurmaData, DesempenhoAlunoData } from '../types'

export const dashboardService = {
  getProfessor: () =>
    apiClient.get<DashboardProfessorData>('/professor/dashboard').then(r => r.data),

  getDesempenhoTurma: (turmaId: number, periodo?: string) =>
    apiClient.get<DesempenhoTurmaData>(`/turmas/${turmaId}/desempenho`, { params: { periodo } }).then(r => r.data),

  getDesempenhoAluno: (turmaId: number, alunoId: string) =>
    apiClient.get<DesempenhoAlunoData>(`/turmas/${turmaId}/alunos/${alunoId}/desempenho`).then(r => r.data),

  criarObservacao: (turmaId: number, alunoId: string, texto: string) =>
    apiClient.post(`/turmas/${turmaId}/alunos/${alunoId}/observacoes`, { texto }),

  exportarPDF: (tipo: 'turma' | 'aluno', id: number | string) =>
    apiClient.post(`/relatorios/${tipo}/${id}/pdf`).then(r => r.data),
}
