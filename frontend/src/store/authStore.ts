import type { AuthUser } from '@/features/auth/services/auth.service'

let accessToken: string | null = null
let currentUser: AuthUser | null = null
let expiresAt: Date | null = null

export function setAuth(token: string, user: AuthUser, expiresIn?: number) {
  accessToken = token
  currentUser = user
  expiresAt = expiresIn ? new Date(Date.now() + expiresIn * 1000) : null
}

export function clearAuth() {
  accessToken = null
  currentUser = null
  expiresAt = null
}

export function getAccessToken(): string | null {
  return accessToken
}

export function getCurrentUser(): AuthUser | null {
  return currentUser
}

export function getExpiresAt(): Date | null {
  return expiresAt
}

export function isAuthenticated(): boolean {
  return accessToken !== null
}
