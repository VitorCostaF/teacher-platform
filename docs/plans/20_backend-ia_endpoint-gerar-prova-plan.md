# Plano de Implementação — backend-ia_endpoint-gerar-prova

> **Task origem:** `docs/Tasks/backend-ia_endpoint-gerar-prova.md`
> **Escopo:** Backend — Integração com IA
> **Complexidade:** G
> **Sprint:** 3 — Criação com IA
> **Depende de:** `backend-model_avaliacoes-plan.md` (entidades Questao, Avaliacao existentes)

---

## Contexto do Codebase

Entidades `Avaliacao`, `Questao`, `TipoQuestaoEnum` já existem. Spring Security, JWT, Redis já estão configurados (plano do login). Redis já usado para rate limiting em `RateLimitService`. Este plano adiciona integração com Claude API (Anthropic) e endpoints de geração de conteúdo com IA.

---

## Dependências a Adicionar no pom.xml

```xml
<!-- HTTP client para chamar API externa (Claude) -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- Parsing de PDF -->
<dependency>
  <groupId>org.apache.pdfbox</groupId>
  <artifactId>pdfbox</artifactId>
  <version>3.0.3</version>
</dependency>

<!-- Parsing de DOCX -->
<dependency>
  <groupId>org.apache.poi</groupId>
  <artifactId>poi-ooxml</artifactId>
  <version>5.3.0</version>
</dependency>
```

