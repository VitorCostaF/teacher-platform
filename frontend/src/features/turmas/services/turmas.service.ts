import { apiClient } from '@/lib/api'
import type { AlunoTurma, ImportacaoResult, PeriodoLetivo, TurmaDetalhe, TurmaResumo } from '../types'

export const turmasService = {
  listar: (periodoId?: number) =>
    apiClient
      .get<TurmaResumo[]>('/professor/turmas', { params: periodoId ? { periodo: periodoId } : undefined })
      .then(r => r.data),

  listarPeriodos: () =>
    apiClient.get<PeriodoLetivo[]>('/periodos-letivos').then(r => r.data),

  detalhe: (id: number) =>
    apiClient.get<TurmaDetalhe>(`/turmas/${id}`).then(r => r.data),

  listarAlunos: (turmaId: number) =>
    apiClient.get<AlunoTurma[]>(`/turmas/${turmaId}/alunos`).then(r => r.data),

  adicionarAluno: (turmaId: number, data: { alunoId?: string; email?: string }) =>
    apiClient.post<AlunoTurma>(`/turmas/${turmaId}/alunos`, data).then(r => r.data),

  removerAluno: (turmaId: number, alunoId: string) =>
    apiClient.delete(`/turmas/${turmaId}/alunos/${alunoId}`).then(r => r.data),

  importarAlunos: (turmaId: number, file: File) => {
    const form = new FormData()
    form.append('file', file)
    return apiClient.post<ImportacaoResult>(`/turmas/${turmaId}/alunos/importar`, form).then(r => r.data)
  },

  buscarAlunos: (query: string) =>
    apiClient.get<AlunoTurma[]>('/alunos/busca', { params: { q: query } }).then(r => r.data),
}
