import { useQuery } from '@tanstack/react-query'
import { turmasService } from '../services/turmas.service'

export function useAlunosTurma(turmaId: number) {
  return useQuery({
    queryKey: ['turma-alunos', turmaId],
    queryFn: () => turmasService.listarAlunos(turmaId),
    enabled: turmaId > 0,
  })
}
