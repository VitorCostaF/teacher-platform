import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ToastProvider } from '@/components/feedback/ToastProvider'
import { LoadingBarProvider } from '@/components/feedback/LoadingBarProvider'
import { setupAuthInterceptor } from '@/lib/authInterceptor'
import { router } from '@/router'
import './index.css'

setupAuthInterceptor()

const queryClient = new QueryClient()

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
        <LoadingBarProvider>
          <RouterProvider router={router} />
        </LoadingBarProvider>
      </ToastProvider>
    </QueryClientProvider>
  </StrictMode>,
)
