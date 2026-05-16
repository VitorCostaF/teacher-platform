# Área do Aluno — Realização de Atividade

> **Escopo:** aluno  
> **Tipo:** Frontend  
> **Complexidade estimada:** G  
> **Depende de:** `backend-aluno_endpoint-entregas.md`

---

## Contexto

O aluno realiza uma atividade online. Pode navegar livremente entre questões, salvar rascunho e retomar depois. Sem timer obrigatório.

---

## O que deve ser implementado

- Rota `/aluno/atividades/:id`
- Header com título, disciplina e prazo
- Barra de progresso "Questão X de Y"
- Renderização de questões por tipo: múltipla escolha (radio), V/F (toggle), dissertativa (textarea), upload de arquivo
- Navegação livre entre questões (botões Anterior/Próxima + índice clicável)
- Autosave a cada 30 segundos (silencioso, sem feedback visual)
- Botão "Salvar Rascunho" (manual, explícito)
- Botão "Entregar" habilitado apenas quando todas as questões respondidas
- Modal de confirmação antes de entregar
- Se prazo encerrado e professor não permite atraso: botão desabilitado + mensagem
- Se prazo encerrado e professor permite atraso: aviso amarelo + entrega marcada como atrasada

---

## Critérios de Aceite

- [ ] Aluno pode navegar entre questões livremente
- [ ] Respostas são preservadas ao navegar entre questões
- [ ] Autosave silencioso acontece a cada 30s
- [ ] Botão "Entregar" desabilitado com questões sem resposta
- [ ] Modal de confirmação exibido antes de entregar
- [ ] Prazo encerrado (sem permissão de atraso) desabilita entrega com mensagem clara
- [ ] Upload de arquivo funciona para questões dissertativas (PDF/imagem, máx 5MB)
- [ ] Ao retornar a atividade em andamento, respostas do rascunho são pré-carregadas

---

## Especificação de Referência

- **Arquivo:** `03-area-aluno.md`
- **Seção:** `Tela: Realização de Atividade`

---

## Notas e Edge Cases

- Rascunho salvo no servidor (não apenas localStorage) para persistir entre dispositivos
- Se a rede cair durante o autosave, tentar novamente na próxima janela de 30s sem alertar o usuário

---

## Definition of Done

- [ ] Código implementado e funcionando
- [ ] Testado com todos os tipos de questão
- [ ] Testado em mobile
- [ ] Code review realizado
