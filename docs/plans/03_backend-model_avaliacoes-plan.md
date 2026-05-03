# Plano de Implementação — backend-model_avaliacoes

> **Task origem:** `docs/Tasks/backend-model_avaliacoes.md`
> **Escopo:** Backend — Modelos de Dados
> **Complexidade:** M
> **Sprint:** 0 — Fundação
> **Depende de:** `backend-model_turmas-plan.md` (V1–V10 já executadas)

---

## Contexto do Codebase

Terceira e última task de modelos de dados. As migrations V1–V10 e todas as entities de usuário, sessão, turma, matrícula e frequência já existem. Este plano adiciona o núcleo pedagógico: avaliações, questões, entregas, conteúdos e flashcards.

---

## Dependências

Nenhuma nova dependência no pom.xml.

> **Nota sobre JSONB:** O campo `alternativas` em `questoes` usa JSONB no PostgreSQL. No JPA, mapear como `String` com `@Column(columnDefinition = "jsonb")` + `@Convert` customizado, ou como `@JdbcTypeCode(SqlTypes.JSON)` com Hibernate 6+ (já disponível no Spring Boot 4.x).

---

## Arquivos a Criar

### Migrations Flyway

`src/main/resources/db/migration/V11__create_avaliacoes.sql`
```sql
CREATE TYPE tipo_avaliacao_enum AS ENUM ('prova','atividade','trabalho');
CREATE TYPE status_avaliacao_enum AS ENUM ('rascunho','agendada','publicada','encerrada');
CREATE TYPE gabarito_liberacao_enum AS ENUM ('imediata','apos_encerramento','manual');

CREATE TABLE avaliacoes (
  id BIGSERIAL PRIMARY KEY,
  turma_id BIGINT NOT NULL REFERENCES turmas(id),
  professor_id UUID NOT NULL REFERENCES usuarios(id),
  titulo VARCHAR(255) NOT NULL,
  tipo tipo_avaliacao_enum NOT NULL,
  status status_avaliacao_enum NOT NULL DEFAULT 'rascunho',
  peso_nota NUMERIC(4,2),
  duracao_minutos INTEGER,
  disponivel_em TIMESTAMPTZ,
  encerra_em TIMESTAMPTZ,
  embaralhar_questoes BOOLEAN NOT NULL DEFAULT FALSE,
  embaralhar_alternativas BOOLEAN NOT NULL DEFAULT FALSE,
  gabarito_liberacao gabarito_liberacao_enum NOT NULL DEFAULT 'apos_encerramento',
  permite_entrega_atrasada BOOLEAN NOT NULL DEFAULT FALSE,
  gerado_por_ia BOOLEAN NOT NULL DEFAULT FALSE,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_avaliacoes_turma_id ON avaliacoes(turma_id);
CREATE INDEX idx_avaliacoes_professor_id ON avaliacoes(professor_id);
```

`src/main/resources/db/migration/V12__create_questoes.sql`
```sql
CREATE TYPE tipo_questao_enum AS ENUM ('multipla_escolha','verdadeiro_falso','dissertativa','upload_arquivo');

CREATE TABLE questoes (
  id BIGSERIAL PRIMARY KEY,
  avaliacao_id BIGINT NOT NULL REFERENCES avaliacoes(id),
  ordem INTEGER NOT NULL,
  tipo tipo_questao_enum NOT NULL,
  enunciado TEXT NOT NULL,
  alternativas JSONB,
  gabarito_dissertativo TEXT,
  dificuldade VARCHAR(20),
  topico VARCHAR(150),
  pontos NUMERIC(5,2) NOT NULL DEFAULT 1.0
);
```

`src/main/resources/db/migration/V13__create_sessoes_prova.sql`
```sql
CREATE TABLE sessoes_prova (
  id BIGSERIAL PRIMARY KEY,
  avaliacao_id BIGINT NOT NULL REFERENCES avaliacoes(id),
  aluno_id UUID NOT NULL REFERENCES usuarios(id),
  iniciada_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  encerrada_em TIMESTAMPTZ,
  entregue_manualmente BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT uq_sessao_prova_avaliacao_aluno UNIQUE (avaliacao_id, aluno_id)
);
```

