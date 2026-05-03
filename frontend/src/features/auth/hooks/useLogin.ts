import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import { authService } from '../services/auth.service'
import { setAuth } from '@/store/authStore'
import { useToast } from '@/components/feedback/ToastProvider'
import { ROTAS_POR_PERFIL } from '../utils/profileRoutes'
import { useProactiveRefresh } from './useProactiveRefresh'
import type { LoginFormData } from '../schemas/login.schema'

export function useLogin() {
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isLocked, setIsLocked] = useState(false)
  const [unlockTime, setUnlockTime] = useState<Date | null>(null)
  const navigate = useNavigate()
  const { addToast } = useToast()
  const { scheduleRefresh } = useProactiveRefresh()

  async function login(data: LoginFormData) {
    setIsLoading(true)
    setErrorMessage(null)
    setIsLocked(false)
    setUnlockTime(null)

    try {
      const response = await authService.login(data)
      setAuth(response.accessToken, response.usuario, response.expiresIn)
      scheduleRefresh(response.expiresIn)

      const redirectUrl = sessionStorage.getItem('redirect_url')
      if (redirectUrl) {
        sessionStorage.removeItem('redirect_url')
        navigate(redirectUrl, { replace: true })
        return
      }

      const rota = ROTAS_POR_PERFIL[response.perfil]
      if (rota) {
        navigate(rota, { replace: true })
      } else {
        console.warn(`Perfil desconhecido: ${response.perfil}`)
        navigate('/', { replace: true })
      }
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const status = error.response?.status
        if (status === 401) {
          setErrorMessage('E-mail ou senha incorretos.')
        } else if (status === 423) {
          setIsLocked(true)
          const desbloqueiaEm = error.response?.data?.desbloqueiaEm
          if (desbloqueiaEm) {
            setUnlockTime(new Date(desbloqueiaEm))
          }
        } else {
          addToast('Não foi possível conectar. Tente novamente.', 'error')
        }
      } else {
        addToast('Não foi possível conectar. Tente novamente.', 'error')
      }
    } finally {
      setIsLoading(false)
    }
  }

  return {
    login,
    isLoading,
    errorMessage,
    isLocked,
    unlockTime,
    clearError: () => setErrorMessage(null),
  }
}
