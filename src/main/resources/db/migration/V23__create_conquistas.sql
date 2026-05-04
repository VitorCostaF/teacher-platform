CREATE TABLE conquistas (
  id BIGSERIAL PRIMARY KEY,
  aluno_id UUID NOT NULL REFERENCES usuarios(id),
  tipo VARCHAR(60) NOT NULL,
  descricao VARCHAR(255) NOT NULL,
  obtida_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_conquistas_aluno ON conquistas(aluno_id);
