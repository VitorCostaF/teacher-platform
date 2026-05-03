# Plano de Implementação — frequencia_lancamento

> **Task origem:** `docs/Tasks/frequencia_lancamento.md`
> **Escopo:** Frontend — Frequência
> **Complexidade:** G
> **Sprint:** 2 — Gestão de Turmas
> **Depende de:** `turmas_detalhe-e-alunos-plan.md`, `backend-frequencia_endpoint-lancar-plan.md`

---

## Contexto do Codebase

`TurmaDetalhePage`, `AlunoListItem`, `Button`, `ConfirmationModal`, `useToast`, `apiClient`, TanStack Query e React Router já existem. Esta task implementa a tela de lançamento de frequência com datepicker, toggle de 3 estados e botão sticky.

---

## Dependências a Adicionar

```bash
npm install react-datepicker
npm install -D @types/react-datepicker
```

---

## Arquivos a Criar

### Serviço

`frontend/src/features/frequencia/services/frequencia.service.ts`
```typescript
export const frequenciaService = {
  buscarPorData: (turmaId: number, data: string) =>
    apiClient.get(`/turmas/${turmaId}/frequencia`, { params: { data } }).then(r => r.data),
  lancar: (turmaId: number, payload: LancarFrequenciaDto) =>
    apiClient.post(`/turmas/${turmaId}/frequencia`, payload).then(r => r.data),
  editar: (turmaId: number, frequenciaId: number, payload: LancarFrequenciaDto) =>
    apiClient.put(`/turmas/${turmaId}/frequencia/${frequenciaId}`, payload).then(r => r.data),
}
```

### Tipos

`frontend/src/features/frequencia/types.ts`
```typescript
export type StatusFrequencia = 'PRESENTE' | 'FALTA' | 'FALTA_JUSTIFICADA'
export interface FrequenciaAluno {
  alunoId: string; status: StatusFrequencia | null; observacao: string
}
export interface LancarFrequenciaDto { data: string; alunos: FrequenciaAluno[] }
```

### Componentes

`frontend/src/features/frequencia/components/FrequenciaToggle.tsx`
- 3 botões: Presente (verde) | Falta (vermelho) | F.Justificada (amarelo)
- Estado selecionado com fundo preenchido; outros com borda apenas
- Prop: `value: StatusFrequencia | null`, `onChange: (v: StatusFrequencia) => void`
- Mobile-friendly: botões grandes, `min-h-11` cada

`frontend/src/features/frequencia/components/AlunoFrequenciaRow.tsx`
- Foto, nome do aluno
- `<FrequenciaToggle>` inline
- Botão de expandir (▾) → mostra `<textarea>` para observação
- Indica se está "preenchido" com cheque visual

`frontend/src/features/frequencia/components/FrequenciaStickyFooter.tsx`
- Fixo no `bottom-0` da tela
- Contador "X de Y alunos registrados"
- Botão "Salvar" → `disabled` se contador < total alunos
- Botão "Marcar todos como presentes" (secundário)
- Background branco com sombra para separar da lista

`frontend/src/features/frequencia/components/FrequenciaDatePicker.tsx`
- Usa `react-datepicker`
- `maxDate = hoje`
- `minDate = turma.inicioEm`
- Badge "Frequência já lançada" quando data tem registro existente

### Hook

`frontend/src/features/frequencia/hooks/useFrequencia.ts`
- Gerencia: data selecionada, mapa `{ [alunoId]: FrequenciaAluno }`, id do registro existente
- `mudarData(novaData)` → verifica se há alterações não salvas; se sim, abre `ConfirmationModal` antes de prosseguir
- `marcarTodosPresentes()` → atualiza todo o mapa para PRESENTE
- `isTodoPreenchido()` → verifica se todos têm status definido
- `salvar()` → chama `lancar` ou `editar` baseado se existe registro; retorna ao detalhe da turma com toast de sucesso

### Página

`frontend/src/features/frequencia/pages/LancamentoFrequenciaPage.tsx`
- Rota: `/professor/turmas/:turmaId/frequencia`
- Carrega lista de alunos da turma
- Pré-carrega frequência ao selecionar uma data
- Badge "Frequência já lançada" mostrado no seletor quando data tem registro
- Aviso suave (não bloqueante) se turma não tem aula no dia selecionado

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Adicionar rota `/professor/turmas/:turmaId/frequencia` |

---

## Ordem de Implementação

```
1. Instalar react-datepicker
2. types.ts + frequencia.service.ts
3. FrequenciaToggle (componente mais simples, base dos outros)
4. AlunoFrequenciaRow (usa FrequenciaToggle)
5. FrequenciaStickyFooter
6. FrequenciaDatePicker
7. useFrequencia hook (gerenciar estado complexo)
8. LancamentoFrequenciaPage
9. Atualizar router
10. Testes: pré-carregamento de dados existentes, "marcar todos presentes", modal ao trocar data com dados
```

---

## Checklist de Validação

- [ ] Datepicker não permite datas futuras nem antes do início da turma
- [ ] Data com registro existente: dados pré-carregados + badge
- [ ] Toggle funciona entre 3 estados
- [ ] Botão "Marcar todos presentes" preenche todos
- [ ] Contador correto; botão Salvar desabilitado com pendentes
- [ ] Modal ao trocar data com alterações não salvas
- [ ] Toast + redirect após salvar
- [ ] Layout responsivo (foco mobile)

---

## Resumo

- **8 arquivos** a criar (service, types, 4 componentes, 1 hook, 1 página)
- **1 arquivo** a modificar (router)
- **Dependência a adicionar:** react-datepicker
- **Complexidade mantida:** G
