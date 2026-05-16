# Spec 02a — Área do Professor: Gestão de Turmas e Frequência

> **Macro funcionalidade:** Gestão de turmas, alunos e controle de presença  
> **Perfis envolvidos:** Professor, Admin da Escola  
> **Plataforma:** Web (desktop-first)

---

## Telas desta Spec

1. [Lista de Turmas](#tela-lista-de-turmas)
2. [Detalhes da Turma](#tela-detalhes-da-turma)
3. [Cadastro / Edição de Turma](#tela-cadastro-de-turma)
4. [Lançamento de Frequência](#tela-lançamento-de-frequência)
5. [Histórico de Frequência por Aluno](#tela-histórico-de-frequência)

---

## Tela: Lista de Turmas

> **Rota:** `/professor/turmas`  
> **Autenticação:** Requerida  
> **Perfis com acesso:** Professor, Admin

### Contexto

Tela inicial da área do professor após o login. Exibe todas as turmas que o professor leciona no período letivo ativo.

### Objetivo

Permitir que o professor visualize, acesse e gerencie suas turmas rapidamente.

### Layout e Componentes

- **Header** — nome do professor, avatar, link para configurações
- **Seletor de período letivo** — dropdown para alternar entre anos/semestres
- **Botão "Nova Turma"** — visível apenas para Admin; para professor, exibir apenas se tiver permissão configurada
- **Grid de cards de turma** — cada card contém:
  - Nome da turma (ex: "9º Ano A — Matemática")
  - Número de alunos
  - Próxima aula agendada
  - Indicador de pendências (atividades não corrigidas, frequência não lançada)
- **Campo de busca** — filtra cards por nome da turma em tempo real (client-side)

### Estados da Tela

| Estado | Descrição | Componentes afetados |
|--------|-----------|----------------------|
| **Carregando** | Requisição inicial em andamento | Skeleton nos cards |
| **Vazio** | Professor sem turmas no período | Ilustração + "Nenhuma turma cadastrada neste período" |
| **Preenchido** | Turmas carregadas | Grid de cards |
| **Busca sem resultado** | Filtro não retornou turmas | Mensagem inline "Nenhuma turma encontrada" |

### Chamadas de API

| Método | Endpoint | Momento | Resposta esperada |
|--------|----------|---------|-------------------|
| GET | /professor/turmas?periodo=:id | Ao montar a tela | Lista de turmas com metadados |

---

## Tela: Detalhes da Turma

> **Rota:** `/professor/turmas/:turmaId`  
> **Autenticação:** Requerida  
> **Perfis com acesso:** Professor dono da turma, Admin

### Contexto

Professor clicou em uma turma específica e deseja ver os alunos, frequência e atividades relacionadas.

### Layout e Componentes

- **Header da turma** — nome, série, disciplina, período letivo
- **Abas de navegação:** Alunos | Frequência | Atividades | Desempenho
- **Aba Alunos:**
  - Lista de alunos com foto, nome e e-mail
  - Botão "Adicionar Aluno" — abre modal de busca/convite
  - Botão "Importar via planilha" — upload de CSV/XLSX
  - Ação por aluno: remover da turma (com confirmação)
- **Botão "Lançar Frequência"** — destacado, leva para tela de frequência com data atual pré-selecionada

### Estados da Tela

| Estado | Descrição |
|--------|-----------|
| **Padrão** | Aba "Alunos" ativa, lista carregada |
| **Turma vazia** | Sem alunos: ilustração + CTA "Adicionar primeiro aluno" |
| **Carregando** | Skeleton na lista de alunos |

### Comportamentos Esperados

- Ao remover um aluno da turma, exibir modal de confirmação: "Tem certeza? O histórico do aluno nesta turma será mantido, mas ele perderá acesso às atividades."
- A importação via planilha deve aceitar `.csv` e `.xlsx`. Erros de formatação devem ser listados linha a linha antes de confirmar a importação.
- Alunos removidos não são excluídos da plataforma — apenas desvinculados da turma.

### Chamadas de API

| Método | Endpoint | Momento | Dados enviados | Resposta esperada |
|--------|----------|---------|----------------|-------------------|
| GET | /turmas/:id | Ao montar | — | Dados da turma |
| GET | /turmas/:id/alunos | Ao montar | — | Lista de alunos |
| POST | /turmas/:id/alunos | Ao adicionar | { alunoId } ou { email } | Aluno adicionado |
| DELETE | /turmas/:id/alunos/:alunoId | Ao remover | — | 204 No Content |
| POST | /turmas/:id/alunos/importar | Upload planilha | multipart/form-data | { importados, erros } |

---

## Tela: Lançamento de Frequência

> **Rota:** `/professor/turmas/:turmaId/frequencia`  
> **Autenticação:** Requerida  
> **Perfis com acesso:** Professor dono da turma

### Contexto

Professor vai registrar a presença dos alunos para uma data específica. Por padrão, a data é a de hoje. É possível lançar frequência retroativa (até 7 dias atrás).

### Objetivo

Registrar presença/falta de cada aluno de forma rápida, com mínimo de cliques.

### Layout e Componentes

- **Seletor de data** — datepicker limitado entre a data de início da turma e hoje
- **Indicador "Frequência já lançada"** — badge de aviso se já existe registro para a data selecionada
- **Lista de alunos** — cada linha contém:
  - Foto e nome do aluno
  - Toggle Presente / Falta / Falta Justificada
  - Campo de observação (expandível, opcional)
- **Botão "Marcar todos como presentes"** — ação rápida no topo da lista
- **Botão "Salvar"** — fixo no rodapé (sticky), com contador "X de Y alunos registrados"

### Entradas

| Campo | Tipo | Obrigatório | Validação |
|-------|------|-------------|-----------|
| data | date | sim | Entre início da turma e hoje |
| presenca[alunoId] | enum | sim | presente / falta / falta_justificada |
| observacao[alunoId] | string | não | Máximo 200 caracteres |

### Interações do Usuário

| Ação | Gatilho | O que acontece |
|------|---------|----------------|
| Alterna status do aluno | Toggle | Atualiza estado local imediatamente |
| Clica "Marcar todos presentes" | Botão | Define todos como "presente" |
| Clica "Salvar" | Botão sticky | Chama POST/PUT /frequencia, exibe loading |
| Troca de data com dados não salvos | Seletor de data | Modal de confirmação: "Deseja descartar as alterações?" |

### Estados da Tela

| Estado | Descrição |
|--------|-----------|
| **Padrão (nova)** | Todos os alunos sem status definido; botão Salvar desabilitado |
| **Padrão (existente)** | Status pré-carregado do registro anterior; badge de aviso exibido |
| **Parcialmente preenchido** | Contador no botão Salvar indica alunos pendentes |
| **Salvando** | Botão com spinner, lista bloqueada |
| **Salvo com sucesso** | Toast "Frequência salva!" + redirect para detalhes da turma |

### Comportamentos Esperados

- O botão "Salvar" só habilita quando todos os alunos tiverem status definido
- Frequência já salva pode ser editada (sobrescreve o registro, mantém histórico de edição)
- Alunos adicionados à turma após uma data não aparecem retroativamente naquele registro
- Se a turma não tem aula no dia selecionado (baseado na grade), exibir aviso suave — não bloquear

### Alertas Automáticos de Falta

O sistema deve disparar notificação automática (e-mail + push) para o responsável quando:
- Aluno atinge 20% de faltas no total de aulas da disciplina
- Aluno falta 3 dias consecutivos

### Chamadas de API

| Método | Endpoint | Momento | Dados enviados | Resposta esperada |
|--------|----------|---------|----------------|-------------------|
| GET | /turmas/:id/frequencia?data=YYYY-MM-DD | Ao montar / trocar data | — | Registro existente ou null |
| POST | /turmas/:id/frequencia | Salvar nova | { data, registros[] } | 201 Created |
| PUT | /turmas/:id/frequencia/:frequenciaId | Editar existente | { registros[] } | 200 OK |

---

## Tela: Histórico de Frequência por Aluno

> **Rota:** `/professor/turmas/:turmaId/alunos/:alunoId/frequencia`  
> **Autenticação:** Requerida  
> **Perfis com acesso:** Professor, Admin, Responsável (read-only)

### Layout e Componentes

- **Header** — nome do aluno, foto, turma
- **Resumo numérico** — Total de aulas | Presenças | Faltas | % de frequência
- **Barra de progresso visual** — mostra percentual de presença com indicador do limite (ex: linha vermelha em 75%)
- **Calendário mensal** — dias com aula coloridos: verde (presente), vermelho (falta), amarelo (justificada), cinza (sem aula)
- **Lista detalhada** — linha por data com status e observação do professor

### Comportamentos Esperados

- Percentual de frequência é calculado como: `(presenças / total de aulas até hoje) * 100`
- Se frequência < 75%, exibir banner de alerta: "Este aluno está em risco de reprovação por falta."
- Exportar frequência individual como PDF (botão secundário)

### Chamadas de API

| Método | Endpoint | Resposta esperada |
|--------|----------|-------------------|
| GET | /turmas/:id/alunos/:alunoId/frequencia | { resumo, registros[], percentual } |

---

## Endpoints: Gestão de Turmas

### GET /professor/turmas

> **Autenticação:** Bearer Token — Professor ou Admin

**Query Parameters:**
| Parâmetro | Tipo | Padrão | Descrição |
|-----------|------|--------|-----------|
| periodo | string (UUID) | período ativo | Filtra por período letivo |

**200 OK:**
```json
{
  "turmas": [
    {
      "id": "uuid",
      "nome": "9º Ano A — Matemática",
      "serie": "9º Ano",
      "disciplina": "Matemática",
      "totalAlunos": 32,
      "proximaAula": "2025-01-16",
      "pendencias": {
        "frequenciasNaoLancadas": 2,
        "atividadesNaoCorrigidas": 5
      }
    }
  ],
  "total": 4
}
```

### Regras de Negócio — Turmas

- Um professor só pode visualizar e editar turmas às quais está vinculado
- Admins têm acesso irrestrito a todas as turmas da escola
- A exclusão de uma turma é um soft delete — históricos de frequência e notas são preservados
- Turmas de períodos anteriores ficam em modo somente leitura
