# Área do Responsável — Painel, Boletim e Frequência

> **Escopo:** responsavel  
> **Tipo:** Frontend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-responsavel_endpoints.md`

---

## Contexto

O responsável acessa a plataforma para acompanhar o desempenho e frequência do filho. Pode ter múltiplos filhos vinculados. Acesso de leitura apenas.

---

## O que deve ser implementado

**Painel (`/responsavel/acompanhamento`):**
- Dropdown para selecionar aluno (se múltiplos filhos)
- Cards de resumo: média geral, % frequência, próxima prova, alertas ativos
- Seção de alertas recentes (clicáveis)
- Acesso rápido: boletim, frequência, calendário

**Boletim (`/responsavel/alunos/:alunoId/boletim`):**
- Seletor de período (bimestre/semestre/ano)
- Tabela com disciplinas, notas por período, média final e situação
- Toggle para gráfico de evolução por disciplina

**Frequência (`/responsavel/alunos/:alunoId/frequencia`):**
- Resumo numérico e barra de progresso com linha de risco em 75%
- Calendário mensal com dias coloridos por status
- Filtro por disciplina
- Lista detalhada de faltas
- Banner de alerta se frequência < 75%

**Calendário (`/responsavel/alunos/:alunoId/calendario`):**
- Calendário com provas marcadas
- Lista de provas futuras e histórico com nota

---

## Critérios de Aceite

- [ ] Dropdown de aluno só aparece quando há mais de um filho vinculado
- [ ] Cards de painel exibem dados atualizados
- [ ] Boletim: células vazias para bimestres futuros
- [ ] Frequência: barra de progresso com linha vermelha em 75%
- [ ] Calendário de frequência: cores corretas por status
- [ ] Banner de risco exibido quando frequência < 75%
- [ ] Responsável não tem acesso a nenhuma tela de edição

---

## Especificação de Referência

- **Arquivo:** `04-area-responsavel.md`

---

## Definition of Done

- [ ] Código implementado e funcionando
- [ ] Testado em mobile (foco do responsável)
- [ ] Code review realizado
