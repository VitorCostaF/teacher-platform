CREATE TABLE sessoes (
  id BIGSERIAL PRIMARY KEY,
  usuario_id UUID NOT NULL REFERENCES usuarios(id),
  refresh_token_hash VARCHAR(512) NOT NULL,
  user_agent VARCHAR(500),
  ip VARCHAR(45),
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  expira_em TIMESTAMPTZ NOT NULL,
  revogado_em TIMESTAMPTZ
);

CREATE INDEX idx_sessoes_usuario_id ON sessoes(usuario_id);
