CREATE TYPE tipo_token_enum AS ENUM ('convite','recuperacao_senha');

CREATE TABLE tokens_temporarios (
  id BIGSERIAL PRIMARY KEY,
  usuario_id UUID NOT NULL REFERENCES usuarios(id),
  tipo tipo_token_enum NOT NULL,
  token_hash VARCHAR(512) UNIQUE NOT NULL,
  expira_em TIMESTAMPTZ NOT NULL,
  usado_em TIMESTAMPTZ,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
