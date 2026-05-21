CREATE TABLE usuarios (
  id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
  escola_id BIGINT NOT NULL REFERENCES escolas(id),
  nome VARCHAR(150) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  senha_hash VARCHAR(255),
  perfil ENUM('professor','aluno','responsavel','coordenador','admin') NOT NULL,
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  avatar_url VARCHAR(500),
  criado_em DATETIME NOT NULL DEFAULT NOW(),
  ultimo_acesso DATETIME
);

CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_escola_perfil ON usuarios(escola_id, perfil);
