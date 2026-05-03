# Spec 05 — Área Administrativa

> **Macro funcionalidade:** Gestão da escola, professores, turmas e visão consolidada de desempenho  
> **Perfis envolvidos:** Admin da Escola, Coordenador Pedagógico  
> **Plataforma:** Web (desktop-first)

---

## Telas desta Spec

1. [Visão Geral da Escola](#tela-visão-geral)
2. [Gestão de Professores](#tela-gestão-de-professores)
3. [Gestão de Alunos](#tela-gestão-de-alunos)
4. [Gestão de Turmas](#tela-gestão-de-turmas-admin)
5. [Relatórios Consolidados](#tela-relatórios-consolidados)
6. [Configurações da Escola](#tela-configurações)

---

## Tela: Visão Geral da Escola

> **Rota:** `/admin/visao-geral`  
> **Autenticação:** Requerida  
> **Perfis com acesso:** Admin, Coordenador

### Layout e Componentes

**KPIs principais (cards no topo):**
- Total de alunos ativos
- Total de professores
- Total de turmas no período ativo
- % médio de frequência da escola
- Média geral de notas da escola

**Gráfico: Desempenho por série**
- Barras agrupadas por série (6º Ano, 7º Ano...) com média de notas por disciplina
- Permite identificar quais séries e disciplinas estão com maior dificuldade

**Gráfico: Frequência por turma**
- Ranking de turmas por % de frequência
- Destaque para turmas abaixo de 75%

**Tabela: Alertas da escola**
- Alunos em risco consolidados (frequência ou nota)
- Filtro por série, turma, disciplina

**Seção: Atividade recente**
- Últimas ações: provas publicadas, comunicados enviados, alunos cadastrados

### Chamadas de API

| Método | Endpoint | Resposta |
|--------|----------|----------|
| GET | /admin/dashboard | { kpis, desempenhoPorSerie[], frequenciaPorTurma[], alertas[], atividadeRecente[] } |

---

## Tela: Gestão de Professores

> **Rota:** `/admin/professores`  
> **Autenticação:** Requerida  
> **Perfis com acesso:** Admin

### Layout e Componentes

- **Barra de ações:** Campo de busca | Botão "Convidar Professor"
- **Tabela de professores:**
  - Nome | E-mail | Disciplinas | Turmas ativas | Status (ativo/inativo) | Ações
- **Modal "Convidar Professor":**
  - Nome, e-mail, disciplinas que leciona
  - Ao salvar: envia e-mail de convite com link de primeiro acesso
- **Modal de detalhes do professor:**
  - Turmas vinculadas (pode desvincular)
  - Histórico de ações na plataforma
  - Botão "Desativar conta"

### Comportamentos Esperados

- Desativar professor não exclui histórico — turmas ficam sem professor ativo, coordenador recebe alerta
- Professor desativado não consegue fazer login
- Reativar professor restaura acesso e vinculações anteriores
- Importação em lote via CSV (Nome, E-mail, Disciplinas)

### Chamadas de API

| Método | Endpoint | Dados | Resposta |
|--------|----------|-------|----------|
| GET | /admin/professores | — | Lista paginada |
| POST | /admin/professores/convidar | { nome, email, disciplinas[] } | { conviteId } |
| PATCH | /admin/professores/:id/status | { ativo: bool } | 200 OK |
| POST | /admin/professores/importar | CSV multipart | { importados, erros[] } |

---

## Tela: Gestão de Alunos

> **Rota:** `/admin/alunos`  
> **Autenticação:** Requerida  
> **Perfis com acesso:** Admin, Coordenador

### Layout e Componentes

- **Busca e filtros:** Por nome, turma, série, status
- **Tabela de alunos:** Nome | E-mail | Turmas | Status | Ações
- **Modal "Novo Aluno":**
  - Dados pessoais: nome, e-mail, data de nascimento
  - Turmas a matricular (multiselect)
  - Responsáveis: nome, e-mail, parentesco (pode adicionar múltiplos)
  - Ao salvar: envia convite para aluno e responsáveis
- **Modal de detalhes do aluno:**
  - Turmas matriculadas
  - Responsáveis vinculados
  - Botão para transferir para outra turma
  - Botão "Desativar matrícula"

### Comportamentos Esperados

- Aluno só recebe convite de acesso após ser cadastrado
- Transferência de turma mantém histórico da turma anterior
- Importação em lote via CSV (Nome, E-mail, Turma, E-mail Responsável)
- Dados de menores seguem regras reforçadas de LGPD: consentimento dos responsáveis é obrigatório

### Chamadas de API

| Método | Endpoint | Dados | Resposta |
|--------|----------|-------|----------|
| GET | /admin/alunos | query params | Lista paginada |
| POST | /admin/alunos | { dadosPessoais, turmas[], responsaveis[] } | { alunoId } |
| POST | /admin/alunos/importar | CSV multipart | { importados, erros[] } |
| PATCH | /admin/alunos/:id/turma | { novaTurmaId } | 200 OK |

---

## Tela: Gestão de Turmas (Admin)

> **Rota:** `/admin/turmas`  
> **Autenticação:** Requerida  
> **Perfis com acesso:** Admin

### Layout e Componentes

- **Filtros:** Período letivo | Série | Disciplina | Professor
- **Tabela:** Nome | Série | Disciplina | Professor | Alunos | Status
- **Modal "Nova Turma":**
  - Nome (ex: "9º Ano A")
  - Série e disciplina
  - Professor responsável
  - Período letivo
  - Alunos (pode adicionar depois)
- **Modal de edição:** Mesmos campos + opção de encerrar turma

### Comportamentos Esperados

- Encerrar turma = soft delete + período muda para "encerrado" (somente leitura)
- Novas turmas são criadas apenas por Admin (professores não criam turmas, apenas gerenciam as suas)

---

## Tela: Configurações da Escola

> **Rota:** `/admin/configuracoes`  
> **Autenticação:** Requerida  
> **Perfis com acesso:** Admin

### Seções

**Dados da Escola:**
- Nome, CNPJ, endereço, logo
- Período letivo atual (datas de início e fim)

**Regras Pedagógicas:**
- Nota mínima de aprovação (padrão: 5,0)
- Frequência mínima de aprovação (padrão: 75%)
- Sistema de avaliação: bimestral / trimestral / semestral
- Peso por bimestre/trimestre

**Comunicação:**
- Configurar templates de e-mail (convite, alerta de falta, queda de nota)
- Canais de notificação habilitados (e-mail, push, SMS)

**Integrações:**
- Google Classroom: botão de conectar conta da escola
- Microsoft Teams: botão de conectar
- Status de cada integração (conectado / desconectado / erro)

**Uso da IA:**
- Limite mensal de gerações por professor
- Histórico de consumo (tokens usados / custo estimado)

### Chamadas de API

| Método | Endpoint | Dados | Resposta |
|--------|----------|-------|----------|
| GET | /admin/escola/configuracoes | — | { escola, regras, comunicacao, integracoes, ia } |
| PUT | /admin/escola/configuracoes | { ...campos editados } | 200 OK |
| POST | /admin/escola/logo | multipart | { logoUrl } |

---

## Tela: Relatórios Consolidados

> **Rota:** `/admin/relatorios`  
> **Autenticação:** Requerida  
> **Perfis com acesso:** Admin, Coordenador

### Relatórios Disponíveis

| Relatório | Descrição | Filtros | Formato |
|-----------|-----------|---------|---------|
| Desempenho geral | Médias por série, disciplina e bimestre | Período, série, disciplina | PDF, XLSX |
| Frequência consolidada | % por turma e aluno | Turma, período | PDF, XLSX |
| Alunos em risco | Lista de alunos abaixo do mínimo | Tipo de risco, série | PDF |
| Uso da plataforma | Acessos, atividades criadas, provas | Período | PDF |
| Ranking de turmas | Turmas por média e frequência | Período | PDF |

### Comportamentos Esperados

- Geração de relatórios é assíncrona (via fila) — notificar quando pronto
- Relatórios ficam disponíveis para download por 7 dias
- Relatórios em XLSX têm formatação condicional (células em vermelho para valores abaixo do mínimo)

---

## Regras de Negócio — Área Administrativa

- Admin só tem acesso à escola à qual pertence — não há acesso cross-escola
- Coordenador tem acesso de leitura a tudo e pode gerar relatórios, mas não pode editar configurações, criar professores ou excluir dados
- Todas as ações destrutivas (desativar professor, encerrar turma, remover aluno) exigem confirmação explícita com motivo registrado em log de auditoria
- Logs de auditoria são imutáveis e armazenados por mínimo de 5 anos (conformidade LGPD)
