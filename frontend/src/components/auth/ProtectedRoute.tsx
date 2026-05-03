import { Navigate, useLocation } from 'react-router-dom'
import { isAuthenticated } from '@/store/authStore'

export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const location = useLocation()

  if (!isAuthenticated()) {
    sessionStorage.setItem('redirect_url', location.pathname + location.search)
    return <Navigate to="/login" replace />
  }

  return <>{children}</>
}
