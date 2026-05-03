# Plano de Implementação — criacao-ia_grade-e-sugestoes

> **Task origem:** `docs/Tasks/criacao-ia_grade-e-sugestoes.md`
> **Escopo:** Frontend — Criação com IA
> **Complexidade:** M
> **Sprint:** 3 — Criação com IA
> **Depende de:** `backend-ia_endpoint-gerar-prova-plan.md`

---

## Contexto do Codebase

`iaService`, `Button`, `Input`, `Skeleton`, `useTurmas`, `useToast`, TanStack Query disponíveis. Esta task adiciona dois novos geradores: grade de aulas e sugestão de conteúdos BNCC.

---

## Dependências a Adicionar

```bash
npm install @dnd-kit/core @dnd-kit/sortable @dnd-kit/utilities
npm install jspdf html2canvas
```

---

## Arquivos a Criar

### Serviço (extensão)

Adicionar ao `frontend/src/features/criacao-ia/services/ia.service.ts`:
```typescript
gerarGrade: (req: GerarGradeDto) =>
  apiClient.post<GradeResponse>('/ia/gerar-grade', req).then(r => r.data),
getSugestoesConteudo: (params: SugestaoParams) =>
  apiClient.get<SugestaoConteudoResponse>('/ia/sugestoes-conteudo', { params }).then(r => r.data),
```

### Tipos

Adicionar ao `frontend/src/features/criacao-ia/types.ts`:
```typescript
export interface AulaGrade {
  id: string; semana: number; aula: number
  conteudo: string; objetivos: string; recursosSugeridos: string
}
export interface GradeResponse { aulas: AulaGrade[] }
export interface SugestaoConteudoResponse {
  competenciasBNCC: string[]; topicos: string[]; linksComplementares: string[]
}
```

### Componentes — Grade de Aulas

`frontend/src/features/criacao-ia/components/GradeAulasTable.tsx`
- Tabela editável: colunas Semana | Aula | Conteúdo | Objetivos | Recursos Sugeridos
- Cada célula: texto normal; ao clicar, vira `<textarea autoFocus>`; blur salva
- Drag-and-drop de linhas: usa `@dnd-kit/sortable` (DragHandle no início de cada linha)
- Linhas reordenáveis apenas via handle (não a linha inteira)

`frontend/src/features/criacao-ia/components/GradeExportButtons.tsx`
- Botão "Exportar PDF": usa `jspdf` + `html2canvas` para capturar a tabela
- Botão "Exportar DOCX": usa `xlsx` (já instalado) para criar planilha ou texto estruturado
- Botão "Salvar na plataforma": chama endpoint de salvamento (POST /grades)

### Página Grade

`frontend/src/features/criacao-ia/pages/GeradorGradePage.tsx`
- Rota: `/professor/criar/grade`
- Formulário: selects de turma/disciplina, período (Semana/Mês/Semestre), aulas/semana
- Input multi-linha para tópicos obrigatórios
- Toggle de alinhamento com BNCC
- Botão "Gerar Grade" → `POST /ia/gerar-grade`
- Loading state durante geração (skeleton da tabela)
- Após geração: `<GradeAulasTable>` + `<GradeExportButtons>`

### Componentes — Sugestão de Conteúdos

`frontend/src/features/criacao-ia/components/SugestaoConteudoResult.tsx`
- Seções: Competências BNCC (lista), Tópicos Sugeridos (chips clicáveis), Links Curados (com ícone externo)
- Botão "Usar para gerar prova" → navega para `/professor/criar/prova` passando tópicos via router state
- Botão "Usar para gerar atividade" → similar

### Página Sugestão

`frontend/src/features/criacao-ia/pages/SugestoesConteudoPage.tsx`
- Rota: `/professor/criar/sugestoes`
- Selects: série, disciplina, bimestre/trimestre
- Botão "Buscar Sugestões"
- Estado de resultado: `<SugestaoConteudoResult>`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/features/criacao-ia/types.ts` | Adicionar tipos de grade e sugestão |
| `frontend/src/router/index.tsx` | Rotas `/professor/criar/grade` e `/professor/criar/sugestoes` |
| `frontend/src/features/criacao-ia/pages/GeradorProvasPage.tsx` | Ler router state de tópicos pré-preenchidos vindos de SugestoesConteudoPage |

---

## Ordem de Implementação

```
1. Instalar @dnd-kit, jspdf, html2canvas
2. Adicionar tipos em types.ts
3. Extensão de ia.service.ts
4. GradeAulasTable (edição inline + drag-and-drop)
5. GradeExportButtons
6. GeradorGradePage
7. SugestaoConteudoResult
8. SugestoesConteudoPage
9. Atualizar router + GeradorProvasPage (pré-preenchimento de tópicos)
10. Testes: drag-and-drop em touch, edição inline, export PDF
```

---

## Checklist de Validação

- [ ] Tabela da grade editável inline
- [ ] Drag-and-drop reordena linhas
- [ ] Exportação PDF captura a tabela
- [ ] Sugestões BNCC exibidas com competências e tópicos
- [ ] Botão "Usar para gerar prova/atividade" pré-preenche o gerador

---

## Resumo

- **7 arquivos** a criar (tipos, extensão service, 3 componentes, 2 páginas)
- **2 arquivos** a modificar (router, GeradorProvasPage)
- **Dependências a adicionar:** @dnd-kit/core+sortable, jspdf, html2canvas
- **Complexidade mantida:** M
