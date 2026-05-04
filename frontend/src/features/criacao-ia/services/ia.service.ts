import { apiClient } from '@/lib/api'
import type {
  GeracaoResponse, GerarProvaDto, QuestaoGerada, RegerarQuestaoDto,
  GerarGradeDto, GradeResponse, SugestaoParams, SugestaoConteudoResponse,
} from '../types'

export const iaService = {
  gerarProva: (req: GerarProvaDto) =>
    apiClient.post<GeracaoResponse>('/ia/gerar-prova', req).then(r => r.data),

  regenerarQuestao: (req: RegerarQuestaoDto) =>
    apiClient.post<GeracaoResponse>('/ia/regenerar-questao', req).then(r => r.data),

  uploadConteudo: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return apiClient
      .post<{ texto: string; aviso?: string }>('/upload/conteudo', form, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: undefined,
      })
      .then(r => r.data)
  },

  gerarGrade: (req: GerarGradeDto) =>
    apiClient.post<GradeResponse>('/ia/gerar-grade', req).then(r => r.data),

  getSugestoesConteudo: (params: SugestaoParams) =>
    apiClient.get<SugestaoConteudoResponse>('/ia/sugestoes-conteudo', { params }).then(r => r.data),
}
