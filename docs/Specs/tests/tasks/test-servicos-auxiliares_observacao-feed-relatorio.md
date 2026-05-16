# Testes Unitários — ObservacaoService, FeedAlunoService e RelatorioService

> **Escopo:** Backend — serviços auxiliares de conteúdo  
> **Tipo:** Backend  
> **Complexidade estimada:** P  
> **Depende de:** Nenhuma

---

## Contexto

Três serviços de suporte ao fluxo pedagógico: `ObservacaoService` (registros qualitativos de professores sobre alunos), `FeedAlunoService` (lista de atividades disponíveis) e `RelatorioService` (geração assíncrona de PDFs). São menores em lógica de negócio, mas críticos para a experiência do aluno e responsável.

---

## O que deve ser implementado

Criar três classes de teste cobrindo os comportamentos documentados na spec.

---

## Critérios de Aceite

**`ObservacaoServiceTest`:**
- [ ] `criar`: `observacaoRepository.save` chamado com `turmaId`, `alunoId` e `professorId` corretos
- [ ] `criar`: `criadoEm != null`
- [ ] `listar` com perfil `PROFESSOR`: filtra por autor (professor atual)
- [ ] `listar` com perfil `ADMIN`: retorna todas (sem filtro por autor)
- [ ] `listar` com perfil `RESPONSAVEL`: retorna todas (sem filtro por autor)

**`FeedAlunoServiceTest`:**
- [ ] Avaliação em `RASCUNHO` **não** aparece no feed
- [ ] Avaliação `PUBLICADA` da turma do aluno aparece no feed
- [ ] Aluno que entregou: `statusEntrega` preenchido na resposta
- [ ] Aluno sem entrega: `statusEntrega = "NAO_INICIADO"`

**`RelatorioServiceTest`:**
- [ ] `solicitarRelatorioPDF`: retorna `RelatorioStatusResponse` com `status = PENDENTE`
- [ ] Duas chamadas geram `jobId` distintos
- [ ] `getStatus`: job não encontrado → exceção ou resposta `NOT_FOUND`
- [ ] `getStatus`: retorna status atual do job

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/ServicosAuxiliares.md`
- **Seções:** `ObservacaoService`, `FeedAlunoService`, `RelatorioService`

---

## Detalhes Técnicos

**Localizações:**
- `src/test/java/.../service/ObservacaoServiceTest.java`
- `src/test/java/.../service/FeedAlunoServiceTest.java`
- `src/test/java/.../service/RelatorioServiceTest.java`

---

## Definition of Done

- [ ] Três classes de teste criadas
- [ ] Todos os cenários cobertos (mínimo 5 testes por classe)
- [ ] Testes passam com `./mvnw test`
