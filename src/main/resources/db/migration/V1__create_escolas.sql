CREATE TABLE escolas (
  id BIGSERIAL PRIMARY KEY,
  nome VARCHAR(255) NOT NULL,
  cnpj VARCHAR(14) UNIQUE NOT NULL,
  logo_url VARCHAR(500),
  nota_minima_aprovacao NUMERIC(4,2) DEFAULT 5.0 NOT NULL,
  frequencia_minima_aprovacao NUMERIC(5,2) DEFAULT 75.0 NOT NULL,
  sistema_avaliacao VARCHAR(30) NOT NULL,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
