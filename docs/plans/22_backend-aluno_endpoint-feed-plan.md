# Plano de Implementação — backend-aluno_endpoint-feed

> **Task origem:** `docs/Tasks/backend-aluno_endpoint-feed.md`
> **Escopo:** Backend — Área do Aluno (Feed e Desempenho)
> **Complexidade:** M
> **Sprint:** 4 — Área do Aluno
> **Depende de:** `backend-model_avaliacoes-plan.md`

---

## Contexto do Codebase

Entidades `Avaliacao`, `Entrega`, `Questao`, `Flashcard`, `Usuario` já existem. DashboardService já implementado e pode ser reutilizado para cálculo de desempenho. Algoritmo de repetição espaçada SM-2 precisa ser implementado do zero.

---

## Arquivos a Criar

### DTOs

`src/main/java/br/com/inovadados/teacherplatform/dto/response/FeedAlunoResponse.java`
```java
public record FeedAlunoResponse(
  List<ItemFeedDto> urgentes,
  List<ItemFeedDto> paraFazer,
  List<ConteudoFeedDto> novosConteudos,
  List<RecomendacaoDto> recomendados
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/ItemFeedDto.java`
```java
public record ItemFeedDto(
  Long id,
  String tipo,   // "PROVA" | "ATIVIDADE"
  String titulo,
  String disciplina,
  LocalDateTime prazo,
  String status, // "NAO_INICIADO" | "EM_ANDAMENTO" | "ENTREGUE"
  boolean atrasado
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/DesempenhoAlunoSimplesResponse.java`
- Resumo, por disciplina com tendência, evolução de notas, conquistas (lista de badges)

`src/main/java/br/com/inovadados/teacherplatform/dto/response/FlashcardResponse.java`
```java
public record FlashcardResponse(Long id, String pergunta, String resposta, String topico) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/request/AvaliacaoFlashcardRequest.java`
```java
public record AvaliacaoFlashcardRequest(boolean sabia) {}
```

### Migration

`src/main/resources/db/migration/V10__create_flashcards_and_progresso.sql`
```sql
CREATE TABLE flashcards (
  id BIGSERIAL PRIMARY KEY,
  escola_id BIGINT NOT NULL REFERENCES escolas(id),
  disciplina VARCHAR(100) NOT NULL,
  topico VARCHAR(150) NOT NULL,
  pergunta TEXT NOT NULL,
  resposta TEXT NOT NULL,
  gerado_por_ia BOOLEAN NOT NULL DEFAULT TRUE,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE progresso_flashcard (
  id BIGSERIAL PRIMARY KEY,
  aluno_id UUID NOT NULL REFERENCES usuarios(id),
  flashcard_id BIGINT NOT NULL REFERENCES flashcards(id),
  intervalo_dias INTEGER NOT NULL DEFAULT 1,
  fator_facilidade NUMERIC(4,2) NOT NULL DEFAULT 2.5,
  proxima_revisao DATE NOT NULL DEFAULT CURRENT_DATE,
  total_revisoes INTEGER NOT NULL DEFAULT 0,
  UNIQUE(aluno_id, flashcard_id)
);
CREATE INDEX idx_progresso_aluno_revisao ON progresso_flashcard(aluno_id, proxima_revisao);
```

`src/main/resources/db/migration/V11__create_conquistas.sql`
```sql
CREATE TABLE conquistas (
  id BIGSERIAL PRIMARY KEY,
  aluno_id UUID NOT NULL REFERENCES usuarios(id),
  tipo VARCHAR(60) NOT NULL,
  descricao VARCHAR(255) NOT NULL,
  obtida_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

### Serviços

`src/main/java/br/com/inovadados/teacherplatform/service/FeedAlunoService.java`
- `getFeed(UUID alunoId)` →
  - Urgentes: avaliacoes disponíveis com `prazo < NOW() + 24h` e sem entrega
  - Para fazer: todas disponíveis sem entrega, ordenadas por prazo
  - Novos conteúdos: materiais publicados nas últimas 48h nas turmas do aluno
  - Recomendados: identifica tópicos com < 60% de acerto via `DashboardService.getDesempenhoAluno()` e busca flashcards/materiais relacionados

`src/main/java/br/com/inovadados/teacherplatform/service/GamificacaoService.java`
- `calcularPontos(UUID alunoId)` → entrega no prazo: +10pts, entrega atrasada: +3pts, flashcard revisado: +1pt, nota ≥ 7: +20pts
- `verificarConquistas(UUID alunoId)` → verifica critérios: streak de 7 dias, top 3 na turma, mês sem faltas, etc. Persiste novas conquistas.
- `getConquistas(UUID alunoId)` → lista badges

`src/main/java/br/com/inovadados/teacherplatform/service/FlashcardService.java`
- `getFlashcardsPriorizados(UUID alunoId, Long disciplinaId)` → busca `progresso_flashcard` onde `proxima_revisao <= hoje`, ordena por data
- `registrarAvaliacao(UUID alunoId, Long flashcardId, boolean sabia)` → implementa SM-2:
  - Se sabia: `fator_facilidade += 0.1`, `intervalo_dias = max(1, intervalo * fator)`
  - Se não sabia: `fator_facilidade = max(1.3, fator - 0.2)`, `intervalo_dias = 1`
  - `proxima_revisao = hoje + intervalo_dias`

### Controller

`src/main/java/br/com/inovadados/teacherplatform/controller/AlunoController.java`
- `GET /aluno/feed`
- `GET /aluno/desempenho`
- `GET /aluno/flashcards?disciplina=:id`
- `POST /aluno/flashcards/:cardId/avaliacao`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `SecurityConfig` | Rotas `/aluno/**` somente perfil ALUNO |

---

## Ordem de Implementação

```
1. Migrations V10, V11
2. DTOs
3. FlashcardService — algoritmo SM-2 (testar com unitários)
4. GamificacaoService — calcularPontos e verificarConquistas
5. FeedAlunoService — getFeed (depende de DashboardService)
6. AlunoController
7. SecurityConfig
8. Testes unitários: SM-2 (vários cenários de acerto/erro), cálculo de pontos
9. Testes de integração: GET /aluno/feed
```

---

## Resumo

- **10 arquivos** a criar (DTOs, 3 serviços, 1 controller, 2 migrations)
- **1 arquivo** a modificar (SecurityConfig)
- **Nenhuma dependência nova** no pom.xml
- **Complexidade mantida:** M
