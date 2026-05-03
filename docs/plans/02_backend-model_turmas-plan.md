# Plano de Implementação — backend-model_turmas

> **Task origem:** `docs/Tasks/backend-model_turmas.md`
> **Escopo:** Backend — Modelos de Dados
> **Complexidade:** M
> **Sprint:** 0 — Fundação
> **Depende de:** `backend-model_usuario-plan.md` (V1–V4 já executadas)

---

## Contexto do Codebase

Continuação direta da task `backend-model_usuario`. As migrations V1–V4 e as entidades de Escola, Usuario, Sessao e TokenTemporario já existem. Este plano adiciona a estrutura pedagógica: turmas, matrículas, frequência e auditoria. PostgreSQL e Flyway já estão configurados.

---

## Dependências

Nenhuma nova dependência no pom.xml. Tudo já disponível via JPA + PostgreSQL.

---

## Arquivos a Criar

### Migrations Flyway

`src/main/resources/db/migration/V5__create_periodos_letivos.sql`
```sql
CREATE TABLE periodos_letivos (
  id BIGSERIAL PRIMARY KEY,
  escola_id BIGINT NOT NULL REFERENCES escolas(id),
  nome VARCHAR(100) NOT NULL,
  inicio DATE NOT NULL,
  fim DATE NOT NULL,
  ativo BOOLEAN NOT NULL DEFAULT FALSE,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Apenas 1 período ativo por escola
CREATE UNIQUE INDEX idx_periodos_letivos_escola_ativo
  ON periodos_letivos(escola_id)
  WHERE ativo = TRUE;
```

`src/main/resources/db/migration/V6__create_turmas.sql`
```sql
CREATE TABLE turmas (
  id BIGSERIAL PRIMARY KEY,
  escola_id BIGINT NOT NULL REFERENCES escolas(id),
  periodo_letivo_id BIGINT NOT NULL REFERENCES periodos_letivos(id),
  professor_id UUID NOT NULL REFERENCES usuarios(id),
  nome VARCHAR(150) NOT NULL,
  serie VARCHAR(50) NOT NULL,
  disciplina VARCHAR(100) NOT NULL,
  deletado_em TIMESTAMPTZ
);

CREATE INDEX idx_turmas_professor_id ON turmas(professor_id);
CREATE INDEX idx_turmas_escola_id ON turmas(escola_id);
```

`src/main/resources/db/migration/V7__create_matriculas.sql`
```sql
CREATE TABLE matriculas (
  id BIGSERIAL PRIMARY KEY,
  turma_id BIGINT NOT NULL REFERENCES turmas(id),
  aluno_id UUID NOT NULL REFERENCES usuarios(id),
  matriculado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  removido_em TIMESTAMPTZ,
  CONSTRAINT uq_matriculas_turma_aluno UNIQUE (turma_id, aluno_id)
);
```

`src/main/resources/db/migration/V8__create_vinculos_responsavel.sql`
```sql
CREATE TABLE vinculos_responsavel (
  id BIGSERIAL PRIMARY KEY,
  responsavel_id UUID NOT NULL REFERENCES usuarios(id),
  aluno_id UUID NOT NULL REFERENCES usuarios(id),
  parentesco VARCHAR(50),
  CONSTRAINT uq_vinculo_responsavel_aluno UNIQUE (responsavel_id, aluno_id)
);
```

`src/main/resources/db/migration/V9__create_registros_frequencia.sql`
```sql
CREATE TYPE status_frequencia_enum AS ENUM ('presente','ausente','justificado','meio_periodo');

CREATE TABLE registros_frequencia (
  id BIGSERIAL PRIMARY KEY,
  turma_id BIGINT NOT NULL REFERENCES turmas(id),
  aluno_id UUID NOT NULL REFERENCES usuarios(id),
  data_aula DATE NOT NULL,
  status status_frequencia_enum NOT NULL,
  observacao TEXT,
  lancado_por UUID NOT NULL REFERENCES usuarios(id),
  lancado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  editado_em TIMESTAMPTZ,
  CONSTRAINT uq_frequencia_turma_aluno_data UNIQUE (turma_id, aluno_id, data_aula)
);

CREATE INDEX idx_frequencia_turma_data ON registros_frequencia(turma_id, data_aula);
CREATE INDEX idx_frequencia_aluno ON registros_frequencia(aluno_id);
```

`src/main/resources/db/migration/V10__create_logs_auditoria.sql`
```sql
CREATE TABLE logs_auditoria (
  id BIGSERIAL PRIMARY KEY,
  escola_id BIGINT NOT NULL REFERENCES escolas(id),
  usuario_id UUID REFERENCES usuarios(id),
  acao VARCHAR(100) NOT NULL,
  entidade VARCHAR(100) NOT NULL,
  entidade_id VARCHAR(255),
  dados_anteriores JSONB,
  motivo TEXT,
  ip VARCHAR(45),
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_logs_auditoria_escola ON logs_auditoria(escola_id);
CREATE INDEX idx_logs_auditoria_usuario ON logs_auditoria(usuario_id);

-- Nenhuma permissão de UPDATE/DELETE — append-only garantido por policy no PostgreSQL (opcional)
-- ALTER TABLE logs_auditoria ENABLE ROW LEVEL SECURITY;
```

