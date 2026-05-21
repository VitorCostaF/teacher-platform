CREATE TABLE matriculas (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  turma_id BIGINT NOT NULL REFERENCES turmas(id),
  aluno_id VARCHAR(36) NOT NULL REFERENCES usuarios(id),
  matriculado_em DATETIME NOT NULL DEFAULT NOW(),
  removido_em DATETIME,
  CONSTRAINT uq_matriculas_turma_aluno UNIQUE (turma_id, aluno_id)
);
