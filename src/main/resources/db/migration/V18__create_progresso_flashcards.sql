CREATE TYPE resultado_flashcard_enum AS ENUM ('facil','medio','dificil');

CREATE TABLE progresso_flashcards (
  id BIGSERIAL PRIMARY KEY,
  aluno_id UUID NOT NULL REFERENCES usuarios(id),
  flashcard_id BIGINT NOT NULL REFERENCES flashcards(id),
  resultado resultado_flashcard_enum NOT NULL,
  revisado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  proxima_revisao TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_progresso_flashcards_aluno ON progresso_flashcards(aluno_id, proxima_revisao);