`src/main/resources/db/migration/V14__create_entregas.sql`
```sql
CREATE TYPE status_entrega_enum AS ENUM ('nao_iniciada','rascunho','entregue','corrigida');

CREATE TABLE entregas (
  id BIGSERIAL PRIMARY KEY,
  avaliacao_id BIGINT NOT NULL REFERENCES avaliacoes(id),
  aluno_id UUID NOT NULL REFERENCES usuarios(id),
  status status_entrega_enum NOT NULL DEFAULT 'nao_iniciada',
  iniciado_em TIMESTAMPTZ,
  entregue_em TIMESTAMPTZ,
  nota_automatica NUMERIC(5,2),
  nota_final NUMERIC(5,2),
  entrega_atrasada BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT uq_entrega_avaliacao_aluno UNIQUE (avaliacao_id, aluno_id)
);
```

`src/main/resources/db/migration/V15__create_respostas.sql`
```sql
CREATE TABLE respostas (
  id BIGSERIAL PRIMARY KEY,
  entrega_id BIGINT NOT NULL REFERENCES entregas(id),
  questao_id BIGINT NOT NULL REFERENCES questoes(id),
  resposta_indice INTEGER,
  resposta_texto TEXT,
  arquivo_url VARCHAR(500),
  correta BOOLEAN,
  nota_manual NUMERIC(5,2)
);
```

