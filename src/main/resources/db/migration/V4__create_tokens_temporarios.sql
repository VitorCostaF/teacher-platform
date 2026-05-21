CREATE TABLE tokens_temporarios (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id VARCHAR(36) NOT NULL REFERENCES usuarios(id),
  tipo ENUM('convite','recuperacao_senha') NOT NULL,
  token_hash VARCHAR(512) UNIQUE NOT NULL,
  expira_em DATETIME NOT NULL,
  usado_em DATETIME,
  criado_em DATETIME NOT NULL DEFAULT NOW()
);
