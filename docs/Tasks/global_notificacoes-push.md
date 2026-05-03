# Global — Sistema de Notificações Push

> **Escopo:** global  
> **Tipo:** Fullstack  
> **Complexidade estimada:** G  
> **Depende de:** `global_offline-e-pwa.md`, `backend-frequencia_endpoint-lancar.md`

---

## Contexto

Notificações push para alertar responsáveis sobre faltas e queda de desempenho, e para lembrar alunos de prazos. Frontend solicita permissão e registra o device; backend dispara via fila.

---

## O que deve ser implementado

**Frontend:**
- Solicitar permissão de notificação após 1ª interação significativa (não imediatamente no login)
- Registrar subscription via `POST /notificacoes/registrar-device` com o endpoint do Service Worker
- Tratar clique na notificação: abrir app na rota relevante

**Backend:**
- `POST /notificacoes/registrar-device` — salvar subscription por usuário e dispositivo
- Fila de notificações: worker que processa e envia via Web Push API
- Tipos de disparo: falta registrada → notificação para responsável, frequência < 75% → alerta, prazo de prova em 24h → lembrete para aluno
- Configurações por usuário: `GET/PUT /usuario/preferencias-notificacao`

---

## Critérios de Aceite

- [ ] Permissão não solicitada imediatamente no login
- [ ] Notificação clicada abre a rota correta no app
- [ ] Notificação de falta chega ao responsável em < 5min após lançamento
- [ ] Alerta de frequência < 75% enviado corretamente
- [ ] Usuário pode desativar tipos específicos de notificação
- [ ] Dispositivos descadastrados removidos automaticamente (expiração)

---

## Especificação de Referência

- **Arquivo:** `07-comportamentos-globais.md` e `04-area-responsavel.md`
- **Seção:** `6. Notificações Push` e `Sistema de Alertas e Notificações`

---

## Definition of Done

- [ ] Push funcionando em Chrome e Safari
- [ ] Fila de notificações com retry para falhas
- [ ] Testes de integração para os gatilhos principais
- [ ] Code review realizado
