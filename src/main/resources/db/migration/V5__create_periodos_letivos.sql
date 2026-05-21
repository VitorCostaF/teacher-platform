CREATE TABLE periodos_letivos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  escola_id BIGINT NOT NULL REFERENCES escolas(id),
  nome VARCHAR(100) NOT NULL,
  inicio DATE NOT NULL,
  fim DATE NOT NULL,
  ativo BOOLEAN NOT NULL DEFAULT FALSE,
  criado_em DATETIME NOT NULL DEFAULT NOW()
);

-- MySQL não suporta partial indexes; a unicidade de período ativo por escola
-- deve ser garantida pela camada de aplicação
CREATE INDEX idx_periodos_letivos_escola_ativo ON periodos_letivos(escola_id, ativo);
