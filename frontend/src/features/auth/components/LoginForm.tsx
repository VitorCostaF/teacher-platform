import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { PasswordInput } from './PasswordInput'

export function LoginForm() {
  return (
    <div className="flex flex-col items-center gap-6">
      <div className="flex flex-col items-center gap-2">
        <img src="/logo.svg" alt="Teacher Platform" className="h-10 w-auto" />
        <h1 className="text-xl font-semibold text-gray-900">Entrar na plataforma</h1>
      </div>

      <form className="flex w-full flex-col gap-4" onSubmit={(e) => e.preventDefault()}>
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
          />
        </div>

        <div className="flex flex-col gap-1.5">
          <label htmlFor="password" className="text-sm font-medium text-gray-700">
            Senha
          </label>
          <PasswordInput id="password" autoComplete="current-password" placeholder="••••••••" />
        </div>

        <Button type="submit" className="w-full mt-2">
          Entrar
        </Button>
      </form>

      <Link
        to="/recuperar-senha"
        className="text-sm text-blue-600 hover:underline"
      >
        Esqueci minha senha
      </Link>
    </div>
  )
}
