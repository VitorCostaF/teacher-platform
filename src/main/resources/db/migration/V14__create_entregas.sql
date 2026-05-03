CREATE TYPE status_entrega_enum AS ENUM ('nao_iniciada','rascunho','entregue','corrigida');

CREATE TABLE entregas (
  id BIGSERIAL PRIMARY KEY,
  avaliacao_id BIGINT NOT NULL REFERENCES avaliacoes(id),
  aluno_id UUID NOT NULL REFERENCES usuarios(id),
  status status_entrega_enum NOT NULL DEFAULT 'nao_iniciada',
  iniciado_em TIMESTAMPTZ,
  entregue_em TIMESTAMPTZ,
  nota_automatica NUMERIC(5,2),
  nota_final NUMERIC(5,2),
  entrega_atrasada BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT uq_entrega_avaliacao_aluno UNIQUE (avaliacao_id, aluno_id)
);
