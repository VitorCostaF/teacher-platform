# Turmas — Tela de Detalhe da Turma e Gestão de Alunos

> **Escopo:** turmas  
> **Tipo:** Frontend  
> **Complexidade estimada:** G  
> **Depende de:** `turmas_lista-e-cards.md`, `backend-turmas_endpoint-alunos.md`

---

## Contexto

Após clicar em uma turma, o professor acessa esta tela com abas: Alunos, Frequência, Atividades e Desempenho. Esta task cobre a aba "Alunos" com todas as operações de gestão.

---

## O que deve ser implementado

- Rota `/professor/turmas/:turmaId` com abas de navegação
- Aba "Alunos": lista de alunos (foto, nome, e-mail) com skeleton de carregamento
- Botão "Adicionar Aluno": abre modal de busca por nome/e-mail ou convite por e-mail
- Botão "Importar via planilha": upload de CSV/XLSX, exibir preview com erros antes de confirmar
- Ação "Remover da turma" por aluno: modal de confirmação com descrição das consequências
- Estado vazio da aba: ilustração + CTA "Adicionar primeiro aluno"
- Botão "Lançar Frequência" destacado no header

---

## Critérios de Aceite

- [ ] Abas navegam sem recarregar a página
- [ ] Lista de alunos carrega com skeleton e exibe foto, nome, e-mail
- [ ] Modal de adicionar aluno busca pelo backend ao digitar (debounce 300ms)
- [ ] Upload de planilha exibe lista de erros por linha antes de confirmar
- [ ] Modal de remoção exibe consequências e requer confirmação explícita
- [ ] Estado vazio da aba é exibido quando não há alunos
- [ ] Botão "Lançar Frequência" navega para a tela correta

---

## Especificação de Referência

- **Arquivo:** `02-area-professor/02a-gestao-turmas.md`
- **Seção:** `Tela: Detalhes da Turma`

---

## Notas e Edge Cases

- Importação de planilha: aceitar apenas `.csv` e `.xlsx` (validar no frontend antes de enviar)
- Busca de aluno no modal deve buscar apenas alunos da mesma escola que ainda não estão na turma

---

## Definition of Done

- [ ] Código implementado e funcionando conforme critérios de aceite
- [ ] Testes dos modais de adicionar e remover
- [ ] Testado com planilhas válidas e inválidas
- [ ] Code review realizado
