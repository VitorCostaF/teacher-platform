CREATE TABLE respostas (
  id BIGSERIAL PRIMARY KEY,
  entrega_id BIGINT NOT NULL REFERENCES entregas(id),
  questao_id BIGINT NOT NULL REFERENCES questoes(id),
  resposta_indice INTEGER,
  resposta_texto TEXT,
  arquivo_url VARCHAR(500),
  correta BOOLEAN,
  nota_manual NUMERIC(5,2)
);
