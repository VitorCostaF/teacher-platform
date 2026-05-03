# Plano de Implementação — dashboard_professor-visao-geral

> **Task origem:** `docs/Tasks/dashboard_professor-visao-geral.md`
> **Escopo:** Frontend — Dashboard do Professor
> **Complexidade:** G
> **Sprint:** 5 — Dashboards
> **Depende de:** `backend-dashboard_endpoint-professor-plan.md`

---

## Contexto do Codebase

`apiClient`, `Button`, `Skeleton`, `useToast`, TanStack Query disponíveis. Esta task introduz bibliotecas de gráficos pela primeira vez no projeto.

---

## Dependências a Adicionar

```bash
npm install recharts
npm install -D @types/recharts
```

---

## Arquivos a Criar

### Serviço

`frontend/src/features/dashboard/services/dashboard.service.ts`
```typescript
export const dashboardService = {
  getProfessor: () =>
    apiClient.get<DashboardProfessorData>('/professor/dashboard').then(r => r.data),
}
```

### Tipos

`frontend/src/features/dashboard/types.ts`
```typescript
export interface DashboardProfessorData {
  alertas: AlertaItem[]; turmas: TurmaDashboard[]
  mediasHistoricas: MediaHistorica[]; mapaCalor: MapaCalorItem[]
}
export interface AlertaItem {
  tipo: string; descricao: string; referenciaId: number; referenciaUrl: string
}
export interface MapaCalorItem { turma: string; topico: string; percentualErros: number }
```

### Hook

`frontend/src/features/dashboard/hooks/useDashboardProfessor.ts`
- `useQuery` com `queryKey: ['dashboard', 'professor']`
- Refetch via polling: `refetchInterval: 30000` — mas só ativar se há prova em andamento (`data.temProvaAtiva`)

### Componentes

`frontend/src/features/dashboard/components/AlertasSection.tsx`
- Cards de alerta com cor por tipo (amarelo = aviso, vermelho = urgente)
- Clicáveis (navega para `referenciaUrl`)
- Botão X para descartar — guarda descartados em `Set` local (sessionStorage para persistir na sessão)
- Estados: loading (skeleton de 3 cards), vazio ("Nenhuma pendência")

`frontend/src/features/dashboard/components/TurmasDashboardTable.tsx`
- Tabela: Turma | Média | % Frequência | Alunos em alerta | Última atividade
- Cabeçalhos clicáveis para ordenação (asc/desc por coluna)
- Linha clicável → navega para detalhe da turma

`frontend/src/features/dashboard/components/MediasHistoricasChart.tsx`
- Gráfico de linha via Recharts (`LineChart`)
- Uma linha por turma, cores distintas
- Seletor de período: Mês | Bimestre | Semestre | Ano (muda query)
- Tooltip customizado com nome da turma e nota
- Botão "Exportar PNG": `canvas.toDataURL()` do container do gráfico

`frontend/src/features/dashboard/components/MapaCalorChart.tsx`
- Grid customizado (não usa Recharts): CSS Grid com células coloridas
- Gradiente: verde (< 30% erros) → amarelo (30-60%) → vermelho (> 60%)
- Tooltip `<div>` posicionado no hover exibindo: topico, turma, % erros
- Se muitas combinações (> 100 células): virtualização via `windowing` simples (renderizar apenas células visíveis)

### Página

`frontend/src/features/dashboard/pages/DashboardProfessorPage.tsx`
- Rota: `/professor/dashboard`
- Skeleton de cada seção durante carregamento
- Layout vertical: Alertas → Tabela Turmas → Gráfico Médias → Mapa de Calor
- Estado "sem dados" para professor sem provas publicadas

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Adicionar rota `/professor/dashboard` como rota padrão do professor |

---

## Ordem de Implementação

```
1. Instalar recharts
2. types.ts + dashboard.service.ts
3. useDashboardProfessor hook
4. AlertasSection (skeleton + descartar)
5. TurmasDashboardTable (ordenação)
6. MediasHistoricasChart (Recharts LineChart)
7. MapaCalorChart (CSS Grid + tooltip)
8. DashboardProfessorPage
9. Atualizar router
10. Testes: descartar alerta não reaparece, polling só com prova ativa, export PNG
```

---

## Checklist de Validação

- [ ] Alertas clicáveis e com X funcional
- [ ] Alerta descartado não reaparece na sessão
- [ ] Gráfico de linha com seletor de período
- [ ] Mapa de calor cores corretas
- [ ] Tooltip do mapa de calor preciso
- [ ] Gráficos exportáveis como PNG
- [ ] Estado "sem dados" para professor novo

---

## Resumo

- **7 arquivos** a criar (service, types, hook, 4 componentes, 1 página)
- **1 arquivo** a modificar (router)
- **Dependência a adicionar:** recharts
- **Complexidade mantida:** G
