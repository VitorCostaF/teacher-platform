import { apiClient } from '@/lib/api'

export type Perfil = 'professor' | 'aluno' | 'responsavel' | 'coordenador' | 'admin'

export interface LoginDto {
  email: string
  senha: string
}

export interface AuthUser {
  id: string
  nome: string
  email: string
  perfil: Perfil
  avatarUrl: string
}

export interface LoginResponse {
  accessToken: string
  expiresIn: number
  perfil: Perfil
  usuario: AuthUser
}

export interface ConviteTokenResponse {
  nome: string
  email: string
  perfil: Perfil
  status: 'VALIDO' | 'EXPIRADO' | 'JA_USADO'
}

export interface AtivarContaDto {
  nome: string
  senha: string
}

export const authService = {
  login: (data: LoginDto) =>
    apiClient.post<LoginResponse>('/auth/login', data).then((r) => r.data),

  refresh: () =>
    apiClient
      .post<{ accessToken: string; expiresIn: number }>('/auth/refresh')
      .then((r) => r.data),

  logout: () => apiClient.post('/auth/logout').then((r) => r.data),

  validarTokenConvite: (token: string) =>
    apiClient.get<ConviteTokenResponse>(`/auth/convite/${token}`).then((r) => r.data),

  ativarConta: (token: string, data: AtivarContaDto) =>
    apiClient.post<LoginResponse>(`/auth/convite/${token}/ativar`, data).then((r) => r.data),

  solicitarRecuperacaoSenha: (email: string) =>
    apiClient.post('/auth/recuperar-senha', { email }).then((r) => r.data),

  redefinirSenha: (token: string, data: { senha: string }) =>
    apiClient.post(`/auth/recuperar-senha/${token}`, data).then((r) => r.data),
}
