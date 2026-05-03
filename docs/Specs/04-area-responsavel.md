# Spec 04 — Área do Responsável

> **Macro funcionalidade:** Acompanhamento de desempenho e frequência do aluno vinculado  
> **Perfis envolvidos:** Responsável (pai, mãe ou guardião)  
> **Plataforma:** Mobile-first (PWA)

---

## Telas desta Spec

1. [Painel do Responsável](#tela-painel-do-responsável)
2. [Boletim do Aluno](#tela-boletim)
3. [Frequência do Aluno](#tela-frequência)
4. [Calendário de Provas](#tela-calendário)
5. [Notificações e Alertas](#tela-notificações)

---

## Tela: Painel do Responsável

> **Rota:** `/responsavel/acompanhamento`  
> **Autenticação:** Requerida  
> **Perfis com acesso:** Responsável

### Contexto

Responsável acessa a plataforma para verificar como está o aluno. Um responsável pode estar vinculado a mais de um aluno (múltiplos filhos na mesma escola).

### Layout e Componentes

**Seletor de aluno** — dropdown se houver mais de um aluno vinculado

**Cards de resumo rápido:**
- Média geral do bimestre atual
- % de frequência
- Próxima prova (nome e data)
- Alertas ativos (ex: "3 faltas consecutivas")

**Seção de Alertas Recentes:**
- Lista de notificações não lidas: queda de nota, faltas, prova próxima
- Cada alerta clicável leva à tela correspondente

**Acesso rápido:**
- Ver boletim completo
- Ver histórico de frequência
- Ver calendário de provas

### Estados da Tela

| Estado | Descrição |
|--------|-----------|
| **Carregando** | Skeleton nos cards |
| **Sem alertas** | Cards verdes "Tudo em ordem!" |
| **Com alertas** | Cards em amarelo/vermelho conforme severidade |

### Chamadas de API

| Método | Endpoint | Resposta |
|--------|----------|----------|
| GET | /responsavel/alunos | Lista de alunos vinculados |
| GET | /responsavel/alunos/:alunoId/painel | { resumo, alertas[], proximaProva } |

---

## Tela: Boletim do Aluno

> **Rota:** `/responsavel/alunos/:alunoId/boletim`  
> **Autenticação:** Requerida

### Layout e Componentes

**Seletor de período** — bimestre / semestre / ano

**Tabela de boletim:**
| Disciplina | Professor | B1 | B2 | B3 | B4 | Média Final | Situação |
|------------|-----------|----|----|----|----|-------------|----------|
| Matemática | João Silva | 7,5 | 8,0 | — | — | — | Em andamento |

- Células vazias para bimestres futuros
- Médias finais calculadas automaticamente
- Situação: Em andamento / Aprovado / Reprovado / Recuperação

**Gráfico de evolução por disciplina** — linhas sobrepostas (opcional, toggle para exibir)

### Comportamentos Esperados

- Notas só aparecem após o professor lançar e publicar o resultado
- Responsável não tem acesso às observações privadas do professor
- Boletim exportável como PDF (botão secundário)

### Chamadas de API

| Método | Endpoint | Resposta |
|--------|----------|----------|
| GET | /responsavel/alunos/:alunoId/boletim?periodo=:p | { disciplinas[], situacaoGeral } |

---

## Tela: Frequência do Aluno

> **Rota:** `/responsavel/alunos/:alunoId/frequencia`  
> **Autenticação:** Requerida

### Layout e Componentes

- **Resumo:** Total de aulas | Presenças | Faltas | % de frequência
- **Barra de progresso visual** com linha de risco em 75%
- **Calendário mensal** — dias coloridos por status de presença
- **Filtro por disciplina** — ver frequência específica por matéria
- **Lista de faltas** — data, disciplina, status (justificada / não justificada)

### Comportamentos Esperados

- Responsável não pode alterar registros de frequência
- Faltas justificadas são marcadas pelo professor — responsável vê apenas a marcação, não pode editar
- Se frequência < 75%: banner de alerta "Seu filho está em risco de reprovação por frequência."

### Chamadas de API

| Método | Endpoint | Resposta |
|--------|----------|----------|
| GET | /responsavel/alunos/:alunoId/frequencia | { resumo, calendario[], faltas[] } |

---

## Tela: Calendário de Provas

> **Rota:** `/responsavel/alunos/:alunoId/calendario`  
> **Autenticação:** Requerida

### Layout e Componentes

- **Calendário mensal** — dias com provas marcados
- **Lista de provas futuras:** nome, disciplina, data, duração
- **Histórico de provas passadas** com nota (se disponível)

### Chamadas de API

| Método | Endpoint | Resposta |
|--------|----------|----------|
| GET | /responsavel/alunos/:alunoId/calendario | { futuras[], passadas[] } |

---

## Sistema de Alertas e Notificações

### Tipos de Alerta

| Tipo | Gatilho | Canal | Urgência |
|------|---------|-------|----------|
| Falta simples | Aluno registrado como faltante | Push + e-mail | Informativo |
| Faltas consecutivas | 3+ faltas seguidas | Push + e-mail | Alerta |
| Risco por frequência | Frequência < 75% | Push + e-mail + SMS | Crítico |
| Queda de nota | Média caiu > 1,5 pontos em relação ao bimestre anterior | Push + e-mail | Alerta |
| Prova amanhã | Prova agendada para o dia seguinte | Push | Lembrete |
| Nova comunicado da escola | Admin publicou comunicado | Push + e-mail | Informativo |

### Configurações de Notificação

Responsável pode configurar (em Perfil > Notificações):
- Canais por tipo de alerta (push, e-mail, SMS)
- Horário preferido para receber notificações não urgentes
- Silenciar notificações (com opção de manter apenas os críticos)

### Regras de Negócio — Responsável

- Um responsável pode ser vinculado a múltiplos alunos (irmãos)
- Um aluno pode ter múltiplos responsáveis (pai e mãe separados, por exemplo)
- Responsável vê apenas dados de leitura — não pode editar notas, frequência nem conteúdos
- Vínculo responsável ↔ aluno é gerenciado pelo Admin da Escola
- Dados de menores são tratados com atenção especial à LGPD — consentimento explícito no cadastro
