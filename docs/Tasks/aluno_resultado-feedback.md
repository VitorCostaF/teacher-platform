# Área do Aluno — Resultado e Feedback Pós-Entrega

> **Escopo:** aluno  
> **Tipo:** Frontend  
> **Complexidade estimada:** M  
> **Depende de:** `aluno_realizacao-atividade.md`, `backend-aluno_endpoint-entregas.md`

---

## Contexto

Após entregar uma atividade ou prova, o aluno vê o resultado (se disponível) com gabarito por questão e análise da IA sugerindo conteúdo de reforço.

---

## O que deve ser implementado

- Rota `/aluno/avaliacoes/:entregaId/resultado`
- Se gabarito disponível: exibir nota, média da turma, gabarito por questão (✅ certa / ❌ errada + alternativa correta para objetivas), observação do professor para dissertativas
- Se gabarito não disponível: confirmação de entrega + "O professor irá liberar o gabarito em breve."
- Seção de análise da IA: "Você errou X questões sobre [tópico]. Que tal revisar este material?" com link para conteúdo recomendado
- Questões dissertativas mostram "Aguardando correção do professor" até nota manual

---

## Critérios de Aceite

- [ ] Nota e média da turma exibidas quando gabarito disponível
- [ ] Gabarito mostra resposta do aluno e resposta correta por questão
- [ ] Alternativa correta destacada visualmente nas múltipla escolha
- [ ] Estado "aguardando gabarito" exibido corretamente
- [ ] Análise da IA exibida com link funcional para conteúdo
- [ ] Dissertativas mostram observação do professor quando corrigida

---

## Especificação de Referência

- **Arquivo:** `03-area-aluno.md`
- **Seção:** `Tela: Resultado e Feedback`

---

## Definition of Done

- [ ] Código implementado e funcionando
- [ ] Testado com avaliações objetivas e dissertativas
- [ ] Code review realizado
