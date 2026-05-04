import { apiClient } from '@/lib/api'
import type {
  AdminDashboardData,
  AlunoAdmin,
  ConfiguracoesDto,
  ConfiguracoesEscola,
  ConvidarProfessorDto,
  CriarAlunoDto,
  FiltrosAluno,
  FiltrosProfessor,
  PaginadoResponse,
  ProfessorAdmin,
  TurmaResumoAdmin,
} from '../types'

export const adminService = {
  getDashboard: () =>
    apiClient.get<AdminDashboardData>('/admin/dashboard').then(r => r.data),

  listarProfessores: (params: FiltrosProfessor) =>
    apiClient.get<PaginadoResponse<ProfessorAdmin>>('/admin/professores', { params }).then(r => r.data),

  convidarProfessor: (data: ConvidarProfessorDto) =>
    apiClient.post('/admin/professores/convidar', data),

  alterarStatusProfessor: (id: string, data: { ativo: boolean; motivo: string }) =>
    apiClient.patch(`/admin/professores/${id}/status`, data),

  importarProfessores: (file: File) => {
    const f = new FormData()
    f.append('file', file)
    return apiClient.post('/admin/professores/importar', f)
  },

  listarAlunos: (params: FiltrosAluno) =>
    apiClient.get<PaginadoResponse<AlunoAdmin>>('/admin/alunos', { params }).then(r => r.data),

  criarAluno: (data: CriarAlunoDto) =>
    apiClient.post('/admin/alunos', data),

  transferirAluno: (id: string, novaTurmaId: number) =>
    apiClient.patch(`/admin/alunos/${id}/turma`, { novaTurmaId }),

  importarAlunos: (file: File) => {
    const f = new FormData()
    f.append('file', file)
    return apiClient.post('/admin/alunos/importar', f)
  },

  getConfiguracoes: () =>
    apiClient.get<ConfiguracoesEscola>('/admin/escola/configuracoes').then(r => r.data),

  salvarConfiguracoes: (data: ConfiguracoesDto) =>
    apiClient.put<ConfiguracoesEscola>('/admin/escola/configuracoes', data).then(r => r.data),

  getTurmas: () =>
    apiClient.get<TurmaResumoAdmin[]>('/professor/turmas').then(r => r.data),
}
