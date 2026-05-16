# Testes Unitários — AlertaFrequenciaService: Disparo de Alertas e Preferências

> **Escopo:** Backend — `AlertaFrequenciaService`  
> **Tipo:** Backend  
> **Complexidade estimada:** P  
> **Depende de:** Nenhuma

---

## Contexto

`AlertaFrequenciaService` envia notificações push para responsáveis quando um aluno atinge critérios de risco de frequência. É assíncrono (`@Async`) — nos testes deve ser executado de forma síncrona. As preferências do responsável devem ser respeitadas: se desabilitadas, nenhuma notificação é enviada.

---

## O que deve ser implementado

Criar `AlertaFrequenciaServiceTest` cobrindo os dois critérios de alerta (percentual < 75% e 3 faltas consecutivas) e a lógica de preferências por responsável.

---

## Critérios de Aceite

**Critério de frequência baixa:**
- [ ] Percentual < 75.0 → `pushNotificationService.enviar` chamado para cada responsável com subscription
- [ ] Percentual = 75.0 → `enviar` **não** chamado
- [ ] Percentual > 75.0 → `enviar` **não** chamado
- [ ] Percentual = 0.0 → `enviar` chamado

**Critério de faltas consecutivas:**
- [ ] `tresFaltasConsecutivas = true` → `enviar` chamado para cada responsável
- [ ] `tresFaltasConsecutivas = false` → `enviar` **não** chamado

**Ambos os critérios ativos:**
- [ ] `percentual = 50, consecutivas = true` → `enviar` chamado 2x por responsável (uma por critério)

**Preferências do responsável:**
- [ ] Sem preferências cadastradas (`null`) → `enviar` chamado (padrão: tudo habilitado)
- [ ] `faltaAluno = true` → `enviar` chamado para alerta de consecutivas
- [ ] `faltaAluno = false` → `enviar` **não** chamado para alerta de consecutivas
- [ ] `quedaFrequencia = true` → `enviar` chamado para alerta de frequência
- [ ] `quedaFrequencia = false` → `enviar` **não** chamado para alerta de frequência

**Iteração por responsável:**
- [ ] Sem subscriptions → `enviar` não chamado, sem exceção
- [ ] 2 responsáveis → `enviar` chamado 2x para cada critério ativo

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/AlertaFrequenciaService.md`
- **Seções:** `verificarAlertas`, `Lógica de Preferências`, `Conteúdo das Notificações`

---

## Detalhes Técnicos

**Localização:** `src/test/java/br/com/inovadados/teacherplatform/service/AlertaFrequenciaServiceTest.java`

**Problema do @Async:** Chamar o método diretamente nos testes unitários executa de forma síncrona (sem executor assíncrono). Não é necessário nenhuma configuração especial para testes unitários com Mockito puro.

**Helper:**
```java
private PushSubscription subFake(Long id, UUID responsavelId) {
    var sub = new PushSubscription();
    sub.setId(id);
    var usuario = new Usuario(); usuario.setId(responsavelId);
    sub.setUsuario(usuario);
    return sub;
}
```

---

## Notas e Edge Cases

- O limite é `< 75.0` — exatamente 75% não dispara alerta
- `preferenciasRepository.findByUsuarioId` retorna `Optional<PreferenciasNotificacao>` — mockar com `Optional.empty()` para simular sem preferências
- O envio usa o `sub.getId()` como identificador — não o `usuarioId`

---

## Definition of Done

- [ ] Classe `AlertaFrequenciaServiceTest` criada
- [ ] Todos os cenários cobertos (mínimo 16 testes)
- [ ] Testes passam com `./mvnw test -Dtest=AlertaFrequenciaServiceTest`
