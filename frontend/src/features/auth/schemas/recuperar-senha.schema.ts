import { z } from 'zod'

export const recuperarSenhaStep1Schema = z.object({
  email: z.string().min(1, 'E-mail é obrigatório').email('Formato de e-mail inválido'),
})

export const recuperarSenhaStep2Schema = z
  .object({
    senha: z
      .string()
      .min(8, 'Mínimo de 8 caracteres')
      .regex(/[0-9]/, 'Deve conter pelo menos 1 número')
      .regex(/[A-Z]/, 'Deve conter pelo menos 1 letra maiúscula'),
    confirmarSenha: z.string(),
  })
  .refine((d) => d.senha === d.confirmarSenha, {
    message: 'As senhas não coincidem',
    path: ['confirmarSenha'],
  })

export type RecuperarSenhaStep1Data = z.infer<typeof recuperarSenhaStep1Schema>
export type RecuperarSenhaStep2Data = z.infer<typeof recuperarSenhaStep2Schema>
