CREATE TABLE sessoes_prova (
  id BIGSERIAL PRIMARY KEY,
  avaliacao_id BIGINT NOT NULL REFERENCES avaliacoes(id),
  aluno_id UUID NOT NULL REFERENCES usuarios(id),
  iniciada_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  encerrada_em TIMESTAMPTZ,
  entregue_manualmente BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT uq_sessao_prova_avaliacao_aluno UNIQUE (avaliacao_id, aluno_id)
);
