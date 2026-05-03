# Plano de Implementação — responsavel_painel-e-boletim

> **Task origem:** `docs/Tasks/responsavel_painel-e-boletim.md`
> **Escopo:** Frontend — Área do Responsável
> **Complexidade:** M
> **Sprint:** 6 — Responsável e Administração
> **Depende de:** `backend-responsavel_endpoints-plan.md`

---

## Contexto do Codebase

`apiClient`, `authStore`, `Skeleton`, `Button`, `recharts` (instalado), TanStack Query disponíveis. Esta task implementa a área do responsável com foco mobile-first.

---

## Arquivos a Criar

### Serviço

`frontend/src/features/responsavel/services/responsavel.service.ts`
```typescript
export const responsavelService = {
  getAlunos: () => apiClient.get<AlunoResponsavel[]>('/responsavel/alunos').then(r => r.data),
  getPainel: (alunoId: string) => apiClient.get(`/responsavel/alunos/${alunoId}/painel`).then(r => r.data),
  getBoletim: (alunoId: string, periodo?: string) =>
    apiClient.get(`/responsavel/alunos/${alunoId}/boletim`, { params: { periodo } }).then(r => r.data),
  getFrequencia: (alunoId: string) =>
    apiClient.get(`/responsavel/alunos/${alunoId}/frequencia`).then(r => r.data),
  getCalendario: (alunoId: string) =>
    apiClient.get(`/responsavel/alunos/${alunoId}/calendario`).then(r => r.data),
}
```

### Tipos

`frontend/src/features/responsavel/types.ts`
```typescript
export interface AlunoResponsavel { id: string; nome: string; avatarUrl?: string }
export interface PainelData { mediaGeral: number; percentualFrequencia: number; proximaProva?: any; alertasAtivos: AlertaItem[] }
export interface BoletimDisciplina { disciplina: string; notas: (number | null)[]; mediaFinal: number | null; situacao: string }
export interface FrequenciaCalendarioDia { data: string; status: 'PRESENTE' | 'FALTA' | 'FALTA_JUSTIFICADA' | 'SEM_AULA' }
```

### Componentes

`frontend/src/features/responsavel/components/AlunoSelector.tsx`
- Dropdown para selecionar entre filhos vinculados
- Se apenas 1 filho: não renderiza dropdown (seleção automática)
- Persiste seleção em sessionStorage

`frontend/src/features/responsavel/components/PainelCardsResponsavel.tsx`
- Cards: Média Geral, % Frequência, Próxima Prova
- Alertas ativos clicáveis em lista abaixo
- Links de acesso rápido: Boletim | Frequência | Calendário

`frontend/src/features/responsavel/components/BoletimTable.tsx`
- Select de período (bimestre/semestre/ano)
- Tabela: Disciplina | B1 | B2 | B3 | B4 | Média Final | Situação
- Células de bimestres futuros: traço (—), não vazio
- Toggle para gráfico de linha por disciplina (usa recharts)

`frontend/src/features/responsavel/components/FrequenciaCalendario.tsx`
- Grade mensal (7 colunas × semanas)
- Cores: verde (presente), vermelho (falta), amarelo (just.), cinza (sem aula)
- Select de disciplina acima
- Banner vermelho no topo se frequência < 75%: "Atenção: frequência abaixo de 75%"
- Barra de progresso com linha de risco em 75%

`frontend/src/features/responsavel/components/FrequenciaResumo.tsx`
- Número de presenças / total de aulas + percentual
- Barra de progresso com cor por faixa

`frontend/src/features/responsavel/components/CalendarioProvas.tsx`
- Lista de provas futuras + histórico
- Nota exibida se disponível

### Páginas

`frontend/src/features/responsavel/pages/PainelResponsavelPage.tsx`
- Rota: `/responsavel/acompanhamento`
- `<AlunoSelector>` se múltiplos filhos
- Tabs: Painel | Boletim | Frequência | Calendário

`frontend/src/features/responsavel/pages/BoletimPage.tsx`
- Rota: `/responsavel/alunos/:alunoId/boletim`

`frontend/src/features/responsavel/pages/FrequenciaResponsavelPage.tsx`
- Rota: `/responsavel/alunos/:alunoId/frequencia`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Rotas da área do responsável |

---

## Ordem de Implementação

```
1. types.ts + responsavel.service.ts
2. AlunoSelector
3. PainelCardsResponsavel
4. BoletimTable (com toggle de gráfico)
5. FrequenciaCalendario (grade mensal + cores)
6. FrequenciaResumo + CalendarioProvas
7. PainelResponsavelPage (tabs)
8. Atualizar router
9. Testes: dropdown ausente com 1 filho, células bimestre futuro, banner frequência < 75%
```

---

## Checklist de Validação

- [ ] Dropdown só aparece com mais de um filho
- [ ] Cards do painel atualizados
- [ ] Células vazias para bimestres futuros
- [ ] Barra de progresso com linha em 75%
- [ ] Calendário: cores corretas por status
- [ ] Banner de risco quando < 75%
- [ ] Responsável sem acesso a edição

---

## Resumo

- **9 arquivos** a criar (service, types, 5 componentes, 2 páginas + routes extras)
- **1 arquivo** a modificar (router)
- **Nenhuma dependência nova**
- **Complexidade mantida:** M
