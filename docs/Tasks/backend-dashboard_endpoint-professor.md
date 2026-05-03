# Backend Dashboard — Endpoints de Desempenho e Relatórios

> **Escopo:** backend-dashboard  
> **Tipo:** Backend  
> **Complexidade estimada:** G  
> **Depende de:** `backend-model_avaliacoes.md`, `backend-frequencia_endpoint-lancar.md`

---

## Contexto

Endpoints que agregam dados de notas, frequência e engajamento para os dashboards do professor, coordenador e responsável. Inclui geração assíncrona de PDFs.

---

## O que deve ser implementado

- `GET /professor/dashboard` — alertas, turmas com métricas, médias históricas, mapa de calor
- `GET /turmas/:id/desempenho?periodo=:p` — métricas da turma, distribuição, ranking de alunos, avaliações, mapa de calor
- `GET /turmas/:id/alunos/:alunoId/desempenho` — resumo, evolução de notas, frequência, tópicos, avaliações
- `POST /turmas/:id/alunos/:alunoId/observacoes` — criar observação privada
- `GET /turmas/:id/alunos/:alunoId/observacoes` — listar observações
- `POST /relatorios/turma/:id/pdf` e `POST /relatorios/aluno/:id/pdf` — geração assíncrona via fila; notificar via webhook/push quando pronto. URL assinada com validade de 24h.
- Cálculo de tendência: comparar média das últimas 3 avaliações com 3 anteriores

---

## Critérios de Aceite

- [ ] Dashboard retorna alertas com dados corretos
- [ ] Mapa de calor calculado por tópico × turma
- [ ] Tendência calculada corretamente (↑ ↓)
- [ ] Posição na turma calculada corretamente por avaliação
- [ ] Geração de PDF assíncrona com notificação ao completar
- [ ] URL do PDF assinada expira em 24h
- [ ] Observações visíveis apenas para professor e coordenador (não responsável)

---

## Especificação de Referência

- **Arquivo:** `02-area-professor/02c-dashboard-desempenho.md`

---

## Definition of Done

- [ ] Endpoints implementados com queries otimizadas (índices no banco)
- [ ] Geração assíncrona de PDF via fila (Bull/Sidekiq/similar)
- [ ] Testes de performance com volume de dados realista
- [ ] Code review realizado
