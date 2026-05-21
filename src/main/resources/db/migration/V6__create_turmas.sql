CREATE TABLE turmas (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  escola_id BIGINT NOT NULL REFERENCES escolas(id),
  periodo_letivo_id BIGINT NOT NULL REFERENCES periodos_letivos(id),
  professor_id VARCHAR(36) NOT NULL REFERENCES usuarios(id),
  nome VARCHAR(150) NOT NULL,
  serie VARCHAR(50) NOT NULL,
  disciplina VARCHAR(100) NOT NULL,
  deletado_em DATETIME
);

CREATE INDEX idx_turmas_professor_id ON turmas(professor_id);
CREATE INDEX idx_turmas_escola_id ON turmas(escola_id);
