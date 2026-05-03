# Criação com IA — Tela do Gerador de Provas

> **Escopo:** criacao-ia  
> **Tipo:** Frontend  
> **Complexidade estimada:** G  
> **Depende de:** `backend-ia_endpoint-gerar-prova.md`, `turmas_lista-e-cards.md`

---

## Contexto

O professor configura parâmetros e fornece o conteúdo; a IA gera uma prova estruturada. O professor revisa e edita cada questão antes de publicar. Esta é a funcionalidade central de diferenciação da plataforma.

---

## O que deve ser implementado

**Painel esquerdo — Configuração:**
- Selects: turma destino, nível de dificuldade (Fácil/Médio/Difícil/Misto)
- Inputs: título, duração (minutos), data de aplicação
- Checkboxes de tipos de questão com inputs de quantidade por tipo
- Tabs para fonte do conteúdo: "Colar texto" (textarea), "Upload de arquivo" (PDF/DOCX, máx 10MB), "Tópicos livres"
- Botão "Gerar com IA" que dispara `POST /ia/gerar-prova`

**Painel direito — Preview:**
- Estado vazio com placeholder enquanto não gerou
- Estado de loading com animação durante geração
- Lista de questões geradas com: enunciado, alternativas, ações por questão (✏️ Editar | 🔄 Regenerar | 🗑️ Remover)
- Botão "Adicionar questão manualmente"
- Botão "Regenerar tudo"
- Botão "Publicar" (navega para tela de revisão)

---

## Critérios de Aceite

- [ ] Validação de configuração antes de chamar a IA (campos obrigatórios)
- [ ] Loading animation exibido no painel direito durante geração
- [ ] Questões renderizadas corretamente por tipo (múltipla escolha, V/F, dissertativa)
- [ ] Edição inline de questão funciona
- [ ] Botão regenerar questão individual chama `POST /ia/regenerar-questao`
- [ ] Remoção de questão atualiza lista e numeração
- [ ] Upload de arquivo envia para `/upload/conteudo` antes de chamar a IA
- [ ] Aviso exibido se conteúdo extraído for insuficiente (< 100 palavras úteis)
- [ ] Badge "Gerado com IA" visível nas questões

---

## Especificação de Referência

- **Arquivo:** `02-area-professor/02b-criacao-com-ia.md`
- **Seção:** `Tela: Gerador de Provas`

---

## Notas e Edge Cases

- Gabarito das questões objetivas é visível apenas para o professor (não exibir no preview do aluno)
- Questões dissertativas têm campo "critérios de correção" editável pelo professor
- Conteúdo gerado não é salvo automaticamente — apenas ao publicar ou salvar como rascunho

---

## Definition of Done

- [ ] Código implementado e funcionando
- [ ] Testado com diferentes tipos de conteúdo e combinações de questões
- [ ] Testado com upload de PDF e DOCX
- [ ] Code review realizado
