import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom'
import { LoginPage } from '@/features/auth/pages/LoginPage'
import { ConvitePage } from '@/features/auth/pages/ConvitePage'
import { ProtectedRoute } from '@/components/auth/ProtectedRoute'
import { useAuthBroadcast } from '@/hooks/useAuthBroadcast'

function RootLayout() {
  useAuthBroadcast()
  return <Outlet />
}

export const router = createBrowserRouter([
  {
    element: <RootLayout />,
    children: [
      { path: '/', element: <Navigate to="/login" replace /> },
      { path: '/login', element: <LoginPage /> },
      { path: '/convite/:token', element: <ConvitePage /> },
      {
        path: '/professor/*',
        element: <ProtectedRoute><Navigate to="/professor/dashboard" replace /></ProtectedRoute>,
      },
      {
        path: '/aluno/*',
        element: <ProtectedRoute><Navigate to="/aluno/feed" replace /></ProtectedRoute>,
      },
      {
        path: '/responsavel/*',
        element: <ProtectedRoute><Navigate to="/responsavel/acompanhamento" replace /></ProtectedRoute>,
      },
      {
        path: '/admin/*',
        element: <ProtectedRoute><Navigate to="/admin/visao-geral" replace /></ProtectedRoute>,
      },
    ],
  },
])
