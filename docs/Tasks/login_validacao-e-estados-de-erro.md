# Tela de Login — Validação de Campos e Estados de Erro

> **Escopo:** login  
> **Tipo:** Frontend  
> **Complexidade estimada:** M  
> **Depende de:** `login_estrutura-do-formulario.md`

---

## Contexto

O formulário de login precisa validar os campos antes de fazer a requisição ao backend e exibir estados visuais distintos para cada tipo de erro (validação local, credencial inválida, conta bloqueada, erro de servidor).

---

## O que deve ser implementado

**Validação frontend (pré-requisição):**
- E-mail: obrigatório + formato RFC 5322. Validar ao tirar o foco (onBlur) e ao submeter
- Senha: obrigatório, mínimo 1 caractere. Validar ao submeter
- Exibir mensagem de erro inline abaixo de cada campo

**Estado "Submetendo":**
- Botão "Entrar" exibe spinner e texto "Entrando..."
- Campos de e-mail e senha ficam desabilitados (`disabled`)
- Não permitir submit duplo

**Estado "Erro de credencial" (resposta 401):**
- Exibir banner acima do formulário: "E-mail ou senha incorretos."
- Não limpar os campos
- Reabilitar o botão após o erro

**Estado "Conta bloqueada" (resposta 423):**
- Banner: "Conta bloqueada temporariamente. Tente novamente às [horário calculado a partir de `desbloqueiaEm`]"
- Botão fica desabilitado até o horário de desbloqueio

**Estado "Erro de servidor" (500 / timeout):**
- Toast no canto superior direito: "Não foi possível conectar. Verifique sua internet e tente novamente."

---

## Critérios de Aceite

- [ ] Campo de e-mail exibe erro inline ao perder o foco com valor inválido
- [ ] Campo de e-mail exibe erro ao tentar submeter vazio
- [ ] Durante a requisição, botão exibe spinner e campos ficam desabilitados
- [ ] Resposta 401 exibe banner genérico sem limpar os campos
- [ ] Resposta 423 exibe banner com horário de desbloqueio formatado em pt-BR
- [ ] Botão fica desabilitado durante o período de bloqueio do 423
- [ ] Resposta 500 / timeout exibe toast de erro de servidor
- [ ] Após erro, formulário é reabilitado corretamente

---

## Especificação de Referência

- **Arquivo:** `01-autenticacao.md`
- **Seção:** `Tela: Login > Estados da Tela` e `Fluxo: Login > Caminhos de Erro`

---

## Detalhes Técnicos

**Validações:**
| Campo | Regra | Mensagem de erro |
|-------|-------|-----------------|
| email | Obrigatório | "E-mail é obrigatório" |
| email | Formato RFC 5322 | "Formato de e-mail inválido" |
| senha | Obrigatório | "Senha é obrigatória" |

**Estados:**
| Estado | Gatilho | Comportamento visual |
|--------|---------|---------------------|
| Submetendo | Clique em "Entrar" | Spinner no botão, campos `disabled` |
| Erro credencial | 401 | Banner vermelho acima do form |
| Conta bloqueada | 423 | Banner amarelo com timer, botão desabilitado |
| Erro servidor | 500 / timeout | Toast canto superior direito |

---

## Notas e Edge Cases

- O horário de desbloqueio deve ser exibido no fuso horário local do usuário
- Nunca indicar qual campo específico está errado nas credenciais (segurança)
- O banner de erro de credencial deve ser descartável (botão X)

---

## Definition of Done

- [ ] Código implementado e funcionando conforme critérios de aceite
- [ ] Testes unitários escritos para cada estado de erro
- [ ] Sem erros no console / logs
- [ ] Revisado por pelo menos um colega (code review)
- [ ] Testado em Chrome, Firefox e Safari (desktop e mobile)
