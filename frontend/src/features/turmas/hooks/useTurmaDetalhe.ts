import { useQuery } from '@tanstack/react-query'
import { turmasService } from '../services/turmas.service'

export function useTurmaDetalhe(turmaId: number) {
  return useQuery({
    queryKey: ['turma', turmaId],
    queryFn: () => turmasService.detalhe(turmaId),
    enabled: turmaId > 0,
  })
}
