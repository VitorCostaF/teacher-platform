import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ToastProvider } from '@/components/feedback/ToastProvider'
import { LoadingBarProvider } from '@/components/feedback/LoadingBarProvider'
import { OfflineBanner } from '@/components/feedback/OfflineBanner'
import { setupAuthInterceptor } from '@/lib/authInterceptor'
import { registerPWA } from '@/lib/pwaRegister'
import { useOfflineSync } from '@/hooks/useOfflineSync'
import { router } from '@/router'
import './index.css'

setupAuthInterceptor()
registerPWA()

const queryClient = new QueryClient()

function AppProviders() {
  useOfflineSync()
  return (
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
        <LoadingBarProvider>
          <OfflineBanner />
          <RouterProvider router={router} />
        </LoadingBarProvider>
      </ToastProvider>
    </QueryClientProvider>
  )
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AppProviders />
  </StrictMode>,
)
