# Plano de Implementação — dashboard_desempenho-turma-e-aluno

> **Task origem:** `docs/Tasks/dashboard_desempenho-turma-e-aluno.md`
> **Escopo:** Frontend — Dashboard do Professor
> **Complexidade:** G
> **Sprint:** 5 — Dashboards
> **Depende de:** `backend-dashboard_endpoint-professor-plan.md`, `turmas_detalhe-e-alunos-plan.md`

---

## Contexto do Codebase

`recharts` já instalado, `dashboardService`, `TurmaTabNavigation` (abas), `Button`, `Skeleton` disponíveis. Esta task implementa os dois subpainéis de desempenho detalhado.

---

## Arquivos a Criar

### Serviço (extensão)

Adicionar ao `frontend/src/features/dashboard/services/dashboard.service.ts`:
```typescript
getDesempenhoTurma: (turmaId: number, periodo?: string) =>
  apiClient.get<DesempenhoTurmaData>(`/turmas/${turmaId}/desempenho`, { params: { periodo } }).then(r => r.data),
getDesempenhoAluno: (turmaId: number, alunoId: string) =>
  apiClient.get<DesempenhoAlunoData>(`/turmas/${turmaId}/alunos/${alunoId}/desempenho`).then(r => r.data),
criarObservacao: (turmaId: number, alunoId: string, texto: string) =>
  apiClient.post(`/turmas/${turmaId}/alunos/${alunoId}/observacoes`, { texto }),
exportarPDF: (tipo: 'turma' | 'aluno', id: number | string) =>
  apiClient.post(`/relatorios/${tipo}/${id}/pdf`).then(r => r.data),
```

### Tipos adicionais

Adicionar ao `frontend/src/features/dashboard/types.ts`:
```typescript
export interface DesempenhoTurmaData {
  mediaGeral: number; maiorNota: number; menorNota: number
  percentualAprovacao: number; percentualFrequencia: number
  histograma: Array<{ faixa: string; quantidade: number; alunos: AlunoResumo[] }>
  ranking: Array<{ aluno: AlunoResumo; media: number; frequencia: number; tendencia: 'UP' | 'DOWN' | 'STABLE' }>
}
export interface DesempenhoAlunoData {
  situacao: string; evolucaoNotas: Array<{ avaliacaoNome: string; nota: number }>
  frequenciaMensal: Array<{ mes: string; percentual: number }>
  topicosAcerto: Array<{ topico: string; percentual: number }>
  observacoes: Array<{ texto: string; criadoEm: string }>
}
```

### Componentes de Desempenho por Turma

`frontend/src/features/dashboard/components/HistogramaNotas.tsx`
- `BarChart` do Recharts com 5 faixas (0-2, 2-4, 4-6, 6-8, 8-10)
- Barras clicáveis: ao clicar, exibe lista de alunos naquela faixa em painel lateral ou modal

`frontend/src/features/dashboard/components/RankingAlunosTable.tsx`
- Tabela ordenável por nome/média/frequência/tendência
- Cabeçalhos com ícone de sort
- Linha de aluno em risco: fundo vermelho suave (`bg-red-50`)
- Ícone de tendência: ↑ (verde), ↓ (vermelho), → (cinza)
- Clique na linha → navega para perfil do aluno

### Componentes de Desempenho por Aluno

`frontend/src/features/dashboard/components/SituacaoBanner.tsx`
- Banner de situação no topo: "Aprovado em Andamento" (verde), "Em Risco" (amarelo), "Reprovado por Falta" (vermelho)

`frontend/src/features/dashboard/components/EvolucaoNotasChart.tsx`
- `LineChart` Recharts com linha de referência em 5.0 (linha pontilhada horizontal)
- Tooltip com nome da avaliação e nota

`frontend/src/features/dashboard/components/FrequenciaMensalChart.tsx`
- `BarChart` Recharts com barras por mês
- Linha de referência em 75% (linha pontilhada horizontal vermelha)
- Colunas abaixo de 75% em vermelho, acima em verde

`frontend/src/features/dashboard/components/TopicosAcertoList.tsx`
- Lista ordenada por % de acerto (do menor para o maior)
- Barra de progresso inline colorida: < 40% vermelho, 40-70% amarelo, > 70% verde
- Clique em tópico → link para flashcards do tópico

`frontend/src/features/dashboard/components/ObservacoesSection.tsx`
- Lista de observações com data
- Input + botão para adicionar nova observação (apenas professor e coordenador)
- Se perfil RESPONSAVEL: não renderizar esta seção

### Páginas

`frontend/src/features/dashboard/pages/DesempenhoTurmaPage.tsx`
- Rota: `/professor/turmas/:turmaId/desempenho`
- Integrada ao `TurmaTabNavigation` (aba "Desempenho" ativa)
- Cards de métricas no topo, depois histograma, depois ranking

`frontend/src/features/dashboard/pages/DesempenhoAlunoPage.tsx`
- Rota: `/professor/turmas/:turmaId/alunos/:alunoId/desempenho`
- Header: foto, nome do aluno, `SituacaoBanner`
- Seções: evolução notas, frequência mensal, tópicos, observações
- Botão "Exportar PDF" → chama `dashboardService.exportarPDF()` → toast "PDF gerado. Você será notificado quando estiver pronto."

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Rotas `/professor/turmas/:turmaId/desempenho` e `.../alunos/:alunoId/desempenho` |

---

## Ordem de Implementação

```
1. Tipos adicionais
2. Extensão dashboardService
3. HistogramaNotas (BarChart)
4. RankingAlunosTable (ordenação, destaque em risco)
5. SituacaoBanner
6. EvolucaoNotasChart (linha de referência 5.0)
7. FrequenciaMensalChart (linha de referência 75%, cor por faixa)
8. TopicosAcertoList
9. ObservacoesSection (com permissão por perfil)
10. DesempenhoTurmaPage
11. DesempenhoAlunoPage
12. Atualizar router
13. Testes: ordenação do ranking, clique no histograma, observações não visíveis para responsável
```

---

## Checklist de Validação

- [ ] Histograma clicável lista alunos da faixa
- [ ] Tabela ordenável por todas as colunas
- [ ] Alunos em risco com fundo vermelho suave
- [ ] Evolução com linha de referência em 5.0
- [ ] Frequência com linha de referência em 75%
- [ ] Observações não visíveis para responsável
- [ ] Exportação PDF dispara notificação

---

## Resumo

- **11 arquivos** a criar (extensão service, tipos, 6 componentes, 2 páginas)
- **1 arquivo** a modificar (router)
- **Nenhuma dependência nova** (recharts já instalado)
- **Complexidade mantida:** G
