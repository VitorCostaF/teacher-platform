import { apiClient } from '@/lib/api'
import type { DashboardProfessorData } from '../types'

export const dashboardService = {
  getProfessor: () =>
    apiClient.get<DashboardProfessorData>('/professor/dashboard').then(r => r.data),
}
