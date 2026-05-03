# Dashboard do Professor — Visão Geral e Alertas

> **Escopo:** dashboard  
> **Tipo:** Frontend  
> **Complexidade estimada:** G  
> **Depende de:** `backend-dashboard_endpoint-professor.md`

---

## Contexto

Tela inicial do professor após o login. Consolida alertas pendentes, métricas das turmas, gráfico de evolução de médias e mapa de calor de dificuldades por tópico.

---

## O que deve ser implementado

- Rota `/professor/dashboard`
- Seção de Alertas: cards clicáveis (amarelo/vermelho) para alunos em risco de reprovação, atividades para corrigir, provas sem gabarito liberado. Cada alert tem botão X para descartar.
- Tabela de turmas: uma linha por turma com média, % frequência, alunos em alerta, última atividade. Colunas clicáveis.
- Gráfico de linha: evolução de médias por turma com seletor de período (mês/bimestre/semestre/ano)
- Mapa de calor: grid tópicos × turmas com gradiente de cor por % de erros. Hover exibe tooltip com dados.
- Polling a cada 30 segundos para atualização em tempo real (apenas durante provas ativas)
- Skeleton em todos os gráficos durante carregamento

---

## Critérios de Aceite

- [ ] Alertas são clicáveis e levam para a ação correta
- [ ] Alerta descartado com X não reaparece na mesma sessão
- [ ] Gráfico de linha funciona com seletor de período
- [ ] Mapa de calor exibe cores corretas (verde < 30%, amarelo 30-60%, vermelho > 60%)
- [ ] Tooltip do mapa de calor exibe % de erros, tópico e turma
- [ ] Gráficos exportáveis como PNG
- [ ] Estado "sem dados" exibido para professor sem provas publicadas

---

## Especificação de Referência

- **Arquivo:** `02-area-professor/02c-dashboard-desempenho.md`
- **Seção:** `Tela: Dashboard Geral do Professor`

---

## Notas e Edge Cases

- Usar biblioteca de gráficos (Recharts, Chart.js ou similar) para linha e calor
- Mapa de calor pode ter muitos dados — garantir performance com virtualização se necessário

---

## Definition of Done

- [ ] Código implementado e funcionando
- [ ] Testado com dados reais (não apenas mock)
- [ ] Gráficos testados em diferentes resoluções
- [ ] Code review realizado
