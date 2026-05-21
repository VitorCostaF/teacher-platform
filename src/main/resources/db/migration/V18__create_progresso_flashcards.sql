CREATE TABLE progresso_flashcards (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  aluno_id VARCHAR(36) NOT NULL REFERENCES usuarios(id),
  flashcard_id BIGINT NOT NULL REFERENCES flashcards(id),
  resultado ENUM('facil','medio','dificil') NOT NULL,
  revisado_em DATETIME NOT NULL DEFAULT NOW(),
  proxima_revisao DATETIME NOT NULL
);

CREATE INDEX idx_progresso_flashcards_aluno ON progresso_flashcards(aluno_id, proxima_revisao);
