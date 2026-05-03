import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { ErrorBanner } from '@/components/feedback/ErrorBanner'
import { PasswordInput } from './PasswordInput'
import { loginSchema, LoginFormData } from '../schemas/login.schema'
import { useLogin } from '../hooks/useLogin'

export function LoginForm() {
  const { register, handleSubmit, formState: { errors } } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    mode: 'onBlur',
  })

  const { login, isLoading, errorMessage, isLocked, unlockTime, clearError } = useLogin()

  const isDisabled = isLoading || isLocked

  const lockedMessage = unlockTime
    ? `Conta bloqueada até ${unlockTime.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}`
    : 'Conta temporariamente bloqueada'

  return (
    <div className="flex flex-col items-center gap-6">
      <div className="flex flex-col items-center gap-2">
        <img src="/logo.svg" alt="Teacher Platform" className="h-10 w-auto" />
        <h1 className="text-xl font-semibold text-gray-900">Entrar na plataforma</h1>
      </div>

      <form className="flex w-full flex-col gap-4" onSubmit={handleSubmit(login)}>
        <div className="flex flex-col gap-1.5">
          <label htmlFor="email" className="text-sm font-medium text-gray-700">
            E-mail
          </label>
          <Input
            id="email"
            type="email"
            autoComplete="email"
            autoFocus
            placeholder="seu@email.com"
            error={errors.email?.message}
            disabled={isDisabled}
            {...register('email')}
          />
        </div>

        <div className="flex flex-col gap-1.5">
          <label htmlFor="password" className="text-sm font-medium text-gray-700">
            Senha
          </label>
          <PasswordInput
            id="password"
            autoComplete="current-password"
            placeholder="••••••••"
            error={errors.senha?.message}
            disabled={isDisabled}
            {...register('senha')}
          />
        </div>

        {isLocked && (
          <ErrorBanner variant="warning" message={lockedMessage} />
        )}

        {errorMessage && !isLocked && (
          <ErrorBanner variant="error" message={errorMessage} onDismiss={clearError} />
        )}

        <Button type="submit" className="w-full mt-2" loading={isLoading} disabled={isDisabled}>
          Entrar
        </Button>
      </form>

      <Link to="/recuperar-senha" className="text-sm text-blue-600 hover:underline">
        Esqueci minha senha
      </Link>
    </div>
  )
}