### Enums Java

`src/main/java/br/com/inovadados/teacherplatform/domain/enums/StatusFrequenciaEnum.java`
```java
public enum StatusFrequenciaEnum { PRESENTE, AUSENTE, JUSTIFICADO, MEIO_PERIODO }
```

### Entities JPA

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/PeriodoLetivo.java`
- `@Entity @Table(name="periodos_letivos")`
- Campos: id (Long), escola (ManyToOne), nome, inicio (LocalDate), fim (LocalDate), ativo (boolean), criadoEm
- `@OneToMany(mappedBy="periodoLetivo") List<Turma> turmas`

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/Turma.java`
- `@Entity @Table(name="turmas")`
- Campos: id (Long), escola (ManyToOne), periodoLetivo (ManyToOne), professor (ManyToOne UUID), nome, serie, disciplina, deletadoEm (nullable)
- `@OneToMany(mappedBy="turma") List<Matricula> matriculas`

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/Matricula.java`
- `@Entity @Table(name="matriculas")`
- Campos: id (Long), turma (ManyToOne), aluno (ManyToOne UUID), matriculadoEm, removidoEm (nullable)

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/VinculoResponsavel.java`
- `@Entity @Table(name="vinculos_responsavel")`
- Campos: id (Long), responsavel (ManyToOne UUID), aluno (ManyToOne UUID), parentesco

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/RegistroFrequencia.java`
- `@Entity @Table(name="registros_frequencia")`
- Campos: id (Long), turma (ManyToOne), aluno (ManyToOne UUID), dataAula (LocalDate), status (StatusFrequenciaEnum), observacao, lancadoPor (ManyToOne UUID), lancadoEm, editadoEm (nullable)

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/LogAuditoria.java`
- `@Entity @Table(name="logs_auditoria")`
- Campos: id (Long), escola (ManyToOne), usuario (ManyToOne UUID nullable), acao, entidade, entidadeId, dadosAnteriores (String/JSONB), motivo, ip, criadoEm
- `@Immutable` — garantir que nenhuma operação de update seja disparada via JPA

### Repositories

`src/main/java/br/com/inovadados/teacherplatform/repository/PeriodoLetivoRepository.java`
- `Optional<PeriodoLetivo> findByEscolaIdAndAtivoTrue(Long escolaId)`

`src/main/java/br/com/inovadados/teacherplatform/repository/TurmaRepository.java`
- `List<Turma> findByProfessorIdAndDeletadoEmIsNull(UUID professorId)`
- `List<Turma> findByEscolaIdAndDeletadoEmIsNull(Long escolaId)`

`src/main/java/br/com/inovadados/teacherplatform/repository/MatriculaRepository.java`
- `List<Matricula> findByTurmaIdAndRemovidoEmIsNull(Long turmaId)`
- `Optional<Matricula> findByTurmaIdAndAlunoId(Long turmaId, UUID alunoId)`

`src/main/java/br/com/inovadados/teacherplatform/repository/VinculoResponsavelRepository.java`
- `List<VinculoResponsavel> findByResponsavelId(UUID responsavelId)`

`src/main/java/br/com/inovadados/teacherplatform/repository/RegistroFrequenciaRepository.java`
- `Optional<RegistroFrequencia> findByTurmaIdAndDataAula(Long turmaId, LocalDate data)`
- `List<RegistroFrequencia> findByTurmaIdAndAlunoId(Long turmaId, UUID alunoId)`

`src/main/java/br/com/inovadados/teacherplatform/repository/LogAuditoriaRepository.java`
- Apenas `save()` — sem métodos de update/delete

---

## Arquivos a Modificar

Nenhum arquivo existente precisa ser modificado nesta task. As migrations são adicionadas, não alteradas.

---

## Ordem de Implementação

1. Criar migrations V5 → V10 (ordem estrita: periodos_letivos antes de turmas, turmas antes de matriculas)
2. Criar enum `StatusFrequenciaEnum`
3. Criar entities (PeriodoLetivo → Turma → Matricula → VinculoResponsavel → RegistroFrequencia → LogAuditoria)
4. Criar repositories
5. Validar com `mvn flyway:migrate` e `mvn test`

---

## Resumo

- **6 arquivos SQL** a criar (migrations V5–V10)
- **1 enum** a criar
- **6 entities** a criar
- **6 repositories** a criar
- **0 arquivos** a modificar
- **Complexidade mantida:** M
