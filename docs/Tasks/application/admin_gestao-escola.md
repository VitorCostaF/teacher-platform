# Área Administrativa — Gestão de Escola, Professores e Alunos

> **Escopo:** admin  
> **Tipo:** Frontend  
> **Complexidade estimada:** G  
> **Depende de:** `backend-admin_endpoints.md`

---

## Contexto

Área exclusiva do Admin da Escola para gerenciar toda a instituição: cadastrar professores, alunos, turmas e configurar as regras pedagógicas da escola.

---

## O que deve ser implementado

**Visão Geral (`/admin/visao-geral`):**
- KPIs: total alunos, professores, turmas, % frequência média, média de notas
- Gráfico de desempenho por série (barras agrupadas)
- Ranking de turmas por frequência
- Tabela de alertas consolidados com filtros
- Feed de atividade recente

**Gestão de Professores (`/admin/professores`):**
- Tabela com busca, filtros, status
- Modal "Convidar Professor": nome, e-mail, disciplinas → dispara convite por e-mail
- Modal de detalhes: turmas vinculadas, histórico, botão desativar (com confirmação + campo motivo)
- Importação em lote via CSV

**Gestão de Alunos (`/admin/alunos`):**
- Tabela com busca por nome/turma/série/status
- Modal "Novo Aluno": dados pessoais, turmas (multiselect), responsáveis (múltiplos)
- Transferência de turma (mantém histórico)
- Importação em lote via CSV

**Configurações (`/admin/configuracoes`):**
- Formulário com dados da escola, regras pedagógicas (nota mínima, frequência mínima, sistema de avaliação), configurações de comunicação e integrações externas

---

## Critérios de Aceite

- [ ] KPIs carregam corretamente na visão geral
- [ ] Convite de professor dispara e-mail (visível na UI como "Convite enviado")
- [ ] Desativação de professor requer confirmação com motivo registrado
- [ ] Importação CSV exibe erros por linha antes de confirmar
- [ ] Modal de aluno suporta múltiplos responsáveis
- [ ] Configurações salvas persistem após recarregar a página
- [ ] Integrações exibem status correto (conectado/desconectado/erro)

---

## Especificação de Referência

- **Arquivo:** `05-area-administrativa.md`

---

## Definition of Done

- [ ] Código implementado e funcionando
- [ ] Testado com volume maior de dados (50+ professores, 500+ alunos)
- [ ] Code review realizado
