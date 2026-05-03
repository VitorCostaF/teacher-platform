import { useQuery } from '@tanstack/react-query'
import { turmasService } from '../services/turmas.service'

export function useTurmas(periodoId?: number) {
  return useQuery({
    queryKey: ['turmas', periodoId],
    queryFn: () => turmasService.listar(periodoId),
  })
}

export function usePeriodosLetivos() {
  return useQuery({
    queryKey: ['periodos-letivos'],
    queryFn: () => turmasService.listarPeriodos(),
  })
}
