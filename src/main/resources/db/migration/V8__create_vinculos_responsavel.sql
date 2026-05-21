CREATE TABLE vinculos_responsavel (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  responsavel_id VARCHAR(36) NOT NULL REFERENCES usuarios(id),
  aluno_id VARCHAR(36) NOT NULL REFERENCES usuarios(id),
  parentesco VARCHAR(50),
  CONSTRAINT uq_vinculo_responsavel_aluno UNIQUE (responsavel_id, aluno_id)
);
