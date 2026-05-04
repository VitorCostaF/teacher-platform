import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom'
import { LoginPage } from '@/features/auth/pages/LoginPage'
import { ConvitePage } from '@/features/auth/pages/ConvitePage'
import { RecuperarSenhaPage } from '@/features/auth/pages/RecuperarSenhaPage'
import { RedefinirSenhaPage } from '@/features/auth/pages/RedefinirSenhaPage'
import { ProtectedRoute } from '@/components/auth/ProtectedRoute'
import { useAuthBroadcast } from '@/hooks/useAuthBroadcast'
import { TurmasPage } from '@/features/turmas/pages/TurmasPage'
import { TurmaDetalhePage } from '@/features/turmas/pages/TurmaDetalhePage'
import { LancamentoFrequenciaPage } from '@/features/frequencia/pages/LancamentoFrequenciaPage'
import { GeradorProvasPage } from '@/features/criacao-ia/pages/GeradorProvasPage'
import { GeradorGradePage } from '@/features/criacao-ia/pages/GeradorGradePage'
import { SugestoesConteudoPage } from '@/features/criacao-ia/pages/SugestoesConteudoPage'

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
      { path: '/recuperar-senha', element: <RecuperarSenhaPage /> },
      { path: '/recuperar-senha/:token', element: <RedefinirSenhaPage /> },
      {
        path: '/professor/turmas',
        element: <ProtectedRoute><TurmasPage /></ProtectedRoute>,
      },
      {
        path: '/professor/turmas/:turmaId',
        element: <ProtectedRoute><TurmaDetalhePage /></ProtectedRoute>,
      },
      {
        path: '/professor/turmas/:turmaId/frequencia',
        element: <ProtectedRoute><LancamentoFrequenciaPage /></ProtectedRoute>,
      },
      {
        path: '/professor/criar/prova',
        element: <ProtectedRoute><GeradorProvasPage /></ProtectedRoute>,
      },
      {
        path: '/professor/criar/grade',
        element: <ProtectedRoute><GeradorGradePage /></ProtectedRoute>,
      },
      {
        path: '/professor/criar/sugestoes',
        element: <ProtectedRoute><SugestoesConteudoPage /></ProtectedRoute>,
      },
      {
        path: '/professor/turmas/:turmaId/atividades',
        element: <ProtectedRoute><TurmaDetalhePage /></ProtectedRoute>,
      },
      {
        path: '/professor/turmas/:turmaId/desempenho',
        element: <ProtectedRoute><TurmaDetalhePage /></ProtectedRoute>,
      },
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
