# Global — Padrão de Confirmação para Ações Destrutivas

> **Escopo:** global  
> **Tipo:** Frontend  
> **Complexidade estimada:** P  
> **Depende de:** Nenhuma

---

## Contexto

Toda ação que resulta em perda de dados ou é de difícil reversão deve exigir confirmação explícita do usuário, com modal padronizado que descreve as consequências.

---

## O que deve ser implementado

- Componente `ConfirmationModal` reutilizável que aceita: título, descrição das consequências, nível (médio/alto/crítico), campo de confirmação textual (opcional), callback de confirmação e cancelamento
- Para nível "crítico": exibir input onde o usuário deve digitar a palavra/texto exato de confirmação antes de habilitar o botão
- Botão de confirmação: vermelho com texto afirmativo
- Botão cancelar: cinza
- Loading no botão de confirmação durante a ação
- Manter modal aberto em caso de erro (não fechar até sucesso)

---

## Critérios de Aceite

- [ ] Modal exibe consequências da ação de forma clara
- [ ] Para nível crítico: botão de confirmar só habilita quando texto correto digitado
- [ ] Modal permanece aberto com erro inline em caso de falha na ação
- [ ] Tecla Escape fecha o modal apenas se não houver ação em andamento
- [ ] Foco vai para o botão "Cancelar" ao abrir o modal (evitar confirmação acidental)
- [ ] Componente documentado com exemplos de uso

---

## Especificação de Referência

- **Arquivo:** `07-comportamentos-globais.md`
- **Seção:** `3. Ações Destrutivas`

---

## Definition of Done

- [ ] Componente implementado e documentado
- [ ] Testes dos 3 níveis de confirmação
- [ ] Testado com teclado (acessibilidade)
- [ ] Code review realizado
