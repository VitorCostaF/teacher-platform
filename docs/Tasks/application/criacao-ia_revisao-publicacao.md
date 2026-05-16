# Criação com IA — Tela de Revisão e Publicação

> **Escopo:** criacao-ia  
> **Tipo:** Frontend  
> **Complexidade estimada:** M  
> **Depende de:** `criacao-ia_gerador-provas.md`, `backend-avaliacoes_endpoint-publicar.md`

---

## Contexto

Última etapa antes de tornar a prova/atividade disponível para os alunos. O professor configura agendamento, permissões e publica.

---

## O que deve ser implementado

- Rota `/professor/criar/prova/:id/publicar` (e equivalente para atividade)
- Preview completo da avaliação exatamente como o aluno verá
- Configurações de publicação: data/hora de disponibilização, data/hora de encerramento, turmas destinatárias (multiselect), toggles de embaralhar questões/alternativas, configuração de liberação do gabarito, peso na nota final (0 a 10)
- Botão "Publicar" (primário) — ao clicar, chama `POST /provas/:id/publicar`, exibe loading, redireciona com toast de sucesso
- Botão "Salvar como Rascunho" — persiste sem publicar

---

## Critérios de Aceite

- [ ] Preview renderiza exatamente como o aluno veria
- [ ] Todas as configurações de publicação funcionam
- [ ] Turmas destinatárias permite seleção múltipla
- [ ] Data de disponibilização no futuro cria prova com status "Agendada"
- [ ] Após publicar: alunos das turmas selecionadas recebem notificação
- [ ] Rascunho salvo pode ser acessado e editado depois

---

## Especificação de Referência

- **Arquivo:** `02-area-professor/02b-criacao-com-ia.md`
- **Seção:** `Tela: Revisão e Publicação`

---

## Definition of Done

- [ ] Código implementado e funcionando
- [ ] Testado com múltiplas turmas destinatárias
- [ ] Code review realizado
