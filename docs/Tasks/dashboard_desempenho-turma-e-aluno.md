# Dashboard — Desempenho por Turma e Perfil do Aluno

> **Escopo:** dashboard  
> **Tipo:** Frontend  
> **Complexidade estimada:** G  
> **Depende de:** `backend-dashboard_endpoint-professor.md`, `turmas_detalhe-e-alunos.md`

---

## Contexto

Duas telas detalhadas de desempenho: uma por turma (com distribuição de notas e ranking) e uma por aluno individual (com evolução, frequência e análise por tópico). Usadas por professor, coordenador e responsável (com permissões diferentes).

---

## O que deve ser implementado

**Desempenho por turma (`/professor/turmas/:turmaId/desempenho`):**
- Cards de métricas: média geral, maior/menor nota, % aprovação, % frequência
- Histograma de distribuição de notas (faixas 0-2, 2-4, 4-6, 6-8, 8-10) — colunas clicáveis listam alunos
- Tabela de alunos ordenável por nome/média/frequência/tendência; linhas em risco com fundo vermelho suave
- Coluna tendência com ícone ↑ ↓
- Linha do tempo de avaliações com média da turma e % de entrega

**Perfil do aluno (`/professor/turmas/:turmaId/alunos/:alunoId/desempenho`):**
- Resumo com situação: "Aprovado em andamento" / "Em risco" / "Reprovado por falta"
- Gráfico de linha de evolução de notas com linha de referência em 5,0
- Barras mensais de frequência com linha de referência em 75%
- Lista de tópicos por % de acerto (crescente)
- Tabela de avaliações com posição na turma
- Campo de observações privadas do professor (com histórico)
- Exportar perfil como PDF

---

## Critérios de Aceite

- [ ] Histograma clicável lista alunos da faixa
- [ ] Tabela de ranking é ordenável por todas as colunas
- [ ] Alunos em risco têm destaque visual
- [ ] Gráfico de evolução do aluno mostra linha de referência em 5.0
- [ ] Frequência mostra linha de referência em 75%
- [ ] Banner de risco exibido quando frequência < 75%
- [ ] Observações são salvas e listadas com data
- [ ] Responsável não vê observações privadas (validar permissão)

---

## Especificação de Referência

- **Arquivo:** `02-area-professor/02c-dashboard-desempenho.md`
- **Seções:** `Tela: Desempenho por Turma` e `Tela: Perfil de Desempenho do Aluno`

---

## Definition of Done

- [ ] Código implementado e funcionando
- [ ] Testado com diferentes cenários de desempenho
- [ ] Exportação PDF testada
- [ ] Code review realizado
