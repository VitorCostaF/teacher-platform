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
