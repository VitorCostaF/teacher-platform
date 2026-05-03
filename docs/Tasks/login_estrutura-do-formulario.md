# Tela de Login — Estrutura e Layout do Formulário

> **Escopo:** login  
> **Tipo:** Frontend  
> **Complexidade estimada:** P  
> **Depende de:** Nenhuma

---

## Contexto

A tela de login é o ponto de entrada da plataforma para todos os perfis de usuário (professor, aluno, responsável, coordenador, admin). Ela deve ser simples, acessível e funcionar bem em desktop e mobile.

---

## O que deve ser implementado

- Criar a rota `/login` na aplicação
- Implementar o layout centralizado com: logo da plataforma no topo, formulário com campo de e-mail (`type=email`, `autocomplete=email`), campo de senha (`type=password`), botão "Entrar" (primário, largura total) e link "Esqueci minha senha"
- Campo de senha deve ter ícone de olho para toggle de visibilidade (alterna entre `type=password` e `type=text`)
- Se o usuário já estiver autenticado ao acessar `/login`, redirecionar automaticamente para o dashboard do seu perfil
- Layout responsivo: centralizado em desktop, fullscreen em mobile

---

## Critérios de Aceite

- [ ] Rota `/login` existe e é acessível sem autenticação
- [ ] Usuário autenticado é redirecionado ao acessar `/login`
- [ ] Campo de e-mail usa `type=email` e `autocomplete=email`
- [ ] Campo de senha usa `type=password` com toggle de visibilidade funcional
- [ ] Botão "Entrar" ocupa largura total do formulário
- [ ] Link "Esqueci minha senha" navega para `/recuperar-senha`
- [ ] Layout funciona corretamente em mobile (375px) e desktop (1280px)

---

## Especificação de Referência

- **Arquivo:** `01-autenticacao.md`
- **Seção:** `Tela: Login > Layout e Componentes`

---

## Detalhes Técnicos

**Estados a implementar nesta task (apenas visual, sem lógica):**

| Estado | Descrição |
|--------|-----------|
| Padrão | Formulário vazio, botão habilitado |

Os demais estados (loading, erro) são cobertos em tasks separadas.

---

## Notas e Edge Cases

- Não usar `localStorage` para armazenar nenhum dado de sessão
- O logo deve ter `alt` descritivo para acessibilidade
- O foco deve ir automaticamente para o campo de e-mail ao carregar a página

---

## Definition of Done

- [ ] Código implementado e funcionando conforme critérios de aceite
- [ ] Testes unitários escritos para a lógica principal
- [ ] Sem erros no console / logs
- [ ] Revisado por pelo menos um colega (code review)
- [ ] Testado em Chrome, Firefox e Safari (desktop e mobile)
