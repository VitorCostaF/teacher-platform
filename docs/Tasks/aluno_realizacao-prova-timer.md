# Área do Aluno — Realização de Prova com Timer

> **Escopo:** aluno  
> **Tipo:** Frontend  
> **Complexidade estimada:** G  
> **Depende de:** `aluno_realizacao-atividade.md`, `backend-aluno_endpoint-entregas.md`

---

## Contexto

Diferente da atividade, a prova tem timer regressivo visível. O tempo corre mesmo com a aba fechada. Ao expirar, a submissão é automática. Questões podem ser embaralhadas por aluno.

---

## O que deve ser implementado

- Rota `/aluno/provas/:id`
- Ao acessar pela primeira vez: chamar `POST /provas/:id/iniciar` para criar sessão com timestamp
- Timer regressivo no header calculado como: `duracao - (agora - iniciadaEm)` — baseado no servidor, não no cliente
- Timer fica vermelho quando < 5 minutos; vibração em mobile
- Entrega automática quando timer = 0 (chamar endpoint de entrega com o que foi respondido)
- Autosave a cada 60 segundos com `PUT /provas/:id/sessoes/:sessaoId/autosave`
- Ao sair da aba: registrar evento (visibilitychange) via API, exibir modal de aviso ao retornar
- Se offline: salvar respostas em localStorage, exibir banner, tentar sync ao reconectar
- Ao reabrir uma prova em andamento: calcular tempo restante a partir do servidor (não reiniciar)

---

## Critérios de Aceite

- [ ] Timer calculado com base no timestamp do servidor (resistente a manipulação local)
- [ ] Timer vira vermelho e vibra (mobile) em < 5 minutos
- [ ] Entrega automática acontece ao chegar a 0
- [ ] Saída de aba registra evento e exibe modal ao retornar
- [ ] Offline: banner de aviso + respostas em localStorage
- [ ] Ao reconectar offline: sync automático das respostas
- [ ] Ao reabrir prova em andamento: timer continua do ponto correto

---

## Especificação de Referência

- **Arquivo:** `03-area-aluno.md`
- **Seção:** `Tela: Realização de Prova (com timer)` e `Estados Críticos`

---

## Notas e Edge Cases

- Nunca usar `Date.now()` do cliente para calcular tempo restante — sempre calcular com base em `iniciadaEm` do servidor
- A página não deve ter `beforeunload` bloqueante — apenas registrar o evento

---

## Definition of Done

- [ ] Timer implementado com base no servidor
- [ ] Testado com sessão expirando de verdade (não mockado)
- [ ] Testado em cenário offline
- [ ] Code review realizado
