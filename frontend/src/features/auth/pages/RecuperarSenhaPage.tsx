import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { authService } from '../services/auth.service'
import {
  recuperarSenhaStep1Schema,
  RecuperarSenhaStep1Data,
} from '../schemas/recuperar-senha.schema'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'

export function RecuperarSenhaPage() {
  const [enviado, setEnviado] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RecuperarSenhaStep1Data>({
    resolver: zodResolver(recuperarSenhaStep1Schema),
    mode: 'onBlur',
  })

  async function onSubmit(data: RecuperarSenhaStep1Data) {
    try {
      await authService.solicitarRecuperacaoSenha(data.email)
    } catch {
      // Ignorar erros para não revelar se o e-mail existe
    } finally {
      setEnviado(true)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <div className="w-full md:max-w-md md:rounded-2xl md:border md:border-gray-200 md:bg-white md:p-8 md:shadow-sm">
        {enviado ? (
          <div className="flex flex-col items-center gap-4 text-center">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-green-100 text-2xl">
              ✉
            </div>
            <h1 className="text-xl font-semibold text-gray-900">Verifique seu e-mail</h1>
            <p className="text-sm text-gray-600">
              Se este e-mail estiver cadastrado, você receberá as instruções em breve.
            </p>
            <Link to="/login" className="text-sm text-blue-600 hover:underline">
              Voltar ao login
            </Link>
          </div>
        ) : (
          <div className="flex flex-col gap-6">
            <div className="flex flex-col gap-2">
              <h1 className="text-xl font-semibold text-gray-900">Recuperar senha</h1>
              <p className="text-sm text-gray-500">
                Informe seu e-mail e enviaremos as instruções para redefinir sua senha.
              </p>
            </div>

            <form className="flex flex-col gap-4" onSubmit={handleSubmit(onSubmit)}>
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
                  {...register('email')}
                />
              </div>

              <Button type="submit" className="w-full" loading={isSubmitting} disabled={isSubmitting}>
                Enviar instruções
              </Button>
            </form>

            <Link to="/login" className="text-center text-sm text-blue-600 hover:underline">
              Voltar ao login
            </Link>
          </div>
        )}
      </div>
    </div>
  )
}
