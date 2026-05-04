CREATE TABLE flashcard_estado_sm2 (
  id BIGSERIAL PRIMARY KEY,
  aluno_id UUID NOT NULL REFERENCES usuarios(id),
  flashcard_id BIGINT NOT NULL REFERENCES flashcards(id),
  intervalo_dias INTEGER NOT NULL DEFAULT 1,
  fator_facilidade NUMERIC(4,2) NOT NULL DEFAULT 2.5,
  proxima_revisao DATE NOT NULL DEFAULT CURRENT_DATE,
  total_revisoes INTEGER NOT NULL DEFAULT 0,
  UNIQUE(aluno_id, flashcard_id)
);
CREATE INDEX idx_flashcard_estado_sm2_aluno_revisao ON flashcard_estado_sm2(aluno_id, proxima_revisao);
