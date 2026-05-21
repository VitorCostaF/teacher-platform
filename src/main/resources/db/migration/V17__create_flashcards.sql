CREATE TABLE flashcards (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  conteudo_id BIGINT REFERENCES conteudos(id),
  turma_id BIGINT NOT NULL REFERENCES turmas(id),
  pergunta TEXT NOT NULL,
  resposta TEXT NOT NULL,
  gerado_por_ia BOOLEAN NOT NULL DEFAULT FALSE,
  criado_em DATETIME NOT NULL DEFAULT NOW()
);
