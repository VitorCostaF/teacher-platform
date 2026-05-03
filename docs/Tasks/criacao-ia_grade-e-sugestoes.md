# Criação com IA — Grade de Aulas e Sugestão de Conteúdos

> **Escopo:** criacao-ia  
> **Tipo:** Frontend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-ia_endpoint-gerar-grade.md`

---

## Contexto

Duas funcionalidades que ajudam o professor a planejar: gerador de grade de aulas (planejamento semestral) e sugestão de conteúdos por série com base na BNCC.

---

## O que deve ser implementado

**Grade de Aulas (`/professor/criar/grade`):**
- Formulário: turma/disciplina, período (Semana/Mês/Semestre), aulas por semana, tópicos obrigatórios, toggle de alinhamento com BNCC
- Após geração: tabela editável com colunas Semana | Aula | Conteúdo | Objetivos | Recursos Sugeridos
- Drag-and-drop para reordenar linhas
- Edição inline de cada célula
- Botões: Exportar PDF, Exportar DOCX, Salvar na plataforma

**Sugestão de Conteúdos (`/professor/criar/sugestoes`):**
- Selects: série, disciplina, bimestre/trimestre
- Resultado: competências BNCC + tópicos sugeridos + links curados
- Botão "Usar este conteúdo para gerar prova/atividade" — pré-preenche o gerador correspondente

---

## Critérios de Aceite

- [ ] Grade gerada exibe tabela formatada corretamente
- [ ] Drag-and-drop funciona para reordenação das aulas
- [ ] Edição inline atualiza célula sem recarregar a tabela
- [ ] Exportação para PDF e DOCX funciona
- [ ] Sugestões BNCC são exibidas com competências e tópicos
- [ ] Botão de uso pré-preenche o gerador de prova/atividade

---

## Especificação de Referência

- **Arquivo:** `02-area-professor/02b-criacao-com-ia.md`
- **Seções:** `Tela: Gerador de Grade de Aulas` e `Tela: Sugestão de Conteúdos por Série`

---

## Definition of Done

- [ ] Código implementado e funcionando
- [ ] Drag-and-drop testado em touch (mobile/tablet)
- [ ] Code review realizado
