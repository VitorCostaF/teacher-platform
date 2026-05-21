CREATE TABLE conquistas (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  aluno_id VARCHAR(36) NOT NULL REFERENCES usuarios(id),
  tipo VARCHAR(60) NOT NULL,
  descricao VARCHAR(255) NOT NULL,
  obtida_em DATETIME NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_conquistas_aluno ON conquistas(aluno_id);
