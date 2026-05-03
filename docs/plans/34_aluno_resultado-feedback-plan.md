# Plano de Implementação — aluno_resultado-feedback

> **Task origem:** `docs/Tasks/aluno_resultado-feedback.md`
> **Escopo:** Frontend — Área do Aluno
> **Complexidade:** M
> **Sprint:** 4 — Área do Aluno
> **Depende de:** `aluno_realizacao-atividade-plan.md`, `backend-aluno_endpoint-entregas-plan.md`

---

## Contexto do Codebase

`alunoService`, `Button`, `Skeleton`, TanStack Query, `QuestaoRenderer` (modo somente leitura) disponíveis. Esta task implementa a tela de resultado pós-entrega.

---

## Arquivos a Criar

### Serviço (extensão)

Adicionar ao `frontend/src/features/aluno/services/aluno.service.ts`:
```typescript
getResultado: (entregaId: number) =>
  apiClient.get<ResultadoData>(`/aluno/avaliacoes/${entregaId}/resultado`).then(r => r.data),
```

### Tipos

Adicionar ao `frontend/src/features/aluno/types.ts`:
```typescript
export interface ResultadoData {
  nota?: number; mediaTurma?: number
  gabaritoDisponivel: boolean
  gabarito?: GabaritoQuestao[]
  analiseTopicos?: AnaliseTopico[]
}
export interface GabaritoQuestao {
  questaoId: number; numero: number; tipo: string; enunciado: string
  respostaAluno: string | number; respostaCorreta?: string | number
  correta?: boolean; alternativas?: string[]
  observacaoProfessor?: string  // dissertativas
}
export interface AnaliseTopico { topico: string; questoesErradas: number; totalQuestoes: number }
```

### Componentes

`frontend/src/features/aluno/components/ResultadoHeader.tsx`
- Se gabarito disponível: nota grande centralizada + "Média da turma: X.X"
- Se não disponível: mensagem "O professor irá liberar o gabarito em breve." + ícone de relógio

`frontend/src/features/aluno/components/GabaritoQuestaoCard.tsx`
- Renderiza por tipo:
  - Objetiva: mostra resposta do aluno + alternativa correta; ✅ se correta, ❌ se errada
  - Múltipla escolha: alternativas listadas; alternativa correta com fundo verde; resposta errada do aluno sublinhada em vermelho
  - V/F: mesmo padrão visual
  - Dissertativa: resposta do aluno + status "Aguardando correção do professor" (ou nota + observação se já corrigida)
- Badge de status: "Correta" (verde) / "Incorreta" (vermelho) / "Aguardando" (cinza)

`frontend/src/features/aluno/components/AnaliseIASection.tsx`
- Título: "Análise personalizada"
- Para cada tópico com erros: "Você errou X de Y questões sobre [tópico]. Que tal revisar?"
- Link para conteúdo recomendado → navega para flashcards do tópico
- Se nenhum erro: "Ótimo trabalho! Você acertou tudo." (sem sugestões)

### Página

`frontend/src/features/aluno/pages/ResultadoPage.tsx`
- Rota: `/aluno/avaliacoes/:entregaId/resultado`
- Skeleton durante carregamento
- `<ResultadoHeader>` + lista de `<GabaritoQuestaoCard>` + `<AnaliseIASection>`
- Botão "Voltar ao feed" → `/aluno/feed`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Adicionar rota `/aluno/avaliacoes/:entregaId/resultado` |

---

## Ordem de Implementação

```
1. Tipos adicionais
2. Extensão de aluno.service.ts
3. ResultadoHeader (estado disponível vs não disponível)
4. GabaritoQuestaoCard (objetiva, VF, dissertativa)
5. AnaliseIASection
6. ResultadoPage
7. Atualizar router
8. Testes: gabarito disponível vs não, dissertativa com correção, análise IA sem erros
```

---

## Checklist de Validação

- [ ] Nota + média quando gabarito disponível
- [ ] Gabarito mostra resposta do aluno vs correta
- [ ] Alternativa correta destacada visualmente
- [ ] "Aguardando correção" para dissertativas
- [ ] Análise IA com link funcional
- [ ] Observação do professor visível quando corrigida

---

## Resumo

- **5 arquivos** a criar (extensão service, tipos, 3 componentes, 1 página)
- **1 arquivo** a modificar (router)
- **Nenhuma dependência nova**
- **Complexidade mantida:** M
