# Criação com IA — Tela do Gerador de Atividades

> **Escopo:** criacao-ia  
> **Tipo:** Frontend  
> **Complexidade estimada:** M  
> **Depende de:** `criacao-ia_gerador-provas.md`, `backend-ia_endpoint-gerar-prova.md`

---

## Contexto

Similar ao gerador de provas, mas para atividades (sem timer obrigatório, com opções de tipo de entrega). Reutiliza grande parte do componente de geração.

---

## O que deve ser implementado

- Rota `/professor/criar/atividade`
- Reutilizar componentes do gerador de provas (painel de configuração + painel de preview)
- Diferenças em relação à prova:
  - Sem campo de duração obrigatória
  - Campo adicional: **Tipo de entrega** (Online / PDF para imprimir / Ambos)
  - Campo adicional: **Prazo de entrega** (datepicker com hora)
  - Suporte a tipos adicionais: Leitura com perguntas, Pesquisa com roteiro, Projeto com etapas

---

## Critérios de Aceite

- [ ] Todos os campos específicos de atividade presentes e funcionando
- [ ] Tipo de entrega "PDF para imprimir" gera preview em formato A4
- [ ] Prazo de entrega aceita data + hora
- [ ] Tipos de atividade adicionais renderizados corretamente

---

## Especificação de Referência

- **Arquivo:** `02-area-professor/02b-criacao-com-ia.md`
- **Seção:** `Tela: Gerador de Atividades`

---

## Definition of Done

- [ ] Código implementado reutilizando componentes da prova
- [ ] Testes dos campos específicos
- [ ] Code review realizado
