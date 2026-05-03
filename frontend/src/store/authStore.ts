import type { AuthUser } from '@/features/auth/services/auth.service'

let accessToken: string | null = null
let currentUser: AuthUser | null = null

export function setAuth(token: string, user: AuthUser) {
  accessToken = token
  currentUser = user
}

export function clearAuth() {
  accessToken = null
  currentUser = null
}

export function getAccessToken(): string | null {
  return accessToken
}

export function getCurrentUser(): AuthUser | null {
  return currentUser
}

export function isAuthenticated(): boolean {
  return accessToken !== null
}
