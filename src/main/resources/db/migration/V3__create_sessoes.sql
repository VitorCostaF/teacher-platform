CREATE TABLE sessoes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id VARCHAR(36) NOT NULL REFERENCES usuarios(id),
  refresh_token_hash VARCHAR(512) NOT NULL,
  user_agent VARCHAR(500),
  ip VARCHAR(45),
  criado_em DATETIME NOT NULL DEFAULT NOW(),
  expira_em DATETIME NOT NULL,
  revogado_em DATETIME
);

CREATE INDEX idx_sessoes_usuario_id ON sessoes(usuario_id);
