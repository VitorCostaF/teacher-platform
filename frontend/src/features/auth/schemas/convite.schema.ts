import { z } from 'zod'

export const conviteSchema = z
  .object({
    nome: z.string().min(3, 'Nome deve ter entre 3 e 100 caracteres').max(100),
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

export type ConviteFormData = z.infer<typeof conviteSchema>
