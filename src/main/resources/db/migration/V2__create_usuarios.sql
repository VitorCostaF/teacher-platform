CREATE TYPE perfil_enum AS ENUM ('professor','aluno','responsavel','coordenador','admin');

CREATE TABLE usuarios (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  escola_id BIGINT NOT NULL REFERENCES escolas(id),
  nome VARCHAR(150) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  senha_hash VARCHAR(255),
  perfil perfil_enum NOT NULL,
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  avatar_url VARCHAR(500),
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  ultimo_acesso TIMESTAMPTZ
);

CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_escola_perfil ON usuarios(escola_id, perfil);
