import { LoginForm } from '../components/LoginForm'

export function LoginPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      {/* Desktop: card centralizado; Mobile: fullscreen sem card */}
      <div className="w-full md:max-w-md md:rounded-2xl md:border md:border-gray-200 md:bg-white md:p-8 md:shadow-sm">
        <LoginForm />
      </div>
    </div>
  )
}
