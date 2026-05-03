import { createBrowserRouter, Navigate } from 'react-router-dom'
import { LoginPage } from '@/features/auth/pages/LoginPage'
import { ProtectedRoute } from '@/components/auth/ProtectedRoute'

export const router = createBrowserRouter([
  { path: '/', element: <Navigate to="/login" replace /> },
  { path: '/login', element: <LoginPage /> },
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
])
