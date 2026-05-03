import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { authService } from '../services/auth.service'
import {
  recuperarSenhaStep2Schema,
  RecuperarSenhaStep2Data,
} from '../schemas/recuperar-senha.schema'
import { ApiError } from '@/lib/errors'
import { Button } from '@/components/ui/Button'
import { PasswordInput } from '../components/PasswordInput'
import { PasswordStrengthIndicator } from '../components/PasswordStrengthIndicator'
import { ErrorBanner } from '@/components/feedback/ErrorBanner'

type ErroState = 'expirado' | 'ja-usado' | null

export function RedefinirSenhaPage() {
  const { token = '' } = useParams<{ token: string }>()
  const navigate = useNavigate()
  const [erroState, setErroState] = useState<ErroState>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    watch,
  } = useForm<RecuperarSenhaStep2Data>({
    resolver: zodResolver(recuperarSenhaStep2Schema),
    mode: 'onBlur',
  })

  const senhaAtual = watch('senha', '')

  async function onSubmit(data: RecuperarSenhaStep2Data) {
    setErroState(null)
    try {
      await authService.redefinirSenha(token, { senha: data.senha })
      navigate('/login', {
        replace: true,
        state: { toastMessage: 'Senha redefinida com sucesso!' },
      })
    } catch (error) {
      if (error instanceof ApiError && error.status === 410) {
        setErroState('expirado')
      } else {
        setErroState('ja-usado')
      }
    }
  }

  if (erroState === 'expirado') {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
        <div className="flex w-full flex-col items-center gap-4 text-center md:max-w-md md:rounded-2xl md:border md:border-gray-200 md:bg-white md:p-8 md:shadow-sm">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-yellow-100 text-2xl">
            ⏰
          </div>
          <h1 className="text-xl font-semibold text-gray-900">Link expirado</h1>
          <p className="text-sm text-gray-600">
            Este link expirou. Solicite um novo link de recuperação.
          </p>
          <Link to="/recuperar-senha" className="text-sm text-blue-600 hover:underline">
            Solicitar novo link
          </Link>
        </div>
      </div>
    )
  }

  if (erroState === 'ja-usado') {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
        <div className="flex w-full flex-col items-center gap-4 text-center md:max-w-md md:rounded-2xl md:border md:border-gray-200 md:bg-white md:p-8 md:shadow-sm">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-blue-100 text-2xl">
            ✓
          </div>
          <h1 className="text-xl font-semibold text-gray-900">Link já utilizado</h1>
          <p className="text-sm text-gray-600">
            Este link já foi utilizado. Acesse pelo login.
          </p>
          <Link to="/login" className="text-sm text-blue-600 hover:underline">
            Ir para o login
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <div className="w-full md:max-w-md md:rounded-2xl md:border md:border-gray-200 md:bg-white md:p-8 md:shadow-sm">
        <div className="flex flex-col gap-6">
          <div className="flex flex-col gap-2">
            <h1 className="text-xl font-semibold text-gray-900">Redefinir senha</h1>
            <p className="text-sm text-gray-500">Escolha uma nova senha para sua conta.</p>
          </div>

          <form className="flex flex-col gap-4" onSubmit={handleSubmit(onSubmit)}>
            <div className="flex flex-col gap-1.5">
              <label htmlFor="senha" className="text-sm font-medium text-gray-700">
                Nova senha
              </label>
              <PasswordInput
                id="senha"
                autoComplete="new-password"
                placeholder="Mínimo 8 caracteres"
                error={errors.senha?.message}
                autoFocus
                {...register('senha')}
              />
              <PasswordStrengthIndicator senha={senhaAtual} />
            </div>

            <div className="flex flex-col gap-1.5">
              <label htmlFor="confirmarSenha" className="text-sm font-medium text-gray-700">
                Confirmar nova senha
              </label>
              <PasswordInput
                id="confirmarSenha"
                autoComplete="new-password"
                placeholder="Repita a nova senha"
                error={errors.confirmarSenha?.message}
                {...register('confirmarSenha')}
              />
            </div>

            <Button
              type="submit"
              className="w-full mt-2"
              loading={isSubmitting}
              disabled={isSubmitting}
            >
              Redefinir senha
            </Button>
          </form>

          <Link to="/login" className="text-center text-sm text-blue-600 hover:underline">
            Voltar ao login
          </Link>
        </div>
      </div>
    </div>
  )
}