`src/main/resources/db/migration/V16__create_conteudos.sql`
```sql
CREATE TYPE tipo_conteudo_enum AS ENUM ('texto','video','arquivo','link');

CREATE TABLE conteudos (
  id BIGSERIAL PRIMARY KEY,
  turma_id BIGINT NOT NULL REFERENCES turmas(id),
  professor_id UUID NOT NULL REFERENCES usuarios(id),
  titulo VARCHAR(255) NOT NULL,
  tipo tipo_conteudo_enum NOT NULL,
  corpo TEXT,
  arquivo_url VARCHAR(500),
  video_url VARCHAR(500),
  link_externo VARCHAR(500),
  publicado_em TIMESTAMPTZ,
  serie_sugerida VARCHAR(50),
  topicos TEXT[],
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

`src/main/resources/db/migration/V17__create_flashcards.sql`
```sql
CREATE TABLE flashcards (
  id BIGSERIAL PRIMARY KEY,
  conteudo_id BIGINT REFERENCES conteudos(id),
  turma_id BIGINT NOT NULL REFERENCES turmas(id),
  pergunta TEXT NOT NULL,
  resposta TEXT NOT NULL,
  gerado_por_ia BOOLEAN NOT NULL DEFAULT FALSE,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

`src/main/resources/db/migration/V18__create_progresso_flashcards.sql`
```sql
CREATE TYPE resultado_flashcard_enum AS ENUM ('facil','medio','dificil');

CREATE TABLE progresso_flashcards (
  id BIGSERIAL PRIMARY KEY,
  aluno_id UUID NOT NULL REFERENCES usuarios(id),
  flashcard_id BIGINT NOT NULL REFERENCES flashcards(id),
  resultado resultado_flashcard_enum NOT NULL,
  revisado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  proxima_revisao TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_progresso_flashcards_aluno ON progresso_flashcards(aluno_id, proxima_revisao);
```

### Enums Java

`src/main/java/br/com/inovadados/teacherplatform/domain/enums/TipoAvaliacaoEnum.java`
```java
public enum TipoAvaliacaoEnum { PROVA, ATIVIDADE, TRABALHO }
```

`src/main/java/br/com/inovadados/teacherplatform/domain/enums/StatusAvaliacaoEnum.java`
```java
public enum StatusAvaliacaoEnum { RASCUNHO, AGENDADA, PUBLICADA, ENCERRADA }
```

`src/main/java/br/com/inovadados/teacherplatform/domain/enums/GabaritoLiberacaoEnum.java`
```java
public enum GabaritoLiberacaoEnum { IMEDIATA, APOS_ENCERRAMENTO, MANUAL }
```

`src/main/java/br/com/inovadados/teacherplatform/domain/enums/TipoQuestaoEnum.java`
```java
public enum TipoQuestaoEnum { MULTIPLA_ESCOLHA, VERDADEIRO_FALSO, DISSERTATIVA, UPLOAD_ARQUIVO }
```

`src/main/java/br/com/inovadados/teacherplatform/domain/enums/StatusEntregaEnum.java`
```java
public enum StatusEntregaEnum { NAO_INICIADA, RASCUNHO, ENTREGUE, CORRIGIDA }
```

`src/main/java/br/com/inovadados/teacherplatform/domain/enums/TipoConteudoEnum.java`
```java
public enum TipoConteudoEnum { TEXTO, VIDEO, ARQUIVO, LINK }
```

`src/main/java/br/com/inovadados/teacherplatform/domain/enums/ResultadoFlashcardEnum.java`
```java
public enum ResultadoFlashcardEnum { FACIL, MEDIO, DIFICIL }
```

### Entities JPA

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/Avaliacao.java`
- `@Entity @Table(name="avaliacoes")`
- Campos conforme migration V11
- `@OneToMany(mappedBy="avaliacao") List<Questao> questoes`

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/Questao.java`
- `@Entity @Table(name="questoes")`
- `alternativas` mapeado como `@Column(columnDefinition="jsonb") @JdbcTypeCode(SqlTypes.JSON) String alternativas`

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/SessaoProva.java`
- `@Entity @Table(name="sessoes_prova")`

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/Entrega.java`
- `@Entity @Table(name="entregas")`
- `@OneToMany(mappedBy="entrega") List<Resposta> respostas`

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/Resposta.java`
- `@Entity @Table(name="respostas")`

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/Conteudo.java`
- `@Entity @Table(name="conteudos")`
- `topicos` mapeado como `@Column(columnDefinition="text[]") String[] topicos`

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/Flashcard.java`
- `@Entity @Table(name="flashcards")`

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/ProgressoFlashcard.java`
- `@Entity @Table(name="progresso_flashcards")`
- `proximaRevisao (LocalDateTime)` — chave para o algoritmo SM-2

### Repositories

`src/main/java/br/com/inovadados/teacherplatform/repository/AvaliacaoRepository.java`
- `List<Avaliacao> findByTurmaIdAndStatus(Long turmaId, StatusAvaliacaoEnum status)`

`src/main/java/br/com/inovadados/teacherplatform/repository/QuestaoRepository.java`
- `List<Questao> findByAvaliacaoIdOrderByOrdem(Long avaliacaoId)`

`src/main/java/br/com/inovadados/teacherplatform/repository/EntregaRepository.java`
- `Optional<Entrega> findByAvaliacaoIdAndAlunoId(Long avaliacaoId, UUID alunoId)`

`src/main/java/br/com/inovadados/teacherplatform/repository/SessaoProvaRepository.java`
- `Optional<SessaoProva> findByAvaliacaoIdAndAlunoId(Long avaliacaoId, UUID alunoId)`
- `List<SessaoProva> findByEncerradaEmIsNullAndAvaliacaoEncerraEmBefore(LocalDateTime now)` — para o job de expiração

`src/main/java/br/com/inovadados/teacherplatform/repository/ConteudoRepository.java`
- `List<Conteudo> findByTurmaIdAndPublicadoEmIsNotNull(Long turmaId)`

`src/main/java/br/com/inovadados/teacherplatform/repository/FlashcardRepository.java`
- `List<Flashcard> findByTurmaId(Long turmaId)`

`src/main/java/br/com/inovadados/teacherplatform/repository/ProgressoFlashcardRepository.java`
- `List<ProgressoFlashcard> findByAlunoIdAndProximaRevisaoBefore(UUID alunoId, LocalDateTime now)`

---

## Arquivos a Modificar

Nenhum arquivo existente precisa ser modificado.

---

## Ordem de Implementação

1. Migrations V11 → V18 (ordem estrita por FK: avaliacoes → questoes → sessoes_prova → entregas → respostas → conteudos → flashcards → progresso)
2. Enums Java (7 enums)
3. Entities (8 entities)
4. Repositories (7 repositories)
5. Validar com `mvn flyway:migrate` + `mvn test`

---

## Resumo

- **8 arquivos SQL** a criar (migrations V11–V18)
- **7 enums** a criar
- **8 entities** a criar
- **7 repositories** a criar
- **0 arquivos** a modificar
- **Complexidade mantida:** M
