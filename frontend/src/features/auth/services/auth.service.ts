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

export const authService = {
  login: (data: LoginDto) =>
    apiClient.post<LoginResponse>('/auth/login', data).then((r) => r.data),

  refresh: () =>
    apiClient
      .post<{ accessToken: string; expiresIn: number }>('/auth/refresh')
      .then((r) => r.data),

  logout: () => apiClient.post('/auth/logout').then((r) => r.data),
}
