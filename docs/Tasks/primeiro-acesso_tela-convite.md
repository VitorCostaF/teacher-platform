# Tela de Primeiro Acesso — Ativação de Conta por Convite

> **Escopo:** primeiro-acesso  
> **Tipo:** Frontend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-auth_endpoint-convite.md`

---

## Contexto

Quando um aluno ou responsável é cadastrado pela escola, recebe um e-mail com link de convite. Ao clicar, chega nesta tela para definir sua senha e ativar a conta. O token no link tem validade de 72 horas.

---

## O que deve ser implementado

- Criar rota `/convite/:token`
- Ao montar a tela, chamar `GET /auth/convite/:token` para validar o token
- Se token válido: exibir formulário com nome pré-preenchido (editável), campo nova senha com indicador de força, campo confirmar senha e botão "Criar minha conta"
- Se token expirado: exibir apenas mensagem "Este link expirou. Solicite um novo à sua escola." sem formulário
- Se token já usado: exibir mensagem "Você já ativou sua conta. Acesse pelo login." com link para `/login`
- Ao submeter com sucesso: armazenar tokens e redirecionar conforme perfil (mesmo fluxo do login)

---

## Critérios de Aceite

- [ ] Ao acessar com token válido, nome do campo vem pré-preenchido da API
- [ ] Token expirado exibe mensagem correta sem formulário
- [ ] Token já usado exibe mensagem correta com link para login
- [ ] Indicador de força de senha é exibido em tempo real durante digitação
- [ ] Campo confirmar senha valida igualdade com campo senha ao perder foco
- [ ] Após ativação bem-sucedida, usuário é redirecionado conforme seu perfil
- [ ] Tela exibe loading (skeleton) enquanto valida o token

---

## Especificação de Referência

- **Arquivo:** `01-autenticacao.md`
- **Seção:** `Tela: Primeiro Acesso (Convite)`

---

## Detalhes Técnicos

**Validações:**
| Campo | Regra | Mensagem de erro |
|-------|-------|-----------------|
| nome | 3–100 caracteres | "Nome deve ter entre 3 e 100 caracteres" |
| senha | Mín 8 chars, 1 número, 1 maiúscula | "Senha não atende aos requisitos" |
| confirmar_senha | Idêntica ao campo senha | "As senhas não coincidem" |

**Chamadas de API:**
| Método | Endpoint | Quando |
|--------|----------|--------|
| GET | /auth/convite/:token | Ao montar a tela |
| POST | /auth/convite/:token/ativar | Ao submeter o formulário |

---

## Notas e Edge Cases

- Se a rede cair durante a validação do token, exibir estado de erro com botão "Tentar novamente"
- O indicador de força de senha pode seguir escala: Fraca / Média / Forte

---

## Definition of Done

- [ ] Código implementado e funcionando conforme critérios de aceite
- [ ] Testes unitários para os 3 estados do token
- [ ] Sem erros no console / logs
- [ ] Revisado por pelo menos um colega (code review)
- [ ] Testado em mobile e desktop
