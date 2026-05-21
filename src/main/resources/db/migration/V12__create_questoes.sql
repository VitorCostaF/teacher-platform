CREATE TABLE questoes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  avaliacao_id BIGINT NOT NULL REFERENCES avaliacoes(id),
  ordem INTEGER NOT NULL,
  tipo ENUM('multipla_escolha','verdadeiro_falso','dissertativa','upload_arquivo') NOT NULL,
  enunciado TEXT NOT NULL,
  alternativas JSON,
  gabarito_dissertativo TEXT,
  dificuldade VARCHAR(20),
  topico VARCHAR(150),
  pontos NUMERIC(5,2) NOT NULL DEFAULT 1.0
);
