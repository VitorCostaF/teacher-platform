CREATE TYPE tipo_conteudo_enum AS ENUM ('texto','video','arquivo','link');

CREATE TABLE conteudos (
  id BIGSERIAL PRIMARY KEY,
  turma_id BIGINT NOT NULL REFERENCES turmas(id),
  professor_id UUID NOT NULL REFERENCES usuarios(id),
  titulo VARCHAR(255) NOT NULL,
  tipo tipo_conteudo_enum NOT NULL,
  corpo TEXT,
  arquivo_url VARCHAR(500),
  video_url VARCHAR(500),
  link_externo VARCHAR(500),
  publicado_em TIMESTAMPTZ,
  serie_sugerida VARCHAR(50),
  topicos TEXT[],
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
