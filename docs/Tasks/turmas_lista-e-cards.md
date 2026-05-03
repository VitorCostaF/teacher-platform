# Turmas — Tela de Lista de Turmas do Professor

> **Escopo:** turmas  
> **Tipo:** Frontend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-turmas_endpoint-listar.md`

---

## Contexto

Tela inicial da área do professor. Exibe todas as turmas que ele leciona no período letivo ativo, com indicadores de pendências (frequência não lançada, atividades para corrigir).

---

## O que deve ser implementado

- Criar rota `/professor/turmas`
- Chamar `GET /professor/turmas?periodo=:id` ao montar
- Exibir grid de cards; cada card contém: nome da turma, número de alunos, próxima aula agendada, badges de pendências (frequências não lançadas, atividades não corrigidas)
- Seletor de período letivo (dropdown) — ao trocar, recarregar os dados
- Campo de busca que filtra cards em tempo real pelo nome (client-side)
- Botão "Nova Turma" — visível apenas para Admin
- Estado de loading com skeleton nos cards
- Estado vazio: ilustração + "Nenhuma turma cadastrada neste período"
- Estado de busca sem resultado: "Nenhuma turma encontrada"

---

## Critérios de Aceite

- [ ] Cards exibem todas as informações corretamente
- [ ] Badges de pendências aparecem apenas quando há pendências
- [ ] Troca de período letivo recarrega os dados
- [ ] Busca filtra em tempo real sem requisição ao servidor
- [ ] Botão "Nova Turma" visível apenas para Admin
- [ ] Skeleton exibido durante carregamento inicial
- [ ] Estado vazio exibido quando não há turmas

---

## Especificação de Referência

- **Arquivo:** `02-area-professor/02a-gestao-turmas.md`
- **Seção:** `Tela: Lista de Turmas`

---

## Definition of Done

- [ ] Código implementado e funcionando conforme critérios de aceite
- [ ] Testes do componente de card e do filtro
- [ ] Testado em desktop e mobile
- [ ] Code review realizado
