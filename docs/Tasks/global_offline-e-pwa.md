# Global — Suporte Offline e Configuração de PWA

> **Escopo:** global  
> **Tipo:** Frontend  
> **Complexidade estimada:** G  
> **Depende de:** `aluno_realizacao-atividade.md`

---

## Contexto

A plataforma deve funcionar em regiões com internet instável. O Service Worker deve cachear conteúdos visitados e as respostas de prova/atividade devem ser preservadas em caso de queda de conexão.

---

## O que deve ser implementado

- Configurar PWA: manifest.json com nome, ícones, cores, `display: standalone`, `start_url`
- Implementar Service Worker com estratégia de cache:
  - Cache-first: assets estáticos (JS, CSS, imagens)
  - Network-first: dados de API (com fallback para cache)
  - Nunca cachear: dados sensíveis de sessão, tokens, respostas de prova em tempo real
- Detector de conexão: monitorar eventos `online`/`offline`; exibir banner no topo com status
- Ao reconectar: sincronizar rascunhos salvos em localStorage (atividades e provas)
- Cachear via Service Worker: conteúdos acessados recentemente, último estado do dashboard

---

## Critérios de Aceite

- [ ] App instalável como PWA em Android e iOS
- [ ] Banner de offline exibido em < 1s após perda de conexão
- [ ] Banner de reconexão exibido em verde por 3s após retorno
- [ ] Conteúdos já acessados disponíveis offline
- [ ] Rascunhos de atividade preservados no localStorage ao ficar offline
- [ ] Ao reconectar, rascunhos são sincronizados automaticamente
- [ ] Tokens e dados de sessão nunca cacheados pelo Service Worker

---

## Especificação de Referência

- **Arquivo:** `07-comportamentos-globais.md`
- **Seção:** `2. Perda de Conexão / Modo Offline`

---

## Notas e Edge Cases

- iOS tem limitações de Service Worker — testar exaustivamente no Safari
- A sincronização de rascunho deve ser idempotente (reenvio seguro)

---

## Definition of Done

- [ ] PWA instalável e testado em Android e iOS
- [ ] Service Worker com estratégias de cache corretas
- [ ] Cenário offline testado com throttling de rede no DevTools
- [ ] Code review realizado
