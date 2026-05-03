CREATE TABLE matriculas (
  id BIGSERIAL PRIMARY KEY,
  turma_id BIGINT NOT NULL REFERENCES turmas(id),
  aluno_id UUID NOT NULL REFERENCES usuarios(id),
  matriculado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  removido_em TIMESTAMPTZ,
  CONSTRAINT uq_matriculas_turma_aluno UNIQUE (turma_id, aluno_id)
);
