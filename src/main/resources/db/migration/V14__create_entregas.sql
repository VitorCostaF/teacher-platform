CREATE TABLE entregas (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  avaliacao_id BIGINT NOT NULL REFERENCES avaliacoes(id),
  aluno_id VARCHAR(36) NOT NULL REFERENCES usuarios(id),
  status ENUM('nao_iniciada','rascunho','entregue','corrigida') NOT NULL DEFAULT 'nao_iniciada',
  iniciado_em DATETIME,
  entregue_em DATETIME,
  nota_automatica NUMERIC(5,2),
  nota_final NUMERIC(5,2),
  entrega_atrasada BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT uq_entrega_avaliacao_aluno UNIQUE (avaliacao_id, aluno_id)
);
