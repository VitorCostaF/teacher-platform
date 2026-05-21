CREATE TABLE logs_auditoria (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  escola_id BIGINT NOT NULL REFERENCES escolas(id),
  usuario_id VARCHAR(36) REFERENCES usuarios(id),
  acao VARCHAR(100) NOT NULL,
  entidade VARCHAR(100) NOT NULL,
  entidade_id VARCHAR(255),
  dados_anteriores JSON,
  motivo TEXT,
  ip VARCHAR(45),
  criado_em DATETIME NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_logs_auditoria_escola ON logs_auditoria(escola_id);
CREATE INDEX idx_logs_auditoria_usuario ON logs_auditoria(usuario_id);
