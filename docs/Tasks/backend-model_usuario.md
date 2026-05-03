# Backend Model — Tabelas de Usuário e Sessão

> **Escopo:** backend-model  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** Nenhuma

---

## Contexto

Modelos e migrations base da plataforma. Praticamente todas as outras tasks dependem destes. Devem ser implementados primeiro.

---

## O que deve ser implementado

**Migration: `escolas`** — id, nome, cnpj (unique), logo_url, nota_minima_aprovacao (decimal, default 5.0), frequencia_minima_aprovacao (decimal, default 75.0), sistema_avaliacao (enum), criado_em, atualizado_em

**Migration: `usuarios`** — id (uuid), escola_id (FK), nome, email (unique), senha_hash (nullable), perfil (enum: professor/aluno/responsavel/coordenador/admin), ativo (bool, default true), avatar_url, criado_em, ultimo_acesso. Índices em `email` e `(escola_id, perfil)`.

**Migration: `sessoes`** — id, usuario_id (FK), refresh_token_hash, user_agent, ip, criado_em, expira_em, revogado_em (nullable). Índice em `usuario_id`.

**Migration: `tokens_temporarios`** — id, usuario_id (FK), tipo (enum: convite/recuperacao_senha), token_hash (unique), expira_em, usado_em (nullable), criado_em.

**Models/Entities:** Criar as entidades correspondentes com os relacionamentos (Usuario pertence a Escola; Usuario tem muitas Sessoes).

---

## Critérios de Aceite

- [ ] Migrations rodam sem erros em ambiente limpo
- [ ] Migrations são reversíveis (down funciona)
- [ ] Índices criados corretamente
- [ ] Models/entities refletem os schemas com tipos corretos
- [ ] Enum de perfil aceita apenas os valores definidos

---

## Especificação de Referência

- **Arquivo:** `06-modelos-de-dados.md`
- **Seções:** `Tabela: escolas`, `Tabela: usuarios`

---

## Definition of Done

- [ ] Migrations implementadas e testadas
- [ ] Models com relacionamentos funcionando
- [ ] Testes de migration up e down
- [ ] Code review realizado
