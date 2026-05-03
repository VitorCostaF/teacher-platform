import { useEffect } from 'react'
import { useLocation } from 'react-router-dom'
import { LoginForm } from '../components/LoginForm'
import { useToast } from '@/components/feedback/ToastProvider'

export function LoginPage() {
  const location = useLocation()
  const { addToast } = useToast()

  useEffect(() => {
    const state = location.state as { toastMessage?: string } | null
    if (state?.toastMessage) {
      addToast(state.toastMessage, 'success')
      window.history.replaceState(null, '')
    }
  }, [])

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <div className="w-full md:max-w-md md:rounded-2xl md:border md:border-gray-200 md:bg-white md:p-8 md:shadow-sm">
        <LoginForm />
      </div>
    </div>
  )
}
