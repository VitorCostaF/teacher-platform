# Backend Aluno — Endpoints de Entrega de Atividades e Provas

> **Escopo:** backend-aluno  
> **Tipo:** Backend  
> **Complexidade estimada:** G  
> **Depende de:** `backend-model_avaliacoes.md`

---

## Contexto

Endpoints para iniciar, autosalvar e entregar atividades e provas. Inclui lógica de sessão de prova, validação de prazo e cálculo automático de nota para questões objetivas.

---

## O que deve ser implementado

- `GET /atividades/:id` — dados da atividade + estado atual do aluno (não iniciado / rascunho / entregue)
- `PUT /atividades/:id/rascunho` — salvar respostas parciais
- `POST /atividades/:id/entregar` — validar prazo, salvar respostas, calcular nota automática (objetivas), retornar feedback se gabarito disponível
- `POST /provas/:id/iniciar` — criar sessão com `iniciadaEm` do servidor; verificar se já existe sessão (retornar existente)
- `PUT /provas/:id/sessoes/:sessaoId/autosave` — salvar respostas, registrar evento de visibilidade se enviado
- `POST /provas/:id/sessoes/:sessaoId/entregar` — validar sessão, salvar, calcular nota
- Entrega automática por expiração do timer: job/cron que entrega sessões expiradas sem entrega manual
- `GET /aluno/avaliacoes/:entregaId/resultado` — retornar resultado, gabarito (se disponível), análise da IA

---

## Critérios de Aceite

- [ ] Sessão de prova criada com timestamp do servidor (não do cliente)
- [ ] Sessão existente retornada ao tentar iniciar prova já iniciada
- [ ] Nota automática calculada corretamente para questões objetivas
- [ ] Job de entrega automática por expiração funciona
- [ ] Gabarito não exposto antes da liberação pelo professor
- [ ] Resultado inclui análise de tópicos com erros

---

## Especificação de Referência

- **Arquivo:** `03-area-aluno.md`
- **Seções:** `Chamadas de API` de cada tela

---

## Notas e Edge Cases

- UNIQUE(avaliacao_id, aluno_id) na tabela entregas — não permitir dupla entrega
- Job de expiração deve rodar a cada minuto e tratar sessões em lote

---

## Definition of Done

- [ ] Endpoints implementados com todas as validações
- [ ] Job de expiração implementado e testado
- [ ] Testes de integração com cenários de tempo real
- [ ] Code review realizado
