# Plano de Implementação — global_acoes-destrutivas

> **Task origem:** `docs/Tasks/global_acoes-destrutivas.md`
> **Escopo:** Frontend — Global
> **Complexidade:** P
> **Sprint:** 0 — Fundação
> **Depende de:** Nenhuma

---

## Contexto do Codebase

Componentes `Button` e `Input` já existem em `src/components/ui/`. Esta task cria o componente `ConfirmationModal` reutilizável que será usado em toda a aplicação para ações que apagam ou modificam dados irreversivelmente.

---

## Componentes Existentes para Reutilizar

| Item | Caminho | Por que reutilizar |
|------|---------|-------------------|
| `Button` | `src/components/ui/Button.tsx` | Botões de confirmar (destructive) e cancelar (secondary) |
| `Input` | `src/components/ui/Input.tsx` | Campo de confirmação textual para nível crítico |

---

## Arquivos a Criar

### Componente Principal

`frontend/src/components/ui/ConfirmationModal.tsx`
```typescript
interface ConfirmationModalProps {
  isOpen: boolean
  title: string
  description: string           // consequências da ação em linguagem clara
  nivel: 'medio' | 'alto' | 'critico'
  confirmationText?: string     // texto que o usuário deve digitar (nível crítico)
  confirmLabel?: string         // texto do botão confirmar (default: "Confirmar")
  isLoading?: boolean           // loading durante a ação
  error?: string | null         // erro inline quando ação falha
  onConfirm: () => void | Promise<void>
  onCancel: () => void
}
```

**Comportamentos:**
- Ao abrir: foco vai automaticamente para o botão "Cancelar" (`autoFocus`)
- Escape fecha o modal apenas se `!isLoading`
- Clique no backdrop fecha apenas se `!isLoading`
- Se `nivel === 'critico'`: renderizar `<Input>` onde usuário digita `confirmationText`; botão confirmar `disabled` até o texto bater exatamente
- Se `isLoading`: botão confirmar mostra spinner; botão cancelar disabled; Escape bloqueado
- Se `error`: exibir `<ErrorBanner>` dentro do modal (não fechar)
- Botão confirmar: `variant="destructive"` (vermelho)
- Botão cancelar: `variant="secondary"` (cinza)
- Usar `<dialog>` nativo ou portal React para acessibilidade (aria-modal, role="alertdialog")

### Hook utilitário

`frontend/src/hooks/useConfirmationModal.ts`
```typescript
// Simplifica uso do modal: useConfirmationModal() retorna
// { open(props), close(), modalProps }
// Evita ter que gerenciar isOpen em cada componente que usa modal
```

---

## Arquivos de Referência (apenas leitura)

| Arquivo | Por que consultar |
|---------|------------------|
| `src/components/ui/Button.tsx` | Props variant (destructive, secondary) e loading |
| `src/components/ui/Input.tsx` | Como renderizar o campo de confirmação |
| `src/components/feedback/ErrorBanner.tsx` | Exibir erro inline no modal |

---

## Ordem de Implementação

```
1. ConfirmationModal.tsx — estrutura base e estilos Tailwind
2. Lógica de nível crítico (campo de digitação + habilitar botão)
3. Acessibilidade: focus trap, aria-modal, Escape handler
4. useConfirmationModal hook
5. Testes: 3 níveis, campo de confirmação, estado de loading, erro inline
6. Testes de acessibilidade: navegação por teclado
```

---

## Checklist de Validação

- [ ] Modal exibe consequências da ação claramente
- [ ] Nível crítico: botão só habilita com texto correto digitado
- [ ] Modal permanece aberto com erro inline em caso de falha
- [ ] Escape fecha apenas se não há ação em andamento
- [ ] Foco inicial no botão "Cancelar"
- [ ] Funciona com navegação por teclado (Tab, Shift+Tab, Enter, Escape)

---

## Resumo

- **2 arquivos** a criar (ConfirmationModal, useConfirmationModal)
- **0 arquivos** a modificar
- **Nenhuma dependência nova**
- **Complexidade mantida:** P
