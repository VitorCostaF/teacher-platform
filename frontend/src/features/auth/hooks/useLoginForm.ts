import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { loginSchema, LoginFormData } from '../schemas/login.schema'

export function useLoginForm() {
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isLocked, setIsLocked] = useState(false)
  const [unlockTime, setUnlockTime] = useState<Date | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    mode: 'onBlur',
  })

  async function onSubmit(data: LoginFormData) {
    console.log('Login data:', data)
    // API call será adicionada no próximo plano
  }

  return {
    register,
    handleSubmit: handleSubmit(onSubmit),
    errors,
    isSubmitting,
    errorMessage,
    isLocked,
    unlockTime,
    setErrorMessage,
    setIsLocked,
    setUnlockTime,
    clearError: () => setErrorMessage(null),
  }
}
