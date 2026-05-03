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
