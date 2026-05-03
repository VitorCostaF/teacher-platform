CREATE TABLE vinculos_responsavel (
  id BIGSERIAL PRIMARY KEY,
  responsavel_id UUID NOT NULL REFERENCES usuarios(id),
  aluno_id UUID NOT NULL REFERENCES usuarios(id),
  parentesco VARCHAR(50),
  CONSTRAINT uq_vinculo_responsavel_aluno UNIQUE (responsavel_id, aluno_id)
);
