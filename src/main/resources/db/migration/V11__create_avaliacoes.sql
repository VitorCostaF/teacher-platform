CREATE TYPE tipo_avaliacao_enum AS ENUM ('prova','atividade','trabalho');
CREATE TYPE status_avaliacao_enum AS ENUM ('rascunho','agendada','publicada','encerrada');
CREATE TYPE gabarito_liberacao_enum AS ENUM ('imediata','apos_encerramento','manual');

CREATE TABLE avaliacoes (
  id BIGSERIAL PRIMARY KEY,
  turma_id BIGINT NOT NULL REFERENCES turmas(id),
  professor_id UUID NOT NULL REFERENCES usuarios(id),
  titulo VARCHAR(255) NOT NULL,
  tipo tipo_avaliacao_enum NOT NULL,
  status status_avaliacao_enum NOT NULL DEFAULT 'rascunho',
  peso_nota NUMERIC(4,2),
  duracao_minutos INTEGER,
  disponivel_em TIMESTAMPTZ,
  encerra_em TIMESTAMPTZ,
  embaralhar_questoes BOOLEAN NOT NULL DEFAULT FALSE,
  embaralhar_alternativas BOOLEAN NOT NULL DEFAULT FALSE,
  gabarito_liberacao gabarito_liberacao_enum NOT NULL DEFAULT 'apos_encerramento',
  permite_entrega_atrasada BOOLEAN NOT NULL DEFAULT FALSE,
  gerado_por_ia BOOLEAN NOT NULL DEFAULT FALSE,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_avaliacoes_turma_id ON avaliacoes(turma_id);
CREATE INDEX idx_avaliacoes_professor_id ON avaliacoes(professor_id);
