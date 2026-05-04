import { useQuery } from '@tanstack/react-query'
import { dashboardService } from '../services/dashboard.service'

export function useDashboardProfessor() {
  const query = useQuery({
    queryKey: ['dashboard', 'professor'],
    queryFn: dashboardService.getProfessor,
    refetchInterval: (q) => (q.state.data?.temProvaAtiva ? 30000 : false),
  })

  return query
}
