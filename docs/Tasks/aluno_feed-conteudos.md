# Área do Aluno — Feed de Conteúdos

> **Escopo:** aluno  
> **Tipo:** Frontend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-aluno_endpoint-feed.md`

---

## Contexto

Tela inicial do aluno após o login. Exibe atividades urgentes, pendências, novos conteúdos e recomendações da IA, priorizando o que requer atenção imediata.

---

## O que deve ser implementado

- Rota `/aluno/feed` (mobile-first)
- Seção "Urgente": cards em amarelo/vermelho para provas/atividades com prazo em menos de 24h
- Seção "Para fazer": cards de avaliações disponíveis, ordenados por prazo, com tipo e status
- Seção "Novos conteúdos": cards de materiais com tipo (texto/vídeo/PDF) e tempo de leitura estimado
- Seção "Recomendados pela IA": cards com badge "Sugerido para você" e explicação contextual
- Badge de notificações no header com contagem
- Exibição de pontos acumulados (gamificação) no header
- Barra de navegação inferior: Feed | Atividades | Desempenho | Perfil
- Skeleton durante carregamento

---

## Critérios de Aceite

- [ ] Seção "Urgente" aparece apenas quando há prazo em menos de 24h
- [ ] Cards de avaliação exibem prazo, tipo, disciplina e status corretamente
- [ ] Badge de notificação mostra contagem correta
- [ ] Recomendações exibem texto explicativo contextual ("Você teve dificuldade em X")
- [ ] Navegação inferior fixa no bottom em mobile
- [ ] Layout funciona em telas a partir de 375px

---

## Especificação de Referência

- **Arquivo:** `03-area-aluno.md`
- **Seção:** `Tela: Feed de Conteúdos`

---

## Definition of Done

- [ ] Código implementado e funcionando
- [ ] Testado em dispositivos mobile reais (iOS e Android)
- [ ] PWA testado (offline parcial funcional)
- [ ] Code review realizado