### Configurações em application.properties
```properties
# Claude API
app.ia.api-key=${ANTHROPIC_API_KEY}
app.ia.api-url=https://api.anthropic.com/v1/messages
app.ia.model=claude-sonnet-4-6
app.ia.max-tokens=4096
app.ia.rate-limit-por-hora=20

# Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

---

## Arquivos a Criar

### DTOs

`src/main/java/br/com/inovadados/teacherplatform/dto/request/GerarProvaRequest.java`
```java
public record GerarProvaRequest(
  @NotBlank String disciplina,
  @NotBlank String serie,
  @NotNull NivelDificuldadeEnum dificuldade,
  Map<TipoQuestaoEnum, Integer> quantidadesPorTipo,
  String conteudoTexto,   // fonte: texto colado
  List<String> topicos    // fonte: tópicos livres
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/request/RegerarQuestaoRequest.java`
```java
public record RegerarQuestaoRequest(
  @NotBlank String contextoProva,
  @NotNull QuestaoGeradaDto questaoAtual
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/QuestaoGeradaDto.java`
```java
public record QuestaoGeradaDto(
  String id,  // temp-uuid
  TipoQuestaoEnum tipo,
  String enunciado,
  List<String> alternativas,
  Integer gabarito,
  String dificuldade,
  String topico,
  String criteriosCorrecao  // para dissertativas
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/GeracaoResponse.java`
```java
public record GeracaoResponse(List<QuestaoGeradaDto> questoes, int tokensUsados) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/GradeAulaDto.java`
- campos: semana, aula, conteudo, objetivos, recursosSugeridos

### Enum

`src/main/java/br/com/inovadados/teacherplatform/domain/enums/NivelDificuldadeEnum.java`
```java
public enum NivelDificuldadeEnum { FACIL, MEDIO, DIFICIL, MISTO }
```

### Serviços

`src/main/java/br/com/inovadados/teacherplatform/service/ClaudeApiService.java`
- Usa `WebClient` (WebFlux)
- `gerarConteudo(String prompt)` → `String` (response JSON da IA)
- Monta header `x-api-key`, `anthropic-version: 2023-06-01`, body `{ model, max_tokens, messages }`
- Trata erros HTTP da API: 429 → lança `IaRateLimitException`; 5xx → lança `IaIndisponibilException`
- Parse parcial: se JSON malformado, tentar extrair array `questoes` com regex antes de lançar erro

`src/main/java/br/com/inovadados/teacherplatform/service/PromptBuilderService.java`
- `buildGerarProvaPrompt(GerarProvaRequest req)` → monta prompt estruturado em português brasileiro
  - Inclui: disciplina, série, tipos e quantidades de questões, dificuldade, conteúdo sanitizado
  - Instrução de retorno: JSON com campo `questoes: []`
- `buildRegerarQuestaoPrompt(RegerarQuestaoRequest req)` → mantém contexto da prova
- `buildGerarGradePrompt(...)` → prompt para grade de aulas
- `buildGerarFlashcardsPrompt(...)` → prompt para flashcards
- `sanitizarConteudo(String texto)` → remove scripts, SQL injection, PII óbvio antes de enviar para IA

`src/main/java/br/com/inovadados/teacherplatform/service/DocumentoParserService.java`
- `extrairTextoPDF(InputStream)` → usa PDFBox; retorna texto limpo
- `extrairTextoDocx(InputStream)` → usa POI; retorna texto limpo
- `contarPalavrasUteis(String texto)` → remove stopwords e retorna contagem

`src/main/java/br/com/inovadados/teacherplatform/service/IaRateLimitService.java`
- Usa `RedisTemplate` (já configurado)
- `verificarLimite(UUID professorId)` → KEY `ia:rate:{professorId}:hora`; se ≥ 20 → lança `IaRateLimitException`
- `incrementar(UUID professorId)` → increment com TTL = segundos restantes da hora atual
- `registrarUso(UUID professorId, String endpoint, int tokens)` → salva em tabela `logs_uso_ia`

`src/main/java/br/com/inovadados/teacherplatform/service/IaService.java`
- `gerarProva(GerarProvaRequest req, UUID professorId)` → verifica rate limit → sanitiza → chama ClaudeApiService → parseia JSON → salva log → retorna `GeracaoResponse`
- `regenerarQuestao(RegerarQuestaoRequest req, UUID professorId)` → similar
- `gerarGrade(GerarGradeRequest req, UUID professorId)` → idem para grade
- `gerarFlashcards(GerarFlashcardsRequest req, UUID professorId)` → idem para flashcards

### Migration

`src/main/resources/db/migration/V8__create_logs_uso_ia.sql`
```sql
CREATE TABLE logs_uso_ia (
  id BIGSERIAL PRIMARY KEY,
  professor_id UUID NOT NULL REFERENCES usuarios(id),
  escola_id BIGINT NOT NULL REFERENCES escolas(id),
  endpoint VARCHAR(100) NOT NULL,
  tokens_usados INTEGER NOT NULL,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_logs_ia_professor_hora ON logs_uso_ia(professor_id, criado_em);
```

### Controller

`src/main/java/br/com/inovadados/teacherplatform/controller/IaController.java`
- `@RestController @RequestMapping("/ia")`
- `POST /gerar-prova`
- `POST /regenerar-questao`
- `POST /gerar-grade`
- `POST /gerar-flashcards`

`src/main/java/br/com/inovadados/teacherplatform/controller/UploadController.java`
- `@RestController @RequestMapping("/upload")`
- `POST /conteudo` → recebe `MultipartFile`, detecta tipo (PDF/DOCX), extrai texto via `DocumentoParserService`, verifica `< 100 palavras úteis` e adiciona campo `avisoConteudoInsuficiente` na response

### Exceptions

`src/main/java/br/com/inovadados/teacherplatform/exception/IaRateLimitException.java`
`src/main/java/br/com/inovadados/teacherplatform/exception/IaIndisponibilException.java`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `pom.xml` | Adicionar webflux, pdfbox, poi-ooxml |
| `application.properties` | Adicionar configurações de IA e multipart |
| `GlobalExceptionHandler` | Handlers: IaRateLimitException → 429 com retryAfter; IaIndisponibilException → 502 |
| `SecurityConfig` | Liberar `/upload/**` e `/ia/**` para PROFESSOR e ADMIN |

---

## Ordem de Implementação

```
1. pom.xml — dependências
2. application.properties — configurações IA e upload
3. Enums: NivelDificuldadeEnum
4. DTOs: request e response
5. Migration V8 — logs_uso_ia
6. DocumentoParserService (independente, testar com arquivos reais)
7. ClaudeApiService (WebClient, testar com mock HTTP server)
8. PromptBuilderService (puro, sem dependências externas)
9. IaRateLimitService (RedisTemplate)
10. IaService (orquestra os anteriores)
11. Exceptions customizadas
12. GlobalExceptionHandler — novos handlers
13. IaController + UploadController
14. SecurityConfig — novas rotas
15. Testes unitários: PromptBuilderService, DocumentoParserService, IaRateLimitService
16. Testes de integração: POST /ia/gerar-prova com mock da Claude API
```

---

## Resumo

- **16 arquivos** a criar (DTOs, serviços, controller, migration, exceptions)
- **4 arquivos** a modificar (pom.xml, application.properties, GlobalExceptionHandler, SecurityConfig)
- **Bibliotecas a adicionar:** spring-boot-starter-webflux, pdfbox, poi-ooxml
- **Complexidade mantida:** G
