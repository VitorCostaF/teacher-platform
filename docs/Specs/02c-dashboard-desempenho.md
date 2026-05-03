# Spec 02c — Área do Professor: Dashboard de Desempenho

> **Macro funcionalidade:** Acompanhamento gráfico de desempenho de alunos em provas e frequência  
> **Perfis envolvidos:** Professor, Coordenador, Admin  
> **Plataforma:** Web (desktop-first)

---

## Telas desta Spec

1. [Dashboard Geral do Professor](#tela-dashboard-geral)
2. [Desempenho por Turma](#tela-desempenho-por-turma)
3. [Perfil de Desempenho do Aluno](#tela-perfil-do-aluno)
4. [Relatório Exportável](#tela-relatório)

---

## Tela: Dashboard Geral do Professor

> **Rota:** `/professor/dashboard`  
> **Autenticação:** Requerida  
> **Perfis com acesso:** Professor

### Contexto

Tela inicial do professor após login. Oferece uma visão consolidada de todas as suas turmas e alertas pendentes.

### Layout e Componentes

**Seção 1 — Alertas e Pendências**
- Cards de alerta (fundo amarelo/vermelho):
  - "X alunos em risco de reprovação por falta"
  - "Y atividades aguardam correção"
  - "Z provas encerradas sem gabarito liberado"
- Cada alerta é clicável e leva direto para a ação

**Seção 2 — Visão Geral por Turma**
- Tabela com uma linha por turma:
  - Nome da turma | Média da turma | % de frequência | Alunos em alerta | Última atividade
- Colunas clicáveis abrem o detalhamento da turma

**Seção 3 — Gráfico de Evolução de Médias**
- Linha do tempo com média por turma ao longo das avaliações
- Seletor de período: último mês | bimestre | semestre | ano

**Seção 4 — Mapa de Calor de Dificuldades**
- Grid: eixo Y = tópicos de conteúdo, eixo X = turmas
- Cor indica % de erros: verde (< 30%) | amarelo (30-60%) | vermelho (> 60%)
- Hover no quadrado exibe: "62% de erros em Frações — 9º Ano A"

### Estados da Tela

| Estado | Descrição |
|--------|-----------|
| **Carregando** | Skeleton em todos os gráficos |
| **Sem dados** | Professor sem provas publicadas: "Publique sua primeira prova para ver os dados" |
| **Com dados** | Dashboard completo |

### Comportamentos Esperados

- Dados são atualizados em tempo real durante a realização de provas (polling a cada 30s)
- Alertas são descartáveis individualmente (botão X) — mas voltam se a condição persistir no dia seguinte
- Gráficos exportáveis individualmente como PNG

### Chamadas de API

| Método | Endpoint | Resposta esperada |
|--------|----------|-------------------|
| GET | /professor/dashboard | { alertas[], turmas[], mediasHistorico[], mapaCalor } |
| GET | /professor/dashboard?periodo=bimestre | Filtragem por período |

---

## Tela: Desempenho por Turma

> **Rota:** `/professor/turmas/:turmaId/desempenho`  
> **Autenticação:** Requerida  
> **Perfis com acesso:** Professor dono da turma, Coordenador, Admin

### Layout e Componentes

**Header:**
- Nome da turma, disciplina, período
- Seletor de bimestre/período

**Bloco 1 — Métricas da Turma**
- Cards com: Média geral | Maior nota | Menor nota | % de aprovação (≥ 5,0) | % de frequência

**Bloco 2 — Distribuição de Notas**
- Histograma: eixo X = faixas de nota (0-2, 2-4, 4-6, 6-8, 8-10), eixo Y = número de alunos
- Clique na barra lista os alunos naquela faixa

**Bloco 3 — Ranking de Alunos**
- Tabela ordenável por: nome | média | frequência | tendência
- Coluna "Tendência": ícone de seta indicando se o aluno está melhorando ou piorando
- Linha clicável abre o perfil de desempenho do aluno
- Alunos em risco destacados com fundo vermelho suave

**Bloco 4 — Desempenho por Avaliação**
- Linha do tempo com todas as provas e atividades
- Para cada prova: média da turma, % de entrega, data

**Bloco 5 — Mapa de Calor de Tópicos (desta turma)**
- Igual ao do dashboard geral, mas filtrado para a turma

### Chamadas de API

| Método | Endpoint | Resposta esperada |
|--------|----------|-------------------|
| GET | /turmas/:id/desempenho?periodo=:p | { metricas, distribuicao, alunos[], avaliacoes[], mapaCalor } |

---

## Tela: Perfil de Desempenho do Aluno

> **Rota:** `/professor/turmas/:turmaId/alunos/:alunoId/desempenho`  
> **Autenticação:** Requerida  
> **Perfis com acesso:** Professor, Coordenador, Admin, Responsável (read-only, rota diferente)

### Layout e Componentes

**Header:**
- Foto, nome e e-mail do aluno
- Turma e período

**Bloco 1 — Resumo**
- Média geral | Frequência | Situação: "Aprovado em andamento" / "Em risco" / "Reprovado por falta"

**Bloco 2 — Evolução de Notas (linha do tempo)**
- Gráfico de linha: eixo X = avaliações, eixo Y = nota (0 a 10)
- Linha de referência em 5,0 (média de aprovação)
- Hover exibe nota, data e nome da avaliação

**Bloco 3 — Frequência Mensal**
- Barras mensais com % de presença
- Linha de referência em 75%

**Bloco 4 — Desempenho por Tópico**
- Lista de tópicos avaliados com % de acerto
- Ordenado do menor para o maior acerto (dificuldades em destaque)

**Bloco 5 — Histórico de Avaliações**
- Tabela: Nome da avaliação | Tipo | Data | Nota | Mediana da turma | Posição na turma

**Bloco 6 — Observações do Professor**
- Campo de texto livre para anotações sobre o aluno (visível apenas para professor e coordenador)
- Histórico de observações com data

### Comportamentos Esperados

- "Posição na turma" é calculada no backend com base na nota da avaliação específica
- Observações são privadas — responsáveis não as veem
- Exportar perfil completo como PDF (para reunião com responsável)

### Chamadas de API

| Método | Endpoint | Dados | Resposta |
|--------|----------|-------|----------|
| GET | /turmas/:id/alunos/:alunoId/desempenho | — | { resumo, evolucao[], frequencia[], topicos[], avaliacoes[] } |
| POST | /turmas/:id/alunos/:alunoId/observacoes | { texto } | { observacaoId, criadaEm } |
| GET | /turmas/:id/alunos/:alunoId/observacoes | — | { observacoes[] } |

---

## Tela: Relatório Exportável

> **Rota:** Gerado via botão "Exportar PDF" nas telas anteriores  
> **Autenticação:** Requerida

### Conteúdo do Relatório por Turma

- Capa com nome da escola, turma, disciplina, professor e data de geração
- Métricas consolidadas
- Gráfico de distribuição de notas
- Tabela de alunos com média e frequência
- Lista de alunos em situação de risco
- Mapa de calor de tópicos

### Conteúdo do Relatório Individual (por aluno)

- Dados do aluno
- Gráfico de evolução de notas
- Gráfico de frequência
- Tabela de avaliações
- Análise de pontos fortes e dificuldades (gerada por IA com base nos dados)

### Chamadas de API

| Método | Endpoint | Dados | Resposta |
|--------|----------|-------|----------|
| POST | /relatorios/turma/:id/pdf | { periodo } | Arquivo PDF (stream ou URL assinada) |
| POST | /relatorios/aluno/:id/pdf | { turmaId, periodo } | Arquivo PDF |

### Comportamentos Esperados

- Geração de PDF é assíncrona: exibir loading e notificar quando pronto (toast + download automático)
- Relatórios gerados ficam disponíveis por 24h via URL assinada (não armazenar indefinidamente)
- A análise de IA no relatório individual deve ser claramente marcada como "Análise gerada por IA — sujeita à revisão do professor"

---

## Regras de Negócio — Desempenho

- Médias são calculadas considerando o peso de cada avaliação configurado pelo professor
- Frequência mínima para aprovação: 75% (configurável pela escola)
- Nota mínima para aprovação: 5,0 (configurável pela escola)
- Aluno é considerado "em risco" quando: nota média < 5,0 OU frequência < 75%
- Tendência é calculada comparando as últimas 3 avaliações com as 3 anteriores
