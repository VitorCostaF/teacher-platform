# Spec de Testes Unitários — AlertaFrequenciaService

**Classe:** `br.com.inovadados.teacherplatform.service.AlertaFrequenciaService`  
**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/AlertaFrequenciaService.java`

---

## Visão Geral

`AlertaFrequenciaService` envia notificações push para responsáveis quando um aluno atinge critérios de alerta de frequência. A lógica central é a decisão de disparo com base no percentual de presença e faltas consecutivas, respeitando as preferências do responsável.

**Dependências para mock:** `PushNotificationService`, `PreferenciasNotificacaoRepository`, `PushSubscriptionRepository`.

> **Atenção:** o método `verificarAlertas` é `@Async` — nos testes unitários, executar de forma síncrona usando `@SpringBootTest` com `@Async` desabilitado ou testando a lógica interna via método privado extraído.

---

## Método: `verificarAlertas(Long turmaId, UUID alunoId, double percentual, boolean tresFaltasConsecutivas)`

### Critério de alerta por frequência baixa

| # | Cenário | `percentual` | Notificação Enviada? | Preferência Checada |
|---|---------|-------------|---------------------|---------------------|
| 1 | Frequência abaixo do mínimo | `74.9` | sim | `quedaFrequencia` |
| 2 | Frequência exatamente 75% | `75.0` | não | — |
| 3 | Frequência acima de 75% | `80.0` | não | — |
| 4 | Frequência 0% | `0.0` | sim | `quedaFrequencia` |

### Critério de alerta por faltas consecutivas

| # | Cenário | `tresFaltasConsecutivas` | Notificação Enviada? | Preferência Checada |
|---|---------|--------------------------|---------------------|---------------------|
| 5 | Três faltas consecutivas | `true` | sim | `faltaAluno` |
| 6 | Sem faltas consecutivas | `false` | não | — |

### Ambos os critérios ativos

| # | Cenário | Condições | Notificações |
|---|---------|-----------|-------------|
| 7 | Freq baixa + 3 consecutivas | `percentual=50, consecutivas=true` | 2 notificações enviadas (uma por critério) |

---

## Lógica de Preferências do Responsável

| # | Cenário | Preferência do Responsável | Notificação Enviada? |
|---|---------|---------------------------|---------------------|
| 1 | Sem preferências cadastradas (`null`) | nenhuma preferência | sim (padrão: notificar) |
| 2 | `faltaAluno = true` | preferência habilitada | sim |
| 3 | `faltaAluno = false` | preferência desabilitada | não |
| 4 | `quedaFrequencia = true` | preferência habilitada | sim |
| 5 | `quedaFrequencia = false` | preferência desabilitada | não |

---

## Lógica de Iteração por Responsável

| # | Cenário | Subscriptions do Aluno | Comportamento |
|---|---------|------------------------|---------------|
| 1 | Sem subscriptions | `findAllByUsuarioId` retorna `[]` | nenhuma notificação enviada |
| 2 | Um responsável | 1 subscription | `pushNotificationService.enviar` chamado 1 vez por critério ativo |
| 3 | Dois responsáveis | 2 subscriptions | `enviar` chamado 2 vezes por critério ativo |
| 4 | Responsável com preferência negada | 1 sub, `faltaAluno=false` | `enviar` não chamado para esse responsável |

---

## Conteúdo das Notificações

### Alerta de frequência baixa

| Campo | Valor Esperado |
|-------|----------------|
| Título | `"Frequência abaixo do mínimo"` |
| Corpo | contém o percentual formatado (ex: `"74,9%"`) e menciona os 75% |
| URL | `"/responsavel/acompanhamento?tab=frequencia"` |

### Alerta de faltas consecutivas

| Campo | Valor Esperado |
|-------|----------------|
| Título | `"Faltas consecutivas registradas"` |
| Corpo | menciona "3 faltas consecutivas" |
| URL | `"/responsavel/acompanhamento?tab=frequencia"` |

---

## Regras de Negócio Críticas

- O limiar de frequência é **estritamente menor que 75%** (`< 75.0`) — exatamente 75% não dispara alerta.
- Preferências `null` equivalem a **permitido** — responsável sem cadastro de preferências recebe tudo.
- Os dois tipos de alerta são **independentes** — podem ser disparados simultaneamente.
- O serviço é `@Async` — testes unitários devem mockar ou executar síncronamente.

---

## Exemplo de Setup

```java
@ExtendWith(MockitoExtension.class)
class AlertaFrequenciaServiceTest {

    @Mock PushNotificationService pushNotificationService;
    @Mock PreferenciasNotificacaoRepository preferenciasRepository;
    @Mock PushSubscriptionRepository pushSubscriptionRepository;

    @InjectMocks AlertaFrequenciaService alertaFrequenciaService;

    private PushSubscription subFake(UUID responsavelId) {
        var sub = new PushSubscription();
        var usuario = new Usuario(); usuario.setId(responsavelId);
        sub.setUsuario(usuario);
        sub.setId(1L);
        return sub;
    }
}
```
