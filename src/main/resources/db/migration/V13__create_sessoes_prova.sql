CREATE TABLE sessoes_prova (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  avaliacao_id BIGINT NOT NULL REFERENCES avaliacoes(id),
  aluno_id VARCHAR(36) NOT NULL REFERENCES usuarios(id),
  iniciada_em DATETIME NOT NULL DEFAULT NOW(),
  encerrada_em DATETIME,
  entregue_manualmente BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT uq_sessao_prova_avaliacao_aluno UNIQUE (avaliacao_id, aluno_id)
);
