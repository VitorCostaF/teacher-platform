import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { authService } from '../services/auth.service'
import { setAuth } from '@/store/authStore'
import { ROTAS_POR_PERFIL } from '../utils/profileRoutes'
import { useProactiveRefresh } from '../hooks/useProactiveRefresh'
import { conviteSchema, ConviteFormData } from '../schemas/convite.schema'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Skeleton } from '@/components/feedback/Skeleton'
import { ErrorBanner } from '@/components/feedback/ErrorBanner'
import { PasswordInput } from '../components/PasswordInput'
import { PasswordStrengthIndicator } from '../components/PasswordStrengthIndicator'
import type { ConviteTokenResponse } from '../services/auth.service'

type PageState = 'loading' | 'network-error' | 'VALIDO' | 'EXPIRADO' | 'JA_USADO'

export function ConvitePage() {
  const { token = '' } = useParams<{ token: string }>()
  const navigate = useNavigate()
  const { scheduleRefresh } = useProactiveRefresh()

  const [pageState, setPageState] = useState<PageState>('loading')
  const [convite, setConvite] = useState<ConviteTokenResponse | null>(null)
  const [submitError, setSubmitError] = useState<string | null>(null)

  const { register, handleSubmit, formState: { errors, isSubmitting }, reset, watch } =
    useForm<ConviteFormData>({
      resolver: zodResolver(conviteSchema),
      mode: 'onBlur',
    })

  const senhaAtual = watch('senha', '')

  async function validar() {
    setPageState('loading')
    try {
      const data = await authService.validarTokenConvite(token)
      setConvite(data)
      setPageState(data.status)
      if (data.status === 'VALIDO') {
        reset({ nome: data.nome, senha: '', confirmarSenha: '' })
      }
    } catch {
      setPageState('network-error')
    }
  }

  useEffect(() => {
    validar()
  }, [token])

  async function onSubmit(data: ConviteFormData) {
    setSubmitError(null)
    try {
      const response = await authService.ativarConta(token, {
        nome: data.nome,
        senha: data.senha,
      })
      setAuth(response.accessToken, response.usuario, response.expiresIn)
      scheduleRefresh(response.expiresIn)
      const rota = ROTAS_POR_PERFIL[response.perfil]
      navigate(rota ?? '/', { replace: true })
    } catch {
      setSubmitError('Não foi possível ativar a conta. Tente novamente.')
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <div className="w-full md:max-w-md md:rounded-2xl md:border md:border-gray-200 md:bg-white md:p-8 md:shadow-sm">

        {pageState === 'loading' && (
          <div className="flex flex-col gap-4">
            <Skeleton className="h-8 w-48 mx-auto" />
            <Skeleton className="h-4 w-64 mx-auto" />
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        )}

        {pageState === 'network-error' && (
          <div className="flex flex-col gap-4">
            <h1 className="text-xl font-semibold text-gray-900 text-center">
              Erro de conexão
            </h1>
            <ErrorBanner
              variant="error"
              message="Não foi possível verificar o convite. Verifique sua conexão."
            />
            <Button type="button" onClick={validar} className="w-full">
              Tentar novamente
            </Button>
          </div>
        )}

        {pageState === 'EXPIRADO' && (
          <div className="flex flex-col items-center gap-4 text-center">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-yellow-100 text-2xl">
              ⏰
            </div>
            <h1 className="text-xl font-semibold text-gray-900">Link expirado</h1>
            <p className="text-sm text-gray-600">
              Este link expirou. Solicite um novo convite à sua escola.
            </p>
          </div>
        )}

        {pageState === 'JA_USADO' && (
          <div className="flex flex-col items-center gap-4 text-center">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-blue-100 text-2xl">
              ✓
            </div>
            <h1 className="text-xl font-semibold text-gray-900">Conta já ativada</h1>
            <p className="text-sm text-gray-600">
              Este link já foi utilizado. Faça login para acessar a plataforma.
            </p>
            <Link to="/login" className="text-sm text-blue-600 hover:underline">
              Ir para o login
            </Link>
          </div>
        )}

        {pageState === 'VALIDO' && convite && (
          <div className="flex flex-col gap-6">
            <div className="flex flex-col items-center gap-2">
              <img src="/logo.svg" alt="Teacher Platform" className="h-10 w-auto" />
              <h1 className="text-xl font-semibold text-gray-900">Ative sua conta</h1>
              <p className="text-sm text-gray-500">{convite.email}</p>
            </div>

            <form className="flex flex-col gap-4" onSubmit={handleSubmit(onSubmit)}>
              <div className="flex flex-col gap-1.5">
                <label htmlFor="nome" className="text-sm font-medium text-gray-700">
                  Nome completo
                </label>
                <Input
                  id="nome"
                  type="text"
                  autoComplete="name"
                  placeholder="Seu nome completo"
                  error={errors.nome?.message}
                  {...register('nome')}
                />
              </div>

              <div className="flex flex-col gap-1.5">
                <label htmlFor="senha" className="text-sm font-medium text-gray-700">
                  Senha
                </label>
                <PasswordInput
                  id="senha"
                  autoComplete="new-password"
                  placeholder="Mínimo 8 caracteres"
                  error={errors.senha?.message}
                  {...register('senha')}
                />
                <PasswordStrengthIndicator senha={senhaAtual} />
              </div>

              <div className="flex flex-col gap-1.5">
                <label htmlFor="confirmarSenha" className="text-sm font-medium text-gray-700">
                  Confirmar senha
                </label>
                <PasswordInput
                  id="confirmarSenha"
                  autoComplete="new-password"
                  placeholder="Repita a senha"
                  error={errors.confirmarSenha?.message}
                  {...register('confirmarSenha')}
                />
              </div>

              {submitError && (
                <ErrorBanner
                  variant="error"
                  message={submitError}
                  onDismiss={() => setSubmitError(null)}
                />
              )}

              <Button type="submit" className="w-full mt-2" loading={isSubmitting} disabled={isSubmitting}>
                Ativar conta
              </Button>
            </form>
          </div>
        )}
      </div>
    </div>
  )
}
