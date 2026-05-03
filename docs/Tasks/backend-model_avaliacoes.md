# Backend Model — Tabelas de Avaliações, Entregas e Conteúdos

> **Escopo:** backend-model  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-model_turmas.md`

---

## Contexto

Migrations e models para o núcleo pedagógico: provas, atividades, questões, entregas, respostas, conteúdos e flashcards.

---

## O que deve ser implementado

**Migrations:**
- `avaliacoes` — id, turma_id, professor_id, titulo, tipo (enum), status (enum), peso_nota, duracao_minutos, disponivel_em, encerra_em, embaralhar_questoes/alternativas, gabarito_liberacao (enum), permite_entrega_atrasada, gerado_por_ia, criado_em
- `questoes` — id, avaliacao_id, ordem, tipo (enum), enunciado, alternativas (JSONB), gabarito_dissertativo, dificuldade, topico, pontos
- `sessoes_prova` — id, avaliacao_id, aluno_id, iniciada_em, encerrada_em, entregue_manualmente
- `entregas` — id, avaliacao_id, aluno_id, status (enum), iniciado_em, entregue_em, nota_automatica, nota_final, entrega_atrasada. UNIQUE(avaliacao_id, aluno_id)
- `respostas` — id, entrega_id, questao_id, resposta_indice, resposta_texto, arquivo_url, correta, nota_manual
- `conteudos` — id, turma_id, professor_id, titulo, tipo (enum), corpo, arquivo_url, video_url, link_externo, publicado_em, serie_sugerida, topicos (array)
- `flashcards` — id, conteudo_id, turma_id, pergunta, resposta, gerado_por_ia, criado_em
- `progresso_flashcards` — id, aluno_id, flashcard_id, resultado (enum), revisado_em, proxima_revisao

---

## Critérios de Aceite

- [ ] Todas as migrations rodam sem erros
- [ ] Todas as migrations são reversíveis
- [ ] UNIQUE constraints corretas (entrega por aluno por avaliação)
- [ ] JSONB de alternativas aceita o formato especificado
- [ ] Enums validados no banco

---

## Especificação de Referência

- **Arquivo:** `06-modelos-de-dados.md`
- **Seções:** Tabelas relevantes

---

## Definition of Done

- [ ] Migrations implementadas e testadas
- [ ] Models/entities com relacionamentos
- [ ] Testes de migration up/down
- [ ] Code review realizado
