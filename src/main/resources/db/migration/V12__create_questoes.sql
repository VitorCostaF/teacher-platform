CREATE TYPE tipo_questao_enum AS ENUM ('multipla_escolha','verdadeiro_falso','dissertativa','upload_arquivo');

CREATE TABLE questoes (
  id BIGSERIAL PRIMARY KEY,
  avaliacao_id BIGINT NOT NULL REFERENCES avaliacoes(id),
  ordem INTEGER NOT NULL,
  tipo tipo_questao_enum NOT NULL,
  enunciado TEXT NOT NULL,
  alternativas JSONB,
  gabarito_dissertativo TEXT,
  dificuldade VARCHAR(20),
  topico VARCHAR(150),
  pontos NUMERIC(5,2) NOT NULL DEFAULT 1.0
);
