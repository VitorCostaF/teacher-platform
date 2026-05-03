CREATE TABLE flashcards (
  id BIGSERIAL PRIMARY KEY,
  conteudo_id BIGINT REFERENCES conteudos(id),
  turma_id BIGINT NOT NULL REFERENCES turmas(id),
  pergunta TEXT NOT NULL,
  resposta TEXT NOT NULL,
  gerado_por_ia BOOLEAN NOT NULL DEFAULT FALSE,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
