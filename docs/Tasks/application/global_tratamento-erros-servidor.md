# Global — Tratamento de Erros de Servidor e Feedback Visual

> **Escopo:** global  
> **Tipo:** Frontend  
> **Complexidade estimada:** M  
> **Depende de:** `login_validacao-e-estados-de-erro.md`

---

## Contexto

Padronizar como todos os erros de API são apresentados para o usuário em toda a plataforma. Erros de campo são inline; erros de ação são toast; erros críticos são banners.

---

## O que deve ser implementado

- Interceptor HTTP global que processa erros antes de chegarem nos componentes
- Toast component reutilizável: canto superior direito, duração 5s, botão X, fila de múltiplos toasts
- Banner de erro inline: para erros críticos que impedem uso (com botão retry)
- Mapeamento de status codes para mensagens padronizadas em português
- Timeout de 15 segundos em todas as requisições; exibir mensagem de timeout
- Loading states globais: spinner para ações pontuais, skeleton para dados iniciais, loading bar para navegação

---

## Critérios de Aceite

- [ ] Toast aparece no canto superior direito para erros 403/404/500/503
- [ ] Toast tem botão X para fechar manualmente
- [ ] Múltiplos toasts ficam em fila (não sobrepostos)
- [ ] Erro 400/422 com campos: erros exibidos inline por campo
- [ ] Requisição que não responde em 15s exibe mensagem de timeout
- [ ] Nenhuma mensagem técnica (stack trace, IDs internos) é exibida para o usuário
- [ ] Loading bar aparece durante navegação entre páginas

---

## Especificação de Referência

- **Arquivo:** `07-comportamentos-globais.md`
- **Seção:** `5. Tratamento de Erros de Servidor` e `4. Estados de Carregamento`

---

## Definition of Done

- [ ] Interceptor implementado e cobrindo todos os status codes
- [ ] Toast component testado com múltiplos erros simultâneos
- [ ] Testes de timeout
- [ ] Code review realizado
